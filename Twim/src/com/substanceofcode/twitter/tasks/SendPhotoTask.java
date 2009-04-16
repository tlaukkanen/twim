/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitPicApi;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;
import java.io.IOException;

/**
 *
 * @author tommi
 */
public class SendPhotoTask extends AbstractTask {

    byte[] photo;
    String comment;
    String username;
    String password;

    public SendPhotoTask(
            byte[] photo,
            String comment, 
            String username, 
            String password) {
        this.photo = photo;
        this.comment = comment;
        this.username = username;
        this.password = password;
    }

    public void doTask() {
        TwitterController controller = TwitterController.getInstance();
        try {
            Status stat = TwitPicApi.sendPhoto(photo, comment, username, password);
            controller.addStatus(stat);
            controller.showTimeline();
            //controller.showRecentTimeline();
        } catch (Exception ex) {
            controller.showError("Error while posting photo: " + ex.toString());
        }

    }

}
