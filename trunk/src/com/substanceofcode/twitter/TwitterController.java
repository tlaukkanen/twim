/*
 * TwitterController.java
 *
 * Copyright (C) 2005-2007 Tommi Laukkanen
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

package com.substanceofcode.twitter;

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.twitter.tasks.RequestTimelineTask;
import com.substanceofcode.twitter.tasks.UpdateStatusTask;
import com.substanceofcode.twitter.views.LoginForm;
import com.substanceofcode.twitter.views.SplashCanvas;
import com.substanceofcode.twitter.views.TimelineCanvas;
import com.substanceofcode.twitter.views.UpdateStatusTextBox;
import com.substanceofcode.twitter.views.WaitCanvas;
import com.substanceofcode.utils.Log;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.rms.RecordStoreException;

/**
 * TwitterController controls the application flow.
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TwitterController {

    private TwitterMidlet midlet;
    private Display display;
    private TwitterApi api;
    private Settings settings;
    private TimelineCanvas timeline;
    
    private Vector friendsTimeline;
    private Vector archiveTimeline;
    private Vector responsesTimeline;
    
    /** 
     * Creates a new instance of TwitterController
     * @param midlet Application midlet.
     */
    public TwitterController(TwitterMidlet midlet) {
        try {
            this.midlet = midlet;
            this.display = Display.getDisplay(midlet);
            this.api = new TwitterApi();
            this.timeline = new TimelineCanvas(this);
            settings = Settings.getInstance(midlet);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }

    public void addStatus(Status status) {
        if(friendsTimeline!=null) {
            friendsTimeline.insertElementAt(status, 0);
        }
        if(archiveTimeline!=null) {
            archiveTimeline.insertElementAt(status, 0);
        }
    }
    
    public Settings getSettings() {
        return settings;
    }

    public void exit() {
        try {
            midlet.destroyApp(true);
            midlet.notifyDestroyed();
        } catch(Exception ex) {
            Log.error("Exit: " + ex.getMessage());
        }
    }

    public Displayable getCurrentDisplay() {
        return display.getCurrent();
    }

    /** 
     * Login to twitter.
     * @param username Username for Twitter
     * @param password Password for Twitter
     */
    public void login(String username, String password) {
        api.setUsername(username);
        api.setPassword(password);
        RequestTimelineTask task = new RequestTimelineTask(
            this, api, RequestTimelineTask.FEED_FRIENDS);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading your timeline...");
        display.setCurrent(wait);
    }

    public void setResponsesTimeline(Vector responsesTimeline) {
        this.responsesTimeline = responsesTimeline;
    }

    public void setUserTimeline(Vector archiveTimeline) {
        this.archiveTimeline = archiveTimeline;
    }

    public void showResponsesTimeline() {
        if(responsesTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_RESPONSES);
            WaitCanvas wait = new WaitCanvas(this, task);
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(responsesTimeline);
            display.setCurrent(timeline);
        }        
    }

    /** Show status updating view. */
    public void showStatusView() {
        UpdateStatusTextBox statusView = new UpdateStatusTextBox(this);
        display.setCurrent(statusView);
    }

    /** 
     * Update Twitter status.
     * @param status    New status
     */
    public void updateStatus(String status) {
        UpdateStatusTask task = new UpdateStatusTask( this, api, status );
        WaitCanvas wait = new WaitCanvas(this, task);
        display.setCurrent(wait);
    }
    
    public void useArchiveTimeline() {
        timeline.setTimeline(archiveTimeline);
    }
    
    public void useResponsesTimeline() {
        timeline.setTimeline(responsesTimeline);
    }
    
    public void showArchiveTimeline() {
        if(archiveTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_ARCHIVE);
            WaitCanvas wait = new WaitCanvas(this, task);
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(archiveTimeline);
            display.setCurrent(timeline);
        }
    }

    /** 
     * Set friends time line entries.
     * @param friendsTimeline 
     */
    public void setFriendsTimeline(Vector friendsTimeline) {
        this.friendsTimeline = friendsTimeline;
    }
    
    /** Show login form */
    public void showLoginForm() {
        LoginForm loginForm = new LoginForm( this );
        display.setCurrent( loginForm );
    }

    public void showFriendsTimeline() {        
        timeline.setTimeline( friendsTimeline );
        display.setCurrent( timeline );
    }    
    
    public void showTimeline(Vector timelineFeed ) {        
        timeline.setTimeline( timelineFeed );
        display.setCurrent( timeline );
    }

    /** Show splash screen */
    void showSplash() {
        SplashCanvas splash = new SplashCanvas(this);
        display.setCurrent(splash);
    }

}
