/*
 * SearchTask.java
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
import java.util.Vector;

/**
 *
 * @author tommi
 */
public class SearchTask extends AbstractTask {

    private String query;
    private TwitterApi api;

    public SearchTask(String query, TwitterApi api) {
        this.query = query;
        this.api = api;
    }

    public void doTask() {
        String state = "";
        TwitterController controller = TwitterController.getInstance();
        try {
            state = "searching";
            Vector results = api.search(query);
            state = "showing results";
            controller.showTweets( results, "Results" );
        } catch(Exception ex) {
            controller.showError("Error while " + state + ": " + ex.getMessage());
        }
    }

}
