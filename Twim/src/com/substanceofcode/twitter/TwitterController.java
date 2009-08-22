/*
 * TwitterController.java
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

package com.substanceofcode.twitter;

import com.substanceofcode.infrastructure.Device;
import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.model.MediaFileSelect;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.twitter.model.User;
import com.substanceofcode.twitter.services.RefreshService;
import com.substanceofcode.twitter.tasks.ToggleFavoriteTask;
import com.substanceofcode.twitter.tasks.RequestFriendsTask;
import com.substanceofcode.twitter.tasks.RequestTimelineTask;
import com.substanceofcode.twitter.tasks.SearchTask;
import com.substanceofcode.twitter.tasks.SendPhotoTask;
import com.substanceofcode.twitter.tasks.SendVideoTask;
import com.substanceofcode.twitter.tasks.ToggleFollowingTask;
import com.substanceofcode.twitter.tasks.UpdateStatusTask;
import com.substanceofcode.twitter.views.AboutCanvas;
import com.substanceofcode.twitter.views.CameraCanvas;
import com.substanceofcode.twitter.views.FileBrowserCanvas;
import com.substanceofcode.twitter.views.SettingsForm;
import com.substanceofcode.twitter.views.MediaCommentForm;
import com.substanceofcode.twitter.views.SearchTextBox;
import com.substanceofcode.twitter.views.SplashCanvas;
import com.substanceofcode.twitter.views.TimelineCanvas;
import com.substanceofcode.twitter.views.UpdateStatusTextBox;
import com.substanceofcode.twitter.views.WaitCanvas;
import com.substanceofcode.twitter.views.menus.PhotoServicesMenu;
import com.substanceofcode.twitter.views.menus.VideoServicesMenu;
import com.substanceofcode.utils.Log;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

/**
 * TwitterController controls the application flow.
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TwitterController {

    TwitterMidlet midlet;
    Display display;
    TwitterApi api;
    Settings settings;
    TimelineCanvas timeline;
    PhotoService activePhotoService;
    VideoService activeVideoService;
    FileBrowserCanvas fileBrowser;

    Vector publicTimeline;
    Vector homeTimeline;
    Vector archiveTimeline;
    Vector responsesTimeline;
    Vector directTimeline;
    Vector friendsStatuses;
    Vector favouriteTimeline;

    static TwitterController instance;

    public static TwitterController getInstance(TwitterMidlet midlet) {
        if(instance==null) {
            instance = new TwitterController(midlet);
        }
        return instance;
    }

    public static TwitterController getInstance() {
        return instance;
    }

    /** 
     * Creates a new instance of TwitterController
     * @param midlet Application midlet.
     */
    private TwitterController(TwitterMidlet midlet) {
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

    public void about() {
        AboutCanvas canvas = new AboutCanvas(this);
        display.setCurrent(canvas);
    }

    public void addFavoriteStatus(Status favoriteStatus) {
        if(favouriteTimeline!=null) {
            favouriteTimeline.addElement(favoriteStatus);
        }
    }

    public void addStatus(Status status) {
        if(homeTimeline!=null) {
            homeTimeline.insertElementAt(status, 0);
        }
        if(archiveTimeline!=null) {
            archiveTimeline.insertElementAt(status, 0);
        }
    }

    public void clearTimelines() {
        setHomeTimeline(null);
        setPublicTimeline(null);
        setResponsesTimeline(null);
        setUserTimeline(null);
        setDirectTimeline(null);
        setFriendsStatuses(null);
        setFavouriteTimeline(null);
    }

    public MIDlet getMIDlet() {
        return midlet;
    }
    
    public Settings getSettings() {
        return settings;
    }

    public void exit() {
        try {
            midlet.quit();
        } catch(Exception ex) {
            Log.error("Exit: " + ex.toString());
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
    public void login(String username, String password, boolean loadTweets) {
        api.setUsername(username);
        api.setPassword(password);

        /** Start refresh service */
        boolean refresh = settings.getBooleanProperty(Settings.REFRESH, false);
        RefreshService refreshService = RefreshService.getInstance();
        if(refresh) {
            refreshService.activate();
        } else {
            refreshService.deactivate();
        }

        if(loadTweets) {
            showHomeTimeline();
        } else {
            showEmptyTimeline();
        }
    }

    public void commentMedia(byte[] media, String filename) {
        MediaCommentForm commentForm = new MediaCommentForm(media, filename);
        display.setCurrent(commentForm);
    }

    public void minimize() {
        display.setCurrent(timeline);
        display.setCurrent(null);
    }

    public void setPhotoService(PhotoService service) {
        this.activePhotoService = service;
        this.activeVideoService = null;
    }

    public void setVideoService(VideoService service) {
        this.activeVideoService = service;
        this.activePhotoService = null;
    }

    public void showMediaService(byte[] mediaData, boolean isPhoto, String filename) {
        if(isPhoto) {
            PhotoServicesMenu servicesMenu = new PhotoServicesMenu(mediaData, filename);
            display.setCurrent(servicesMenu);
        } else {
            VideoServicesMenu servicesMenu = new VideoServicesMenu(mediaData, filename);
            display.setCurrent(servicesMenu);
        }
    }

    public void showVideoBrowser() {
        if(fileBrowser==null) {
            fileBrowser = new FileBrowserCanvas(new MediaFileSelect(false));
            fileBrowser.showRoots();
        }
        fileBrowser.resetToLastFolder();
        display.setCurrent(fileBrowser);
    }

    public void toggleFavorite(Status selectedStatus) {
        ToggleFavoriteTask task = new ToggleFavoriteTask(this, api, selectedStatus);
        WaitCanvas wait = new WaitCanvas(this, task);
        if(selectedStatus.isFavorite()) {
            wait.setWaitText("Unfavorite status...");
        } else {
            wait.setWaitText("Marking as favorite...");
        }
        display.setCurrent(wait);
    }

    public void sendMedia(String comment, String filename, byte[] media) {
        String username = api.getUsername();
        String password = api.getPassword();
        AbstractTask task = null;
        if(activePhotoService!=null) {
            task = new SendPhotoTask(media, comment, username, password, activePhotoService, filename);
        } else {
            task = new SendVideoTask(media, comment, username, password, activeVideoService, filename);
        }
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Sending...");
        display.setCurrent(wait);
    }

    public void setPublicTimeline(Vector publicTimeline) {
        this.publicTimeline = publicTimeline;
    }

    public void setResponsesTimeline(Vector responsesTimeline) {
        this.responsesTimeline = responsesTimeline;
    }

    public void setUserTimeline(Vector archiveTimeline) {
        this.archiveTimeline = archiveTimeline;
    }

    public void setDirectTimeline(Vector directTimeline) {
        this.directTimeline = directTimeline;
    }

    public void setFriendsStatuses(Vector friendStatuses) {
        this.friendsStatuses = friendStatuses;
    }

    public void showCamera() {
        CameraCanvas camCanvas = new CameraCanvas();
        display.setCurrent(camCanvas);
        camCanvas.start();
    }

    public void showDirectMessages() {
        if(directTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_DIRECT);
            WaitCanvas wait = new WaitCanvas(this, task);
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(directTimeline);
            display.setCurrent(timeline);
        }
    }

    public void showError(String string) {
        Alert alert = new Alert("Error");
        alert.setString(string);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, timeline);
    }

    public void showPhotoBrowser() {
        try {
            if(fileBrowser==null) {
                fileBrowser = new FileBrowserCanvas(new MediaFileSelect(true));
                fileBrowser.showRoots();
            }
            fileBrowser.resetToLastFolder();
            display.setCurrent(fileBrowser);
        } catch(Exception ex) {
            showError("Can't show photo browser due to an error. Your phone probably doesn't support File Connection API calls. Err: " + ex.getMessage() );
        }
    }

    /** Show friends */
    public void showFriends() {
        if(friendsStatuses==null) {
            RequestFriendsTask task = new RequestFriendsTask(this, api);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading friends");
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(friendsStatuses);
            display.setCurrent(timeline);
        }
    }

    /** Show friends */
    public void showFriends(Vector friends) {
        String state = "";
        int nullUserCount = 0; // Only for debugging purposes
        try {
            if(friends==null) {
                showError("Friends vector is null");
                return;
            }
            state = "initializing vector";
            friendsStatuses = new Vector();
            state = "creating enumeration";
            Enumeration friendEnum = friends.elements();
            state = "starting the loop friends";
            while(friendEnum.hasMoreElements()) {
                state = "getting user from element";
                User user = (User) friendEnum.nextElement();
                if(user==null) {
                    // why?
                    nullUserCount++;
                }
                state = "getting user's last status";
                if(user.getLastStatus()!=null) {
                    state = "adding last status to vector";
                    friendsStatuses.addElement(user.getLastStatus());
                }
            }
            state = "setting friends timeline";
            timeline.setTimeline(friendsStatuses);
            state = "showing timeline";
            display.setCurrent(timeline);
        } catch(Exception ex) {
            this.showError("Error while " + state + ": " + ex.getMessage()
                    + "\nNull users: " + nullUserCount
                    + "\nFriends: " + friends.capacity());
        }
    }

    public void showPublicTimeline() {
        if(publicTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_PUBLIC);
            WaitCanvas wait = new WaitCanvas(this, task);
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(publicTimeline);
            display.setCurrent(timeline);
        }
    }

    public void showResponsesTimeline() {
        if(responsesTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_RESPONSES);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading responses...");
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(responsesTimeline);
            display.setCurrent(timeline);
        }        
    }

    /** Show status updating view. */
    public void showStatusView(String prefix) {
        UpdateStatusTextBox statusView = new UpdateStatusTextBox(this, prefix);
        display.setCurrent(statusView);
    }

    public void toggleFollow(Status status) {
        ToggleFollowingTask task = new ToggleFollowingTask(this, api, status);
        WaitCanvas wait = new WaitCanvas(this, task);

        String waitText = "";
        if(status.isFollowing()) {
            waitText = "Unfollow";
        } else {
            waitText = "Follow";
        }
        wait.setWaitText(waitText + " " + status.getScreenName() + "...");
        display.setCurrent(wait);
    }

    /** 
     * Update Twitter status.
     * @param status    New status
     */
    public void updateStatus(String status) {
        UpdateStatusTask task = new UpdateStatusTask( this, api, status );
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Updating status...");
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
            wait.setWaitText("Loading tweets...");
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
    public void setHomeTimeline(Vector timeline) {
        this.homeTimeline = timeline;
    }

    public void setFavouriteTimeline(Vector timeline) {
        this.favouriteTimeline = timeline;
    }
    
    /** Show login form */
    public void showSettingsForm() {
        SettingsForm settingsForm = new SettingsForm( this );
        display.setCurrent( settingsForm );
    }

    /**
     * Check if application is minimized
     * @return true if minimized
     */
    public boolean isMinimized() {
        if(display.getCurrent()==null) {
            return true;
        } else {
            return false;
        }
    }

    public void showHomeTimeline() {
        if( homeTimeline==null) {
            timeline.selectHomeTab();
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_HOME);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading your timeline...");
            if(display.getCurrent()!=null) {
                display.setCurrent(wait);
            }
        } else {
            timeline.setTimeline( homeTimeline );
            timeline.resetScrolling();
            if(display.getCurrent()!=null) {
                display.setCurrent( timeline );
            }
        }
    }

    public void showFavouriteTimeline() {
        if( favouriteTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_FAVOURITE);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading your timeline...");
            display.setCurrent(wait);
        } else {
            timeline.setTimeline( favouriteTimeline );
            timeline.resetScrolling();
            display.setCurrent( timeline );
        }
    }
    
    public void showTimeline(Vector timelineFeed ) {
        if(timelineFeed==null || timelineFeed.isEmpty()) {
            showError("No statuses to display");
        } else {
            timeline.setTimeline( timelineFeed );
            /** Don't show if we are in minimized mode */
            if(display.getCurrent()!=null) {
                display.setCurrent( timeline );
            }
        }
    }

    /** Show current timeline */
    public void showTimeline() {
        timeline.resetScrolling();
        timeline.resetMenus();
        display.setCurrent(timeline);
    }

    /** Show splash screen */
    void showSplash() {
        SplashCanvas splash = new SplashCanvas(this);
        display.setCurrent(splash);
    }

    /** Show empty timeline view */
    private void showEmptyTimeline() {
        Vector empty = new Vector();
        empty.addElement(
                new Status(
                "Twim", "Select what tweets you'd like to see",
                Calendar.getInstance().getTime(),
                "0")
            );
        if(Device.isTouch()) {
            timeline.resetMenuTab();
        }
        timeline.setTimeline(empty);
        display.setCurrent(timeline);
    }

    /**
     * Remove specified status from favorite timeline
     * @param unfavoriteStatus Status that have been unfavorited.
     */
    public void removeFavoriteStatus(Status unfavoriteStatus) {
        Enumeration enu = favouriteTimeline.elements();
        while(enu.hasMoreElements()) {
            Status stat = (Status) enu.nextElement();
            if(stat.getId().equals(unfavoriteStatus.getId())) {
                favouriteTimeline.removeElement(stat);
            }
        }
    }

    public void showSearchForm() {
        SearchTextBox searchBox = new SearchTextBox();
        display.setCurrent(searchBox);
    }

    public void search(String query) {
        SearchTask task = new SearchTask(query, api);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Searching...");
        display.setCurrent(wait);
    }

    public void showTweets(Vector results, String string) {
        timeline.setTimeline(results);
        timeline.resetScrolling();
        timeline.resetMenuTab();
        //timeline.
        display.setCurrent(timeline);
    }

    public void showPreviousTimeline() {
        timeline.resetMenus();
        display.setCurrent(timeline);
    }

    /**
     * Get recent status items.
     * @return recent status items in vector
     */
    public Vector getRecentStatuses() {
        return homeTimeline;
    }

    public void vibrate(int milliSeconds) {
        display.vibrate(milliSeconds);
    }

    /**
     * Can we make an auto-refresh?
     * @return true if we can
     */
    public boolean canAutorefresh() {
        boolean timelineIsShown = (display.getCurrent()==timeline);
        if(timelineIsShown || isMinimized()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Play info sound.
     */
    public void playInfoSound() {
        display.vibrate(500);
        AlertType.INFO.playSound(display);
    }

}
