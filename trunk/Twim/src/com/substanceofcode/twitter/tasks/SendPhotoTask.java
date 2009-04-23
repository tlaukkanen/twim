/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.PhotoService;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;

/**
 *
 * @author tommi
 */
public class SendPhotoTask extends AbstractTask {

    byte[] photo;
    String comment;
    String username;
    String password;
    PhotoService service;

    public SendPhotoTask(
            byte[] photo,
            String comment, 
            String username, 
            String password,
            PhotoService service) {
        this.photo = photo;
        this.comment = comment;
        this.username = username;
        this.password = password;
        this.service = service;
    }

    public void doTask() {
        TwitterController controller = TwitterController.getInstance();
        try {
            Status stat = service.sendPhoto(photo, comment, username, password);
            controller.addStatus(stat);
            controller.showTimeline();
            //controller.showRecentTimeline();
        } catch (Exception ex) {
            controller.showError("Error while posting photo: " + ex.toString());
        }

    }

}
