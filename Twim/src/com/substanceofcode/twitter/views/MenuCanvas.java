/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
