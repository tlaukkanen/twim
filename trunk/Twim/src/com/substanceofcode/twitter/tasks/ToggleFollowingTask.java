/*
 * ToggleFollowingTask.java
 *
 * Copyright (C) 2005-2010 Tommi Laukkanen
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
import com.substanceofcode.twitter.TwitterApi;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;

/**
 * Task for marking tweet as favorite.
 * @author Tommi Laukkanen
 */
public class ToggleFollowingTask extends AbstractTask {

    TwitterController controller;
    TwitterApi api;
    Status status;

    public ToggleFollowingTask(
            TwitterController controller,
            TwitterApi api,
            Status status) {
        this.controller = controller;
        this.api = api;
        this.status = status;
    }

    public void doTask() {
        try {
            if(status.isFollowing()) {
                String result = api.unfollowUser(status);
				status.setFollowing(false);
                //controller.showError(result);
            } else {
                String result = api.followUser(status);
				status.setFollowing(true);
                //controller.showError(result);
            }
            controller.showTimeline();
        } catch(Exception ex) {
            controller.showError("Error while toggling following of user: " + ex.getMessage());
        }
        
    }

}
