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
import com.substanceofcode.twitter.model.Status;
import java.util.Enumeration;
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
    private int page;
    public final static int FEED_HOME = 0;
    public final static int FEED_RESPONSES = 1;
    public final static int FEED_ARCHIVE = 2;
    public final static int FEED_PUBLIC = 3;
    public final static int FEED_DIRECT = 4;
    public final static int FEED_FAVOURITE = 5;

    private static String lastHomeStatusID = "";
    
    /** 
     * Creates a new instance of RequestFriendsTimelineTask.
     * @param controller 
     * @param api
     * @param feedType 
     */
    public RequestTimelineTask(
            TwitterController controller,
            TwitterApi api,
            int feedType,
            int page) {
        this.controller = controller;
        this.api = api;
        this.feedType = feedType;
        this.page = page;
    }

    public void doTask() {
        Vector timeline = null;
        if(feedType==FEED_HOME) {
            timeline = api.requestHomeTimeline( page );
            if(timeline!=null && page==0) {
                Status lastStatus = (Status) timeline.lastElement();
                String newStatusID = lastStatus.getId();
                if(lastHomeStatusID.length()>0 &&
                        !lastHomeStatusID.equals(newStatusID)) {
                    controller.playInfoSound();
                }
                lastHomeStatusID = newStatusID;
            }
            if(page<2) {
                controller.setHomeTimeline( timeline );
            } else {
                Vector homeTimeline = controller.getHomeTimeline();
                timeline = appendToTimeline(homeTimeline, timeline);
                controller.setHomeTimeline( timeline );
            }
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
        boolean resetVerticalScrolling = true;
        if(page>1) {
            resetVerticalScrolling = false;
        }
        controller.showTimeline( timeline );
    }

    private Vector appendToTimeline(Vector original, Vector added) {
        Vector newVector = new Vector();
        Enumeration orig = original.elements();
        while(orig.hasMoreElements()) {
            newVector.addElement(orig.nextElement());
        }

        if(added!=null) {
            Enumeration en = added.elements();
            while(en.hasMoreElements()) {
                Status stat = (Status)en.nextElement();
                newVector.addElement( stat );
            }
        }
        
        return newVector;
    }

}
