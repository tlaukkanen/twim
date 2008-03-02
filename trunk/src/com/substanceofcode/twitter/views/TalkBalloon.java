/*
 * TalkBalloon.java
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

import com.substanceofcode.utils.StringUtil;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi Laukkanen
 */
public class TalkBalloon {
    
    public static final Font textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public int screenWidth;
    public int screenHeight;
    
    /** Create new instanc of TalkBalloon. */
    public TalkBalloon(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    /**
     * Draw talk balloon in given coordinates and in given size.
     * @param g             Graphics.
     * @param text          Text inside balloon.
     * @param talkerText    Text below balloon.
     * @param y             Y coordinate of balloon.
     */
    public int draw(Graphics g, String text, String talkerText, int y) {
        
        // Calculate text dimensions
        String[] originalText = { text };
        String[] textLines = StringUtil.formatMessage(originalText, screenWidth-6, textFont);
        int textHeight = (textLines.length) * textFont.getHeight();
        int textWidth;
        if(textLines.length==1) {
            textWidth = textFont.stringWidth(text);
        } else {
            textWidth = screenWidth-6;
        }
        
        // Draw the main balloon box
        g.setColor(0xffffff);
        int x = screenWidth/2 - (textWidth+2)/2;
        g.fillRect(x, y, textWidth+2, textHeight);
        g.setColor(0x888888);
        g.drawRect(x, y, textWidth+2, textHeight);
   
        // Draw the small triangle on the bottom of the balloon
        g.setColor(0xffffff);
        int triSize = Font.getDefaultFont().getHeight()/2;        
        g.fillTriangle(
            x+triSize, y + textHeight, 
            x+triSize * 2, y + textHeight + triSize, 
            x+triSize * 2, y + textHeight);
        g.setColor(0x888888);
        g.drawLine(x+triSize, y + textHeight, x+triSize * 2, y + textHeight + triSize);
        g.drawLine(x+triSize * 2, y + textHeight + triSize, x+triSize * 2, y + textHeight);        
        
        // Draw text inside balloon
        g.setColor(0x000000);
        g.setFont(textFont);
        int textRow = y + textFont.getHeight();
        for(int line=0; line<textLines.length; line++) {
            g.drawString(textLines[line], x+2, textRow, Graphics.LEFT|Graphics.BOTTOM);
            textRow += textFont.getHeight();
        }      
        
        // Draw talker text
        g.setColor(0x0000aa);
        g.drawString(talkerText, x+triSize * 2 + 2, textRow, Graphics.LEFT|Graphics.BOTTOM);
        
        return (int)((textLines.length+1)*Font.getDefaultFont().getHeight() + 4);
    }
    
}
