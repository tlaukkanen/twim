/*
 * ComicTalkBalloon.java
 *
 * Copyright (C) 2005-2010 Tommi Laukkanen
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
public class ComicTalkBalloon implements TalkBalloon {

    public static final Font textFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public int screenWidth;
    public int screenHeight;
    private static final int BORDER_COLOR = 0xAAAAAA;
    private int fontHeight;
    private int textWidth;

    /** Create new instanc of TalkBalloon. */
    public ComicTalkBalloon(int screenWidth, int screenHeight) {
        this.fontHeight = textFont.getHeight();
        setSize(screenWidth, screenHeight);
    }

    public Font getFont() {
        return textFont;
    }

    public int draw(Graphics g, String text, String talkerText, int y) {
        String[] originalText = { text };
        String[] textLines = StringUtil.formatMessage(originalText, screenWidth-textFont.getHeight()*2-textFont.getHeight()/2, textFont);
        return draw(g, textLines, talkerText, y, false);
    }

    public int draw(Graphics g, String text, String talkerText, int y, boolean isSelected) {
        String[] originalText = { text };
        String[] textLines = StringUtil.formatMessage(originalText, screenWidth-textFont.getHeight()*2-textFont.getHeight()/2, textFont);
        return draw(g, textLines, talkerText, y, isSelected);
    }

    /**
     * Draw talk balloon in given coordinates and in given size.
     * @param g             Graphics.
     * @param text          Text inside balloon.
     * @param talkerText    Text below balloon.
     * @param y             Y coordinate of balloon.
     */
    public int draw(Graphics g, String[] textLines, String talkerText, int y, boolean isSelected) {

        // Calculate text dimensions
        int textHeight = (textLines.length) * fontHeight + fontHeight;

        // Draw the main balloon box
        if(!isSelected) {
            g.setColor( Theme.COLOR_TEXT_BG );
        } else {
            g.setColor( Theme.COLOR_SELECTED_BG );
        }
        int x = screenWidth/2 - (textWidth+fontHeight)/2;
        g.fillRect(x, y, textWidth + fontHeight, textHeight);

        if(isSelected) {
            g.setColor(0x0000CC);
            g.drawRect(x-0, y-0, textWidth + fontHeight + 0, textHeight + 0);
            g.drawRect(x-1, y-1, textWidth + fontHeight + 2, textHeight + 2);
            /*int size = (fontHeight)/2;
            g.fillTriangle(
                x-size, y + textHeight/2 - size/2,
                x,      y + textHeight/2,
                x-size, y + textHeight/2 + size/2);
             */
        } else {
            g.setColor(BORDER_COLOR);
            g.drawRect(x, y, textWidth + fontHeight, textHeight);
        }

        // Draw the small triangle on the bottom of the balloon
        if(!isSelected) {
            g.setColor( Theme.COLOR_TEXT_BG );
        } else {
            g.setColor( Theme.COLOR_SELECTED_BG );
        }
        int triSize = fontHeight/2;
        g.fillTriangle(
            x+16, y + textHeight,
            x+16+triSize, y + textHeight + triSize,
            x+16+triSize * 2, y + textHeight);
        g.setColor(BORDER_COLOR);
        g.drawLine(x+16, y + textHeight, x+16+triSize, y + textHeight + triSize);
        g.drawLine(x+16+triSize, y + textHeight + triSize, x+16+triSize * 2, y + textHeight);

        // Draw text inside balloon
        g.setColor( Theme.COLOR_TEXT );
        g.setFont(textFont);
        int textRow = y + fontHeight + fontHeight/2;
        for(int line=0; line<textLines.length; line++) {
            g.drawString(textLines[line], x+fontHeight/2, textRow, Graphics.LEFT|Graphics.BOTTOM);
            textRow += fontHeight;
        }

        // Draw talker text
        g.drawImage(Images.getUser(), x, y + textHeight+4, Graphics.LEFT|Graphics.TOP);

        g.setColor( Theme.COLOR_USER_TEXT );
        g.drawString(talkerText, x+16+triSize * 2 + 2, y + textHeight + 4 + fontHeight, Graphics.LEFT|Graphics.BOTTOM);

        int titleHeight = fontHeight + 1 ;
        if(titleHeight<23) { titleHeight = 23; }

        return (int)((textLines.length)*fontHeight + fontHeight + titleHeight);
    }

    public void setSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.textWidth = screenWidth-fontHeight*3;
    }
}
