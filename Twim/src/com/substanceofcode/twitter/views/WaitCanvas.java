/*
 * WaitCanvas.java
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

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.utils.ImageUtil;
import com.substanceofcode.utils.Log;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 *
 * @author Tommi
 */
public class WaitCanvas extends Canvas implements Runnable {
    
    private TwitterController controller;
    private String waitText = "Please wait...";
    private Displayable nextScreen;
    private AbstractTask task;
    private Thread thread;
    private Font statusFont;
    private int loadingImageIndex;
    private TalkBalloon talkBalloon;
    
    private final Font titleFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
     
        
    public void setWaitText(String text) {
        waitText = text;
    }
    
    /** Creates a new instance of WaitCanvas 
     * @param controller    Application controller.
     * @param task          Task to be executed.
     */
    public WaitCanvas(TwitterController controller, AbstractTask task) {
        this.setFullScreenMode(true);
        this.controller = controller;
        this.waitText = "Please wait...";
        this.task = task;
        this.talkBalloon = new ComicTalkBalloon(getWidth(), getHeight());
        loadingImageIndex = 0;
        statusFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        thread = new Thread(this);
        thread.start();           
    }
    
    protected void paint(Graphics g) {  
        // Clear the background to white
        g.setColor( Theme.COLOR_BACKGROUND );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        int titleY = getHeight()/4;
        talkBalloon.draw(g, waitText, "Twim", titleY);
    }

    public void run() {
        task.execute(); 
        while(controller.getCurrentDisplay() == this) {            
            try {
                Thread.sleep(500);
                waitText += ".";
                this.repaint();
                Thread.yield();
            }catch(Exception ex) {
                Log.error("WaitCanvas.run: " + ex.getMessage());
            }
        }
    }
    
}
