/*
 * TimelineCanvas.java
 *
 * Copyright (C) 2005-2009 Tommi Laukkanen
 * http://www.substanceofcode.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.substanceofcode.twitter.views;

import com.substanceofcode.infrastructure.Device;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.Log;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * TimelineCanvas
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TimelineCanvas extends Canvas {

    private TwitterController controller;
    private Vector statuses;
    private StatusList statusList;
    private TabBar menuBar;
    private Menu menu;
    private Menu statusMenu;
    private Menu mediaSourceMenu;
    private int verticalScroll;
    private Point pointerPressedPoint = new Point(0, 0);
    private int lastY; /** Last touch coordinates */
    private int screenWidth; /** Screen width to identify the screen rotation */
    private String debug = "x";
    private int boxWidth = 20, boxHeight = 20;
    private static final Font LABEL_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private static final int HOME_TAB = 2;
    
    /** 
     * Creates a new instance of TimelineCanvas
     * @param controller Application controller
     */
    public TimelineCanvas(TwitterController controller) {
        this.controller = controller;
        setFullScreenMode(true);
        
        /** Menu bar tabs */
        String[] labels = {
            "Archive",
            "Replies",
            "Home",
            "Direct",
            "Favorites",
            "Following",
            "Public"};
        menuBar = new TabBar(2, labels);
        
        /** Menu */
        String[] menuLabels = {
            "Update status",
            "Send media",
            "Reload tweets",
            "Search tweets",
            "Settings",
            "About",
            "Exit",
            "Minimize"};
        menu = new Menu(menuLabels, null, getWidth(), getHeight());

        /** Photo source */
        String[] photoSourceLabels = {
            "Camera",
            "Photo from file",
            "Video from file",
            "Cancel"};
        mediaSourceMenu = new Menu(photoSourceLabels, null, getWidth(), getHeight());
        mediaSourceMenu.setTitle("Select source");

        /** Status menu */
        String[] statusMenuLabels = {
            "Open in browser",
            "Open link in browser",
            "Reply",
            "Retweet",
            "Mark as favorite",
            "Send direct message",
            "Follow"};
        statusMenu = new Menu(statusMenuLabels, null, getWidth(), getHeight());
        statusMenu.setTitle("Status menu");

        /** Status list control */
        statusList = new StatusList(getWidth(), getHeight());        
        statusList.setSize(getWidth(),getHeight());

        
        verticalScroll = 0;
        screenWidth = getWidth();
    }

    public void resetMenus() {
        menu.deactivate();
        statusMenu.deactivate();
        mediaSourceMenu.deactivate();
    }

    public void resetScrolling() {
        verticalScroll = 0;
    }

    public void setTimeline(Vector friendsTimeline) {
        if(friendsTimeline.isEmpty()) {
            this.statuses = new Vector();
            this.statuses.addElement(
                new Status("Twim", "Sorry... No statuses to display",
                    Calendar.getInstance().getTime(), "")
            );
        }
        this.statuses = friendsTimeline;
    }

    public void paint(Graphics g) {
        /** Check for screen rotation change */
        if(screenWidth != getWidth()) {
            menu.setSize(getWidth(), getHeight());
            statusMenu.setSize(getWidth(), getHeight());
            mediaSourceMenu.setSize(getWidth(), getHeight());
            statusList.setSize(getWidth(),getHeight());
            screenWidth = getWidth();
            if(statuses!=null) {
                Enumeration en = statuses.elements();
                while(en.hasMoreElements()) {
                    Status status = (Status)en.nextElement();
                    status.clearTextLines();
                }
            }
        }

        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if( menu.isActive()==false &&
                statusMenu.isActive()==false &&
                mediaSourceMenu.isActive()==false) {
            boolean drawSelectionBox = menuBar.isSelectedActive();
            statusList.draw(
                    g, statuses,
                    menuBar.getHeight() + verticalScroll + TalkBalloon.textFont.getHeight()/2,
                    drawSelectionBox);
            menuBar.draw(g, 0, getWidth());
            if(Device.isTouch()) {
                drawMenuButton(g);
            }


        } else if(menu.isActive()) {
            menu.draw(g);
        } else if(statusMenu.isActive()) {
            statusMenu.draw(g);
        } else if(mediaSourceMenu.isActive()) {
            mediaSourceMenu.draw(g);
        }

        //g.drawString(debug, 0, 40, Graphics.LEFT|Graphics.BOTTOM);
    }

    private void handleTabChange() {
        verticalScroll = 0;
        int tabIndex = menuBar.getSelectedTabIndex();
        if(tabIndex==0) {
            /** Archive selected */
            controller.showArchiveTimeline();
        } else if(tabIndex==1) {
            /** Responses selected */
            controller.showResponsesTimeline();
        } else if(tabIndex==2) {
            /** Recent selected */
            controller.showHomeTimeline();
        } else if(tabIndex==3) {
            /** Direct messages */
            controller.showDirectMessages();
        } else if(tabIndex==4) {
            /** Favorites */
            controller.showFavouriteTimeline();
        } else if(tabIndex==5) {
            /** Friends */
            controller.showFriends();
        } else if(tabIndex==6) {
            /** Public selected */
            controller.showPublicTimeline();
        }
    }
    
    /** Handle repeated key presses. */
    protected void keyRepeated(int keyCode) {
        handleUpAndDownKeys(keyCode);
        repaint();
    }

    private void handleUpAndDownKeys(int keyCode) {
        Log.debug("handle up/down");
        int gameAction = this.getGameAction(keyCode);
        if(gameAction == GameCanvas.UP) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectPrevious();
            } else if(statusMenu.isActive()) {
                statusMenu.selectPrevious();
            } else if(mediaSourceMenu.isActive()) {
                mediaSourceMenu.selectPrevious();
            } else {
                verticalScroll += getHeight()/6;
                if(verticalScroll>0) {
                    verticalScroll = 0;
                }            
            }
        } else if(gameAction == GameCanvas.DOWN) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectNext();
            } else if(statusMenu.isActive()) {
                statusMenu.selectNext();
            } else if(mediaSourceMenu.isActive()) {
                mediaSourceMenu.selectNext();
            } else {
                verticalScroll -= getHeight()/6; 
            }
        }        
    }
    
    public void activateMenuItem() {
        int selectedIndex = menu.getSelectedIndex();
        if(selectedIndex==0) {
            controller.showStatusView("");
        } else if(selectedIndex==1) {
            /** Media service */
            menu.deactivate();
            mediaSourceMenu.activate();
            repaint();
        } else if(selectedIndex==2) {
            /** Reload tweets */
            controller.clearTimelines();
            handleTabChange();
        } else if(selectedIndex==3) {
            controller.showSearchForm();
        } else if(selectedIndex==4) {
            controller.showSettingsForm();
        } else if(selectedIndex==5) {
            controller.about();
        } else if(selectedIndex==6) {
            controller.exit();
        } else if(selectedIndex==7) {
            controller.minimize();
        }
    }

    public void activateMediaSourceMenuItem() {
        if(mediaSourceMenu.getSelectedIndex()==0) {
            /** Camera */
            controller.showCamera();
        } else if(mediaSourceMenu.getSelectedIndex()==1){
            /** Photo from file */
            controller.showPhotoBrowser();
        } else if(mediaSourceMenu.getSelectedIndex()==2){
            /** Video from file */
            controller.showVideoBrowser();
        } else {
            /** Cancel */
            mediaSourceMenu.deactivate();
            repaint();
        }
    }

    public void activateStatusMenuItem() {
        int selectedIndex = statusMenu.getSelectedIndex();
        Status selectedStatus = statusList.getSelected();
        if(selectedIndex==0) {
            /** Open tweet in browser */
            if(selectedStatus!=null) {
                selectedStatus.openInBrowser(controller.getMIDlet());
                return;
            }
        } else if(selectedIndex==1) {
            /** Open tweet link in browser */
            if(selectedStatus!=null) {
                selectedStatus.openIncludedLink(controller.getMIDlet());
                return;
            }
        } else if(selectedIndex==2) {
            /** Reply to tweet */
            if(selectedStatus!=null) {
                if(selectedStatus.isDirect()) {
                    controller.showStatusView("d " + selectedStatus.getScreenName() + " ");
                } else {
                    controller.showStatusView("@" + selectedStatus.getScreenName() + " ");
                }
            }
        } else if(selectedIndex==3) {
            /** Retweet */
            if(selectedStatus!=null) {
                controller.showStatusView("RT @" + selectedStatus.getScreenName() + " \"" + selectedStatus.getText() + "\"");
            }
        } else if(selectedIndex==4) {
            /** Mark as favorite */
            if(selectedStatus!=null) {
                controller.toggleFavorite(selectedStatus);
            }
        } else if(selectedIndex==5) {
            /** Send direct message */
            if(selectedStatus!=null) {
                controller.showStatusView("d " + selectedStatus.getScreenName() + " ");
            }
        }else if(selectedIndex==6) {
            /** Follow/Unfollow */
            if(selectedStatus!=null) {
                controller.toggleFollow(selectedStatus);
            }
        }
    }
    
    public void keyPressed(int keyCode) {
        int gameAction = this.getGameAction(keyCode);
        String keyName = this.getKeyName(keyCode);
        Log.debug("key: " + keyName);
        if(gameAction == Canvas.LEFT) {
            menuBar.selectPreviousTab();
            //handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.RIGHT) {
            menuBar.selectNextTab();
            //handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.FIRE ||
                keyName.toUpperCase().startsWith("ENTER") ||
                (keyName.toUpperCase().startsWith("SPACE") &&
                    (menu.isActive() || statusMenu.isActive() || mediaSourceMenu.isActive()) ) ) {
            
            if(menuBar.isSelectedActive()==false) {
                menuBar.activateSelectedTab();
                handleTabChange();
                repaint();
                return;
            }

            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();
                return;
            } else if(statusMenu.isActive()) {
                statusMenu.deactivate();
                activateStatusMenuItem();
                return;
            } else if(mediaSourceMenu.isActive()) {
                mediaSourceMenu.deactivate();
                activateMediaSourceMenuItem();
                return;
            } else if(statusList.getSelected()!=null){
                Status selectedStatus = statusList.getSelected();
                if(selectedStatus.isFavorite()) {
                    statusMenu.setLabel(4, "Unfavorite");
                } else {
                    statusMenu.setLabel(4, "Mark as favorite");
                }
                if(selectedStatus.isFollowing()) {
                    statusMenu.setLabel(6, "Unfollow user");
                } else {
                    statusMenu.setLabel(6, "Follow user");
                }
                statusMenu.activate();
            }
                
        } else if( (keyName.indexOf("SOFT")>=0 && keyName.indexOf("1")>0) ||
            (Device.isNokia() && keyCode==-6) ||
            keyCode == TimelineCanvas.KEY_STAR ||
            keyCode == Canvas.KEY_NUM0 ||
            keyCode == ' ') {
            /** Left soft key pressed */
            if(statusMenu.isActive()) {
                statusMenu.deactivate();
            } else if(menu.isActive()) {
                menu.deactivate();
                //activateMenuItem();
                repaint();
                return;
            } else {
                menu.activate();
            }
        } else if( (keyName.indexOf("SOFT")>=0 && keyName.indexOf("2")>0) ||
            (Device.isNokia() && keyCode==-7) ||
            keyCode == TimelineCanvas.KEY_POUND ||
            keyCode == Canvas.KEY_NUM0 ||
            keyCode == ' ') {
            /** Right soft key pressed */
            if(statusMenu.isActive()) {
                statusMenu.deactivate();
            } else if(menu.isActive()) {
                menu.deactivate();
                //activateMenuItem();
                repaint();
                return;
            } else {
                menu.activate();
            }
        }
        handleUpAndDownKeys(keyCode);
        repaint();
    }

    /**
     * Handle touch screen press.
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
        Device.setTouch(true);
        if(menu.isActive()) {
            menu.selectWithPointer(x, y, true);
        } else if(statusMenu.isActive()) {
            statusMenu.selectWithPointer(x, y, true);
        } else if(mediaSourceMenu.isActive()) {
            mediaSourceMenu.selectWithPointer(x, y, true);
        } else {
            pointerPressedPoint.x = x;
            pointerPressedPoint.y = y;
            lastY = y;

            debug = "(" + x + "," + y + ")";

            if(y>getHeight()-boxHeight && x<getWidth()/2) {
                menu.activate();
            }
            if(y>getHeight()-boxHeight && x>getWidth()/2) {
                statusMenu.activate();
            }
        }
        repaint();
    }

    /**
     * Handle touch screen drag
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerDragged(int x, int y) {
        super.pointerDragged(x, y);
        if(menu.isActive()) {
            menu.selectWithPointer(x, y, false);
        } else if(statusMenu.isActive()) {
            statusMenu.selectWithPointer(x, y, false);
        } else if(mediaSourceMenu.isActive()) {
            mediaSourceMenu.selectWithPointer(x, y, false);
        } else {
            if(y<LABEL_FONT.getHeight()*2) {
                int tabWidth = getWidth()/7;
                int selectedTabIndex = pointerPressedPoint.x/tabWidth;
                menuBar.selectTab(selectedTabIndex);
                pointerPressedPoint.x = x;
                pointerPressedPoint.y = y;
            } else {
                if(lastY>0) {
                    int dy = y - lastY;
                    verticalScroll += dy;
                    lastY = y;
                }
            }
        }
        repaint();
    }

    /**
     * Handle touch screen press release event
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerReleased(int x, int y) {
        if(menu.isActive()) {
            menu.selectWithPointer(x, y, false);
            if(menu.getSelectedLabel()!=null && menu.getSelectedLabel().length()>0) {
                Log.debug("Selected menu item: " + menu.getSelectedLabel());
                menu.deactivate();
                activateMenuItem();
            }
        } else if(statusMenu.isActive()) {
            statusMenu.selectWithPointer(x, y, false);
            if(statusMenu.getSelectedLabel()!=null && statusMenu.getSelectedLabel().length()>0) {
                statusMenu.deactivate();
                activateStatusMenuItem();
            }
        } else if(mediaSourceMenu.isActive()) {
            mediaSourceMenu.selectWithPointer(x, y, false);
            if(mediaSourceMenu.getSelectedLabel()!=null && mediaSourceMenu.getSelectedLabel().length()>0) {
                mediaSourceMenu.deactivate();
                activateMediaSourceMenuItem();
            }
        } else {
            if(!menuBar.isSelectedActive()) {
                menuBar.activateSelectedTab();
                handleTabChange();
                return;
            } else {

            }
            if(lastY>0) {
                int dy = y - lastY;
                verticalScroll += dy;
                lastY = y;
            }
        }
        repaint();
    }

    private void drawMenuButton(Graphics g) {
        g.setFont(LABEL_FONT);
        int wi = LABEL_FONT.stringWidth("Menu");
        int wii = LABEL_FONT.stringWidth("Item");
        int fontHeight = LABEL_FONT.getHeight();
        boxWidth = wi + fontHeight;
        boxHeight = fontHeight*2;
        g.setColor(0xaaaaaa);
        g.fillRect(0, getHeight()-boxHeight, getWidth(), boxHeight);
        g.setColor(0x999999);
        g.drawLine(getWidth()/2, getHeight()-boxHeight+2, getWidth()/2, getHeight()-4);
        g.setColor(0x444444);
        g.drawLine(0, getHeight()-boxHeight, getWidth(), getHeight()-boxHeight);
        g.drawString("Menu", boxWidth/2-wi/2, getHeight()-boxHeight/2+fontHeight/2, Graphics.LEFT|Graphics.BOTTOM);
        g.drawString("Item", getWidth()-boxWidth+boxWidth/2-wii/2, getHeight()-boxHeight/2+fontHeight/2, Graphics.LEFT|Graphics.BOTTOM);
    }

    public void resetMenuTab() {
        menuBar.selectNothing();
    }

    public void selectHomeTab() {
        menuBar.selectTab( HOME_TAB );
    }
    
}
