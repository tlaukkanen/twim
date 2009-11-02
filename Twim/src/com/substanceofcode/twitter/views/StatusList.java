/*
 * StatusList.java
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

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.TimeUtil;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * StatusList
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class StatusList {
    
    private Font textFont = Font.getFont(
            Font.FACE_SYSTEM,
            Font.STYLE_PLAIN,
            Font.SIZE_SMALL);
    private int screenWidth;
    private int screenHeight;
    private TalkBalloon talkBalloon;
    private Status selectedStatus;
    private static Status nextPageStatus = new Status("Twim", "Click to load next page...", null, "page");
    
    /** 
     * Creates a new instance of StatusList
     * @param width         Screen width
     * @param screenHeight  Screen height
     */
    public StatusList(int width, int screenHeight) {
        this.screenWidth = width;
        this.screenHeight = screenHeight;
        this.talkBalloon = new TalkBalloon(width, screenHeight);
        nextPageStatus.createTextLines(width, TalkBalloon.textFont);
    }

    /** 
     * Draw status lists 
     * @param g         Graphics.
     * @param statuses  Vector containing status entries.
     * @param row       Row where drawing is started.
     */
    public void draw(
            Graphics g,
            Vector stats,
            int row,
            boolean drawSelectBox,
            boolean drawNextPageLink) {
        if(stats==null) {
            return;
        }
        Enumeration statusEnum = stats.elements();
        
        int currentRow = row;
        selectedStatus = null;
        boolean isSelected;
        int currentStatusIndex = 0;
        while(statusEnum.hasMoreElements()) {
            Status status = (Status)statusEnum.nextElement();
            //System.out.println("Status: " + status.getText());
            int statusHeight = status.getHeight();

            if(status.getTextLines()==null) {
                status.createTextLines(screenWidth-textFont.getHeight()*2-textFont.getHeight()/2, textFont);
            }

            isSelected = false;
            if(currentRow - (textFont.getHeight()+2)>=0 && selectedStatus==null && drawSelectBox==true) {
                selectedStatus = status;
                isSelected = true;
            }

            if( statusHeight==0 ||
               (statusHeight>0 && (currentRow+statusHeight)>0) ) {
                /** Draw status only when it is visible */
                statusHeight = drawStatus(g, currentRow, status, isSelected);
                status.setHeight( statusHeight );
            }
            currentRow += statusHeight;
            if(currentRow>screenHeight) {
                break;
            }
            currentStatusIndex++;
        }

        System.out.println("current status index " + currentStatusIndex);

        /** Draw "Load next page..." link */
        if(drawNextPageLink && currentStatusIndex%20==0) {
            isSelected = false;
            if(currentRow - (textFont.getHeight()+2)>=0 && selectedStatus==null && drawSelectBox==true) {
                selectedStatus = nextPageStatus;
                isSelected = true;
            }
            drawStatus(g, currentRow, nextPageStatus, isSelected);
        }
    }
    
    private int drawStatus(Graphics g, int row, Status status, boolean isSelected) {
        /** Parse the text below the talk balloon */
        String infoText;
        if( status.getDate()!=null ) {
            String time = TimeUtil.getTimeInterval(status.getDate());
            infoText = status.getScreenName() + ", " + time + " ago";
        } else {
            infoText = status.getScreenName();
        }
        return talkBalloon.draw(g, status.getTextLines(), infoText, row, isSelected);
    }

    public Status getSelected() {
        return selectedStatus;
    }

    void setSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        talkBalloon.setSize(width,height);
    }
    
}
