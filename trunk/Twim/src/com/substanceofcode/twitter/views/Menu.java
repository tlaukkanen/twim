/*
 * Menu.java
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
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.TimeUtil;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Menu implementation that will render an overlay menu.
 * @author Tommi Laukkanen
 */
public class Menu {

    private String[] labels;
    private MenuAction[] actions;
    private int screenWidth;
    private int screenHeight;
    private int top = 0;
    private int height;
    private int selectedIndex;
    private boolean active;
    private String title;
    private int rowHeight = 10;
    private boolean alignLeft;
    private int rowsPerScreen;
    private int boxHeight = 24;
    private int boxWidth = 32;
    private int longestLabelWidth = 10;

    private static final Font TITLE_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    private static final Font LABEL_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    private static final int BACK_COLOR = 0xaaaaaa;
    private static final int SELECTED_COLOR = 0xffffff;
    private static final int FONT_COLOR = 0x000000;
    private static final int BORDER_COLOR = 0x666666;
    
    /** Create new Menu instance 
     * @param labels        
     * @param screenWidth 
     * @param screenHeight 
     */
    public Menu(String[] labels, MenuAction[] actions, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.actions = actions;
        selectedIndex = 0;
        active = false;
        title = "Menu";
        alignLeft = false;
        calculateSize();
        setLabels(labels);
    }

    public void alignLeft(boolean align) {
        this.alignLeft = align;
    }

    
    /** 
     * Draw menu
     * @param g Application graphics.
     */
    public void draw(Graphics g) {
        if(active==false) {
            return;
        }

        if(alignLeft==true) {
            top = 11;
            if(selectedIndex>(rowsPerScreen-3)) {
                top -= (selectedIndex-(rowsPerScreen-3))*rowHeight;
            }
        }

        int menuWidth = (alignLeft ? screenWidth-4 : longestLabelWidth);

        /** Draw background and borders */
        g.setColor(BACK_COLOR);
        g.fillRect(screenWidth/2 - menuWidth/2, top-2, menuWidth, height+4);
        g.setColor(BORDER_COLOR);
        g.drawRect(screenWidth/2 - menuWidth/2, top-2, menuWidth, height+4);
        
        /** Draw menu items */
        g.setColor(FONT_COLOR);
        g.setFont(TITLE_FONT);

        g.drawString(
            title,
            screenWidth/2 - LABEL_FONT.stringWidth(title)/2,
            top + TITLE_FONT.getHeight(),
            Graphics.LEFT|Graphics.BOTTOM);
        g.setFont(LABEL_FONT);

        /** Draw time stamp */
        g.setColor(0xAAAAAA);
        String currentTime = TimeUtil.getCurrentTime();
        if(currentTime!=null) {
            g.drawString(
                    currentTime,
                    screenWidth/2 - LABEL_FONT.stringWidth(currentTime)/2,
                    LABEL_FONT.getHeight(),
                    Graphics.BOTTOM|Graphics.LEFT);
        }

        g.setColor(FONT_COLOR);
        int col = (alignLeft ? 13 : 0 );
        for(int menuIndex=0; menuIndex<labels.length; menuIndex++) {
            if(menuIndex==selectedIndex) {
                g.setColor(SELECTED_COLOR);
                g.fillRect(screenWidth/2 - menuWidth/2+1, top+(menuIndex+1)*rowHeight, menuWidth-2, rowHeight);
                g.setColor(FONT_COLOR);
            }
            String label = labels[ menuIndex ];
            int labelWidth = LABEL_FONT.stringWidth(label);
            if(alignLeft==false) {
                col = screenWidth/2 - labelWidth/2;
            }
            g.drawString(labels[menuIndex], col, top + (menuIndex+2)*rowHeight-(rowHeight/2-LABEL_FONT.getHeight()/2), Graphics.LEFT|Graphics.BOTTOM);
        }

        if(Device.isTouch()) {
            drawBackButton(g);
        }
    }

    private void drawBackButton(Graphics g) {
        Font f = g.getFont();
        int textWidth = f.stringWidth("Menu");
        boxHeight = 24;
        boxWidth = 32;

        if(textWidth>32) {
            boxWidth = textWidth + 2;
        }
        boxHeight = f.getHeight()*2;
        g.setColor(0xaaaaaa);
        g.fillRect(0, screenHeight-boxHeight, screenWidth, boxHeight);
        g.setColor(0x999999);
        g.drawLine(screenWidth/2, screenHeight-boxHeight+2, screenWidth/2, screenHeight-4);
        g.setColor(0x444444);
        g.drawLine(0, screenHeight-boxHeight, screenWidth, screenHeight-boxHeight);
        g.drawString("Back", screenWidth-boxWidth+boxWidth/2-textWidth/2, screenHeight-boxHeight/2+f.getHeight()/2, Graphics.LEFT|Graphics.BOTTOM);
    }
    
    public void selectNext() {
        selectedIndex++;
        if(selectedIndex>labels.length-1) {
            selectedIndex = 0;
        }
    }
    
    public void selectPrevious() {
        selectedIndex--;
        if(selectedIndex<0) {
            selectedIndex = labels.length-1;
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }

    String getSelectedLabel() {
        if(labels==null || selectedIndex<0 || selectedIndex>labels.length-1) {
            return null;
        }
        return labels[selectedIndex];
    }

    boolean isActive() {
        return active;
    }
    
    public void activate() {
        active = true;
        if(Device.isTouch()) {
            selectedIndex = -1;
        } else {
            selectedIndex = 0;
        }
    }

    public void activateSelected() {
        if(actions!=null) {
            actions[ getSelectedIndex() ].activate();
        }
    }
    
    public void deactivate() {
        active = false;
    }

    /**
     * Set labels for the menu.
     * @param labels
     */
    void setLabels(String[] labels) {
        this.labels = labels;

        /** Check for longest line */
        if(labels!=null) {
            longestLabelWidth = TITLE_FONT.stringWidth(title);
            for(int i=0; i<labels.length; i++) {
                String label = labels[i];
                int labelWidth = LABEL_FONT.stringWidth(label);
                if(labelWidth>longestLabelWidth) {
                    longestLabelWidth = labelWidth;
                }
            }
            longestLabelWidth += LABEL_FONT.getHeight();
        }

        calculateSize();
        selectedIndex = 0;
    }

    void setLabel(int index, String label) {
        labels[index] = label;
    }

    void setTitle(String title) {
        this.title = title;
    }

    /**
     * Select menu item with touch screen
     * @param x coordinate
     * @param y coordinate
     */
    public void selectWithPointer(int x, int y, boolean isPress) {
        /** Check for back button */
        Log.debug("x: " + x + " y: " + y);
        if(Device.isTouch() && x>screenWidth/2 && y>screenHeight-boxHeight && isPress) {
            TwitterController.getInstance().showTimeline();
            return;
        }
        /** Check that pointer is on top of menu item */
        int menuWidth = (alignLeft ? screenWidth : longestLabelWidth);
        if( x>screenWidth/2-menuWidth/2 &&
            x<screenWidth/2+menuWidth/2) {
            int canvasY = y-top;
            int pointerIndex = canvasY / rowHeight - 1;
            selectedIndex = pointerIndex;
        } else {
            selectedIndex = -1;
        }
    }

    private void calculateSize() {
        if(Device.isTouch()) {
            rowHeight = LABEL_FONT.getHeight()+LABEL_FONT.getHeight()/2;
        } else {
            rowHeight = LABEL_FONT.getHeight();
        }
        if(labels!=null) {
            this.height = (labels.length+1) * rowHeight;
            this.top = screenHeight/2 - height/2;
        }
        rowsPerScreen = screenHeight/rowHeight;
    }

    public void setSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        calculateSize();
    }


    
}
