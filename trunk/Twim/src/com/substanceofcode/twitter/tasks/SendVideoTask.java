/*
 * SendVideoTask.java
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
            Status stat = service.sendVideo(
                    video,
                    comment,
                    username,
                    password,
                    filename);
            controller.addStatus(stat);
            controller.showTimeline();
            //controller.showHomeTimeline();
        } catch (Exception ex) {
            controller.showError("Error while posting video: " + ex.toString());
        }

    }

}
