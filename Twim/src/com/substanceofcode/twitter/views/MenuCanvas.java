/*
 * MenuCanvas.java
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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author tommi
 */
public abstract class MenuCanvas extends Canvas {

    Menu menu;
    int screenWidth;

    public MenuCanvas(
            String title,
            String[] labels,
            MenuAction[] actions) {
        menu = new Menu(labels, actions, getWidth(), getHeight());
        menu.setTitle( title );
        menu.activate();
        screenWidth = getWidth();
        this.setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        /** Check for screen rotation change */
        if(screenWidth != getWidth()) {
            menu.setSize(getWidth(), getHeight());
            screenWidth = getWidth();
        }

        /** Draw menu */
        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0x000000);
        menu.draw(g);
    }

    protected void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int gameAction = this.getGameAction(keyCode);
        String keyName = this.getKeyName(keyCode);
        if(gameAction==Canvas.UP) {
            menu.selectPrevious();
            repaint();
        } else if(gameAction==Canvas.DOWN) {
            menu.selectNext();
            repaint();
        } else if(gameAction==Canvas.FIRE || 
                keyName.toUpperCase().startsWith("ENTER")) {
            menu.activateSelected();
        }
    }

    /**
     * Handle touch screen press
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerPressed(int x, int y) {
        menu.selectWithPointer(x, y, true);
        repaint();
    }

    /**
     * Handle touch screen drag
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerDragged(int x, int y) {
        menu.selectWithPointer(x, y, false);
        repaint();
    }

    /**
     * Handle touch screen release
     * @param x coordinate
     * @param y coordinate
     */
    protected void pointerReleased(int x, int y) {
        menu.selectWithPointer(x, y, false);
        menu.activateSelected();
    }







}
