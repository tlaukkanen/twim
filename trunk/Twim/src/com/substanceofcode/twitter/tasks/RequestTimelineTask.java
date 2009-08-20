/*
 * RequestFriendsTimelineTask.java
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

package com.substanceofcode.twitter.tasks;

import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.TwitterApi;
import com.substanceofcode.twitter.TwitterController;
import java.util.Vector;

/**
 * RequestFriendsTimelineTask
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class RequestTimelineTask extends AbstractTask {

    private TwitterController controller;
    private TwitterApi api;
    private int feedType;
    public final static int FEED_HOME = 0;
    public final static int FEED_RESPONSES = 1;
    public final static int FEED_ARCHIVE = 2;
    public final static int FEED_PUBLIC = 3;
    public final static int FEED_DIRECT = 4;
    public final static int FEED_FAVOURITE = 5;
    
    /** 
     * Creates a new instance of RequestFriendsTimelineTask.
     * @param controller 
     * @param api
     * @param feedType 
     */
    public RequestTimelineTask(
            TwitterController controller,
            TwitterApi api,
            int feedType) {
        this.controller = controller;
        this.api = api;
        this.feedType = feedType;
    }

    public void doTask() {
        Vector timeline = null;
        if(feedType==FEED_HOME) {
            timeline = api.requestHomeTimeline();
            controller.setHomeTimeline( timeline );
        } else if(feedType==FEED_ARCHIVE) {
            timeline = api.requestUserTimeline();
            controller.setUserTimeline( timeline );
        } else if(feedType==FEED_RESPONSES) {
            timeline = api.requestResponsesTimeline();
            controller.setResponsesTimeline( timeline );
        } else if(feedType==FEED_PUBLIC) {
            timeline = api.requestPublicTimeline();
            controller.setPublicTimeline( timeline );
        } else if(feedType==FEED_DIRECT) {
            timeline = api.requestDirectTimeline();
            controller.setDirectTimeline(timeline);
        } else if(feedType==FEED_FAVOURITE) {
            timeline = api.requestFavouriteTimeline();
            controller.setFavouriteTimeline(timeline);
        }
        controller.showTimeline( timeline );
    }

}
