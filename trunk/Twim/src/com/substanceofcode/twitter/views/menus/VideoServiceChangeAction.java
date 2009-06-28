/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.VideoService;
import com.substanceofcode.twitter.views.MenuAction;

/**
 *
 * @author tommi
 */
public class VideoServiceChangeAction implements MenuAction {

    VideoService service;
    byte[] video;
    String filename;

    public VideoServiceChangeAction(VideoService service, byte[] video, String filename) {
        this.service = service;
        this.video = video;
        this.filename = filename;
    }


    public void activate() {
        TwitterController.getInstance().setVideoService( service );
        TwitterController.getInstance().commentMedia(video, filename);
    }

}
