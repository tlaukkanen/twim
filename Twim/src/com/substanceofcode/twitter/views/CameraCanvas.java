/*
 * CameraCanvas.java
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

import com.substanceofcode.twitter.TwitterController;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

/**
 * CameraCanvas for taking photos.
 * @author Tommi Laukkanen
 */
public class CameraCanvas extends Canvas {

    TwitterController controller;
    Player player;
    VideoControl videoControl;
    int width = getWidth();
    int height = getHeight();
    String err;
    Image snapImage;

    public CameraCanvas() {
        try {
            err = "";
            setFullScreenMode(true);
            controller = TwitterController.getInstance();
            player = Manager.createPlayer("capture://video");
            player.realize();
            
            videoControl = (VideoControl) player.getControl("VideoControl");
            videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            videoControl.setDisplayLocation(0, 0);
            videoControl.setDisplaySize(width, height);
            videoControl.setDisplayFullScreen(true);
            videoControl.setVisible(true);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            err = ex.toString();
        }
    }

    public void start() {
        try {
            player.start();
        } catch (MediaException ex) {
            ex.printStackTrace();
        }
    }

    protected void paint(Graphics g) {

        g.setColor( Theme.TWITTER_BLUE_COLOR );
        g.fillRect(0, 0, getWidth(), getHeight());
        if(err.length()>0) {
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            g.drawString(err, 0, 20, Graphics.LEFT|Graphics.BASELINE);
        }
        if(snapImage!=null) {
            g.drawImage(snapImage, 5, 5, Graphics.LEFT|Graphics.TOP);
        }
    }

    protected void keyPressed(int keyCode) {
        boolean exit = false;
        try {
            int gameAction = this.getGameAction(keyCode);
            String keyName = this.getKeyName(keyCode);
            if (gameAction == Canvas.FIRE) {
                // Take photo
                exit = true;
                byte[] image = videoControl.getSnapshot(null);
                controller.showMediaService(image, true, "photo.jpg");
                return;
            } else if( keyName.indexOf("SOFT")>=0) {
                // Cancel
                exit = true;
                controller.showTimeline();
                return;
            }
        } catch (MediaException ex) {
            ex.printStackTrace();
        } finally {
            if(player!=null && exit==true) {
                try {
                    player.stop();
                } catch (MediaException ex) {
                    ex.printStackTrace();
                }
                player.deallocate();
                player.close();
            }
        }
    }



}
