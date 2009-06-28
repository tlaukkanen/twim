/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.VideoService;
import com.substanceofcode.twitter.model.Status;

/**
 *
 * @author tommi
 */
public class SendVideoTask extends AbstractTask {

    byte[] video;
    String comment;
    String username;
    String password;
    VideoService service;
    String filename;

    public SendVideoTask(
            byte[] photo,
            String comment,
            String username,
            String password,
            VideoService service,
            String filename) {
        this.video = photo;
        this.comment = comment;
        this.username = username;
        this.password = password;
        this.service = service;
        this.filename = filename;
    }

    public void doTask() {
        TwitterController controller = TwitterController.getInstance();
        try {
            Status stat = service.sendVideo(video, comment, username, password, filename);
            controller.addStatus(stat);
            controller.showTimeline();
            //controller.showRecentTimeline();
        } catch (Exception ex) {
            controller.showError("Error while posting photo: " + ex.toString());
        }

    }

}
