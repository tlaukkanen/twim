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

    public MenuCanvas(
            String title,
            String[] labels,
            MenuAction[] actions) {
        menu = new Menu(labels, actions, getWidth(), getHeight());
        menu.setTitle( title );
        menu.activate();
        this.setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0x000000);
        menu.draw(g);
    }

    protected void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        int gameAction = this.getGameAction(keyCode);
        if(gameAction==Canvas.UP) {
            menu.selectPrevious();
            repaint();
        } else if(gameAction==Canvas.DOWN) {
            menu.selectNext();
            repaint();
        } else if(gameAction==Canvas.FIRE) {
            menu.activateSelected();
        }
    }
    
}
