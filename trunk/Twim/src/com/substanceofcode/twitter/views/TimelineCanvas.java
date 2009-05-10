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
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

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
    private Menu photoServiceMenu;
    private Menu photoSourceMenu;
    private int verticalScroll;
    
    /** 
     * Creates a new instance of TimelineCanvas
     * @param controller Application controller
     */
    public TimelineCanvas(TwitterController controller) {
        this.controller = controller;
        setFullScreenMode(true);
        
        /** Menu bar tabs */
        String[] labels = {"Archive", "Replies", "Recent", "Direct", "Favorites", "Friends", "Public"};
        menuBar = new TabBar(2, labels, getWidth());
        
        /** Menu */
        String[] menuLabels = {"Update status", "Send photo",
            "Reload tweets", "Settings", "About", "Exit", "Cancel"};
        menu = new Menu(menuLabels, getWidth(), getHeight());

        /** Photo source */
        String[] photoSourceLabels = {"Camera", "File"};
        photoSourceMenu = new Menu(photoSourceLabels, getWidth(), getHeight());
        photoSourceMenu.setTitle("Select source");

        String[] photoServiceLabels = {"Twitgoo", "TwitPic", "yFrog", "Cancel"};
        photoServiceMenu = new Menu(photoServiceLabels, getWidth(), getHeight());
        photoServiceMenu.setTitle("Select service");

        /** Status menu */
        String[] statusMenuLabels = {"Open in browser", "Open link in browser",
            "Reply", "Retweet", "Mark as favorite", "Send direct message", "Cancel"};
        statusMenu = new Menu(statusMenuLabels, getWidth(), getHeight());
        statusMenu.setTitle("Status menu");

        /** Status list control */
        statusList = new StatusList(getWidth(), getHeight());        
        
        verticalScroll = 0;
    }

    public void resetScrolling() {
        verticalScroll = 0;
    }

    public void setTimeline(Vector friendsTimeline) {
        this.statuses = friendsTimeline;
    }

    protected void paint(Graphics g) {
        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if( menu.isActive()==false &&
                statusMenu.isActive()==false &&
                photoServiceMenu.isActive()==false &&
                photoSourceMenu.isActive()==false) {
            boolean drawSelectionBox = menuBar.isSelectedActive();
            statusList.draw(
                    g, statuses,
                    menuBar.getHeight() + verticalScroll + TalkBalloon.textFont.getHeight()/2,
                    drawSelectionBox);
            menuBar.draw(g, 0);
        } else if(menu.isActive()) {
            menu.draw(g);
        } else if(statusMenu.isActive()) {
            statusMenu.draw(g);
        } else if(photoServiceMenu.isActive()) {
            photoServiceMenu.draw(g);
        } else if(photoSourceMenu.isActive()) {
            photoSourceMenu.draw(g);
        }
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
            controller.showRecentTimeline();
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
        int gameAction = this.getGameAction(keyCode);
        if(gameAction == Canvas.UP) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectPrevious();
            } else if(statusMenu.isActive()) {
                statusMenu.selectPrevious();
            } else if(photoServiceMenu.isActive()) {
                photoServiceMenu.selectPrevious();
            } else if(photoSourceMenu.isActive()) {
                photoSourceMenu.selectPrevious();
            } else {
                verticalScroll += getHeight()/6;
                if(verticalScroll>0) {
                    verticalScroll = 0;
                }            
            }
        } else if(gameAction == Canvas.DOWN) {
            menuBar.resetSelectedTab();
            if(menu.isActive()) {
                menu.selectNext();
            } else if(statusMenu.isActive()) {
                statusMenu.selectNext();
            } else if(photoServiceMenu.isActive()) {
                photoServiceMenu.selectNext();
            } else if(photoSourceMenu.isActive()) {
                photoSourceMenu.selectNext();
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
            /** Photo service */
            menu.deactivate();
            photoServiceMenu.activate();
            repaint();
        } else if(selectedIndex==2) {
            controller.clearTimelines();
            handleTabChange();
        } else if(selectedIndex==3) {
            controller.showLoginForm();
        } else if(selectedIndex==4) {
            controller.about();
        } else if(selectedIndex==5) {
            controller.exit();
        } else if(selectedIndex==6) {
            /** Cancel = Do nothing */
            repaint();
        }
    }

    public void activatePhotoServiceMenuIten() {
        int selectedIndex = photoServiceMenu.getSelectedIndex();
        switch(selectedIndex) {
            case(0):
                /** Twitgoo */
                controller.setTwitgooAsCurrentPhotoService();
                photoSourceMenu.activate();
                repaint();
                break;
            case(1):
                /** TinyPic */
                controller.setTwitPicAsCurrentPhotoService();
                photoSourceMenu.activate();
                repaint();
                break;
            case(2):
                /** yfrog */
                controller.setYfrogAsCurrentPhotoService();
                photoSourceMenu.activate();
                repaint();
                break;
            case(3):
                /** Cancel */
                menu.activate();
                repaint();
                break;
            default:
        }
        photoServiceMenu.deactivate();
    }

    public void activatePhotoSourceMenuItem() {
        if(photoSourceMenu.getSelectedIndex()==0) {
            /** Camera */
            controller.showCamera();
        } else {
            /** File */
            controller.showFileBrowser();
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
            /** Cancel = Do nothing */
            repaint();
        }
    }
    
    public void keyPressed(int keyCode) {
        int gameAction = this.getGameAction(keyCode);
        String keyName = this.getKeyName(keyCode);
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
        } else if(gameAction == Canvas.FIRE) {
            
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
            } else if(photoServiceMenu.isActive()) {
                photoServiceMenu.deactivate();
                activatePhotoServiceMenuIten();
                return;
            } else if(photoSourceMenu.isActive()) {
                photoSourceMenu.deactivate();
                activatePhotoSourceMenuItem();
                return;
            } else if(statusList.getSelected()!=null){
                Status selectedStatus = statusList.getSelected();
                if(selectedStatus.isFavorite()) {
                    statusMenu.setLabel(4, "Unfavorite");
                } else {
                    statusMenu.setLabel(4, "Mark as favorite");
                }
                statusMenu.activate();
            }
                
        } else if( (keyName.indexOf("SOFT")>=0 && keyName.indexOf("1")>0) ||
            (Device.isNokia() && keyCode==-6) ||
            keyCode == TimelineCanvas.KEY_STAR ||
            keyCode == Canvas.KEY_NUM0 ||
            keyCode == ' ') {
            /** Left soft key pressed */
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();
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
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();
                return;
            } else {
                menu.activate();
            }
        }
        handleUpAndDownKeys(keyCode);
        repaint();
    }
    
    
    
}
