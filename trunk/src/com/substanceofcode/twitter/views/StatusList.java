/*
 * StatusList.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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
import com.substanceofcode.utils.StringUtil;
import com.substanceofcode.utils.TimeUtil;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;

/**
 * StatusList
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class StatusList {
    
    private Font textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private int screenWidth;
    private int screenHeight;
    private TalkBalloon talkBalloon;
    
    
    /** 
     * Creates a new instance of StatusList
     * @param width         Screen width
     * @param screenHeight  Screen height
     */
    public StatusList(int width, int screenHeight) {
        this.screenWidth = width;
        this.screenHeight = screenHeight;
        this.talkBalloon = new TalkBalloon(width, screenHeight);
    }

    /** 
     * Draw status lists 
     * @param g         Graphics.
     * @param statuses  Vector containing status entries.
     * @param row       Row where drawing is started.
     */
    public void draw(Graphics g, Vector statuses, int row) {
        Enumeration statusEnum = statuses.elements();
        
        int currentRow = row;
        while(statusEnum.hasMoreElements()) {
            Status status = (Status)statusEnum.nextElement();
            currentRow += drawStatus(g, currentRow, status);
            currentRow += 4;
            if(currentRow>screenHeight) {
                break;
            }
        }
    }
    
    private int drawStatus(Graphics g, int row, Status status) {
        
        /** Parse the text below the talk balloon */
        String time = TimeUtil.getTimeInterval(status.getDate());
        String infoText = status.getScreenName() + ", " + time + " ago";        
        
        talkBalloon.draw(g, status.getText(), infoText, row);
        
        String[] originalText = { status.getText() };
        String[] textLines = StringUtil.formatMessage(originalText, screenWidth-4, textFont);// .opStrings(status.getText(), "\n", textFont, width-4);
        int height = (textLines.length) * textFont.getHeight();
        /*
        g.setColor(0xffffff);
        g.fillRect(1, row, screenWidth-2, height);
        
        int triSize = textFont.getHeight()/2;
        g.fillTriangle(triSize, row+height, textFont.getHeight(), row+height+triSize, textFont.getHeight(), row+height);
        
        g.setColor(0x000000);
        g.setFont(textFont);
        int textRow = row + textFont.getHeight();
        for(int line=0; line<textLines.length; line++) {
            g.drawString(textLines[line], 2, textRow, Graphics.LEFT|Graphics.BOTTOM);
            textRow += textFont.getHeight();
        }        
        g.drawString(status.getScreenName(), textFont.getHeight() + 2, textRow, Graphics.LEFT|Graphics.BOTTOM);
        //g.drawString(status.getDate(), width-2, textRow, Graphics.RIGHT|Graphics.BOTTOM);
        */
        return height + textFont.getHeight();
         
    }
    
}
