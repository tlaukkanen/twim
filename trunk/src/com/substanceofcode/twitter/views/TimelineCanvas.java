/*
 * TimelineCanvas.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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
    private int verticalScroll;
    
    /** 
     * Creates a new instance of TimelineCanvas
     * @param controller Application controller
     */
    public TimelineCanvas(TwitterController controller) {
        this.controller = controller;
        setFullScreenMode(true);
        
        /** Menu bar tabs */
        String[] labels = {"Archive", "Replies", "Recent", "Public"};
        menuBar = new TabBar(2, labels, getWidth());
        
        /** Menu */
        String[] menuLabels = {"Update status", "Settings", "About", "Exit", "Cancel"};
        menu = new Menu(menuLabels, getWidth(), getHeight());

        /** Status menu */
        String[] statusMenuLabels = {"Open in browser", "Open link in browser", "Reply", "Cancel"};
        statusMenu = new Menu(statusMenuLabels, getWidth(), getHeight());

        /** Status list control */
        statusList = new StatusList(getWidth(), getHeight());        
        
        verticalScroll = 0;
    }

    public void setTimeline(Vector friendsTimeline) {
        this.statuses = friendsTimeline;
    }

    protected void paint(Graphics g) {
        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        if(menu.isActive()==false && statusMenu.isActive()==false) {
            statusList.draw(g, statuses, menuBar.getHeight() + verticalScroll + TalkBalloon.textFont.getHeight()/2);
            menuBar.draw(g, 0);
        } else if(menu.isActive()) {
            menu.draw(g);
        } else if(statusMenu.isActive()) {
            statusMenu.draw(g);
        }
    }

    private void handleTabChange() {
        verticalScroll = 0;
        int tabIndex = menuBar.getSelectedTabIndex();
        if(tabIndex==0) {
            /** Archive selected */
            controller.showArchiveTimeline();
        } else if(tabIndex==1) {
            /** Friends selected */
            controller.showResponsesTimeline();
        } else if(tabIndex==2) {
            /** Friends selected */
            controller.showFriendsTimeline();
        } else if(tabIndex==3) {
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
            if(menu.isActive()) {
                menu.selectPrevious();
            } else if(statusMenu.isActive()) {
                statusMenu.selectPrevious();
            } else {
                verticalScroll += getHeight()/6;// menuBar.getHeight();
                if(verticalScroll>0) {
                    verticalScroll = 0;
                }            
            }
        } else if(gameAction == Canvas.DOWN) {
            if(menu.isActive()) {
                menu.selectNext();
            } else if(statusMenu.isActive()) {
                statusMenu.selectNext();
            } else {
                verticalScroll -= getHeight()/6; //menuBar.getHeight();
            }
        }        
    }
    
    public void activateMenuItem() {
        int selectedIndex = menu.getSelectedIndex();
        if(selectedIndex==0) {
            controller.showStatusView("");
        } else if(selectedIndex==1) {
            controller.showLoginForm();
        } else if(selectedIndex==2) {
            controller.about();
        } else if(selectedIndex==3) {
            controller.exit();
        } else if(selectedIndex==4) {
            /** Cancel = Do nothing */
        }
    }

    public void activateStatusMenuItem() {
        int selectedIndex = statusMenu.getSelectedIndex();
        Status selectedStatus = statusList.getSelected();
        if(selectedIndex==0) {
            if(selectedStatus!=null) {
                selectedStatus.openInBrowser(controller.getMIDlet());
                return;
            }
        } else if(selectedIndex==1) {
            if(selectedStatus!=null) {
                selectedStatus.openIncludedLink(controller.getMIDlet());
                return;
            }
        } else if(selectedIndex==2) {
            if(selectedStatus!=null) {
                controller.showStatusView("@" + selectedStatus.getScreenName() + " ");
            }
        }else if(selectedIndex==3) {
            /** Cancel = Do nothing */
        }
    }
    
    public void keyPressed(int keyCode) {
        int gameAction = this.getGameAction(keyCode);
        String keyName = this.getKeyName(keyCode);
        if(gameAction == Canvas.LEFT) {
            menuBar.selectPreviousTab();
            handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.RIGHT) {
            menuBar.selectNextTab();
            handleTabChange();
            repaint();
            return;
        } else if(gameAction == Canvas.FIRE) {
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();
            } else if(statusMenu.isActive()) {
                statusMenu.deactivate();
                activateStatusMenuItem();
            } else if(statusList.getSelected()!=null){
                statusMenu.activate();
            }
                
        } else if( keyName.indexOf("SOFT")>=0 && keyName.indexOf("1")>0 ||
            (Device.isNokia() && keyCode==-6) ||
            keyCode == TimelineCanvas.KEY_STAR) {
            /** Left soft key pressed */
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();                
            } else {
                menu.activate();
            }
        } else if( ((keyName.indexOf("SOFT")>=0 && keyName.indexOf("2")>0) ||
            (Device.isNokia() && keyCode==-7) ||
            keyCode == TimelineCanvas.KEY_POUND) ) {
            /** Right soft key pressed */
            if(menu.isActive()) {
                menu.deactivate();
                activateMenuItem();                
            } else {
                menu.activate();
            }
        }
        handleUpAndDownKeys(keyCode);
        repaint();
    }
    
    
    
}
