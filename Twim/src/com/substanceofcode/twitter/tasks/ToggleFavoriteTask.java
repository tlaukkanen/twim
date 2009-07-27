/*
 * ToggleFavoriteTask.java
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
import com.substanceofcode.twitter.TwitterApi;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;

/**
 * Task for marking tweet as favorite.
 * @author Tommi Laukkanen
 */
public class ToggleFavoriteTask extends AbstractTask {

    TwitterController controller;
    TwitterApi api;
    Status status;

    public ToggleFavoriteTask(
            TwitterController controller,
            TwitterApi api,
            Status status) {
        this.controller = controller;
        this.api = api;
        this.status = status;
    }

    public void doTask() {
        try {
            if(status.isFavorite()) {
                Status unfavoriteStatus = api.markAsUnfavorite(status);
                controller.removeFavoriteStatus(unfavoriteStatus);
            } else {
                Status favoriteStatus = api.markAsFavorite(status);
                controller.addFavoriteStatus(favoriteStatus);
            }
            controller.showTimeline();
        } catch(Exception ex) {
            controller.showError("Error while marking tweet as favorite: " + ex.getMessage());
        }
        
    }

}
