/*
 * TwitterController.java
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

package com.substanceofcode.twitter;

import com.substanceofcode.infrastructure.Device;
import com.substanceofcode.tasks.AbstractTask;
import com.substanceofcode.twitter.model.MediaFileSelect;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.twitter.model.User;
import com.substanceofcode.twitter.model.UserList;
import com.substanceofcode.twitter.services.RefreshService;
import com.substanceofcode.twitter.tasks.ToggleFavoriteTask;
import com.substanceofcode.twitter.tasks.RequestFriendsTask;
import com.substanceofcode.twitter.tasks.RequestListsTask;
import com.substanceofcode.twitter.tasks.RequestTimelineTask;
import com.substanceofcode.twitter.tasks.SearchTask;
import com.substanceofcode.twitter.tasks.SendPhotoTask;
import com.substanceofcode.twitter.tasks.SendVideoTask;
import com.substanceofcode.twitter.tasks.ToggleFollowingTask;
import com.substanceofcode.twitter.tasks.TranslateTask;
import com.substanceofcode.twitter.tasks.UpdateStatusTask;
import com.substanceofcode.twitter.views.AboutCanvas;
import com.substanceofcode.twitter.views.CameraCanvas;
import com.substanceofcode.twitter.views.FileBrowserCanvas;
import com.substanceofcode.twitter.views.SettingsForm;
import com.substanceofcode.twitter.views.MediaCommentForm;
import com.substanceofcode.twitter.views.MenuAction;
import com.substanceofcode.twitter.views.SearchTextBox;
import com.substanceofcode.twitter.views.SplashCanvas;
import com.substanceofcode.twitter.views.Theme;
import com.substanceofcode.twitter.views.TimelineCanvas;
import com.substanceofcode.twitter.views.UpdateStatusTextBox;
import com.substanceofcode.twitter.views.WaitCanvas;
import com.substanceofcode.twitter.views.menus.ListsMenu;
import com.substanceofcode.twitter.views.menus.OpenListMenuAction;
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
    boolean tweetsShownOnce = false;

    Vector publicTimeline;
    Vector homeTimeline;
    Vector archiveTimeline;
    Vector responsesTimeline;
    Vector directTimeline;
    Vector friendsStatuses;
    Vector favouriteTimeline;
    Vector retweetsOfMeTimeline;

    private static final int PUBLIC_TIMELINE = 0;
    private static final int HOME_TIMELINE = 1;
    private static final int ARCHIVE_TIMELINE = 2;
    private static final int RESPONSES_TIMELINE = 3;
    private static final int DIRECT_TIMELINE = 4;
    private static final int FRIENDS_TIMELINE = 5;
    private static final int FAVOURITE_TIMELINE = 6;
    private static final int SEARCH_TIMELINE = 7;
    private static final int RETWEETS_OF_ME_TIMELINE = 8;
    int currentTimeline;

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

    /** Login to Twitter stream */
    public void login() {
        /** Get saved credentials */
        String username = settings.getStringProperty(Settings.USERNAME, "");
        api.setUsername(username);
        String password = settings.getStringProperty(Settings.PASSWORD, "");
        api.setPassword(password);
        String token = settings.getStringProperty(Settings.TOKEN, "");
        String tokenSecret = settings.getStringProperty(Settings.TOKEN_SECRET, "");
        api.bypassAuthorization(token, tokenSecret);
        boolean loadTweets = settings.getBooleanProperty(Settings.LOAD_ON_STARTUP, false);
        /** Login */
        login(username, password, loadTweets);
    }

    /** 
     * Login to twitter.
     * @param username Username for Twitter
     * @param password Password for Twitter
     */
    public void login(String username, String password, boolean loadTweets) {
        api.setUsername(username);
        api.setPassword(password);

        /** Set saved theme */
        int theme = settings.getIntProperty(Settings.THEME, 0);
        Theme.setTheme(theme);

        /** Start refresh service */
        boolean refresh = settings.getBooleanProperty(Settings.REFRESH, false);
        RefreshService refreshService = RefreshService.getInstance();
        if(refresh) {
            refreshService.activate();
        } else {
            refreshService.deactivate();
        }

        if(loadTweets) {
            api.authorize();
            showHomeTimeline( false );
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
        currentTimeline = DIRECT_TIMELINE;
        timeline.showDrawNextPageLink(false);
        if(directTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_DIRECT, 1);
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

    public void showInfo(String info) {
        Alert alert = new Alert("Info");
        alert.setString(info);
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
        currentTimeline = FRIENDS_TIMELINE;
        timeline.showDrawNextPageLink(false);
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
        currentTimeline = FRIENDS_TIMELINE;
        timeline.showDrawNextPageLink(false);
        String state = "";
        state = "setting friends timeline";
        timeline.setTimeline(friends);
        state = "showing timeline";
        display.setCurrent(timeline);
    }

    public void showPublicTimeline() {
        currentTimeline = PUBLIC_TIMELINE;
        timeline.showDrawNextPageLink(false);
        if(publicTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_PUBLIC, 1);
            WaitCanvas wait = new WaitCanvas(this, task);
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(publicTimeline);
            display.setCurrent(timeline);
        }
    }

    public void showResponsesTimeline() {
        currentTimeline = RESPONSES_TIMELINE;
        timeline.showDrawNextPageLink(false);
        if(responsesTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_RESPONSES, 1);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading responses...");
            display.setCurrent(wait);
        } else {
            timeline.setTimeline(responsesTimeline);
            display.setCurrent(timeline);
        }        
    }

    public void showRetweetsOfMeTimeline(boolean nextPage) {
        currentTimeline = RETWEETS_OF_ME_TIMELINE;
        timeline.showDrawNextPageLink(true);
        if( retweetsOfMeTimeline==null || nextPage) {
            timeline.selectRetweetsOfMeTab(!nextPage);
            int page = 1;
            if(nextPage) {
                if(retweetsOfMeTimeline!=null) {
                    int pages = retweetsOfMeTimeline.size() / 20;
                    page += pages;
                }
            }
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_RETWEETS_OF_ME, page);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading retweets of you...");
            if(display.getCurrent()!=null || tweetsShownOnce==false) {
                display.setCurrent(wait);
            }
        } else {
            timeline.setTimeline( homeTimeline );
            timeline.resetScrolling();
            if(display.getCurrent()!=null || tweetsShownOnce==false) {
                display.setCurrent( timeline );
            }
            tweetsShownOnce = true;
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
    
    public void showArchiveTimeline(boolean showNextAvailablePage) {
        currentTimeline = ARCHIVE_TIMELINE;
        timeline.showDrawNextPageLink(false);
        if(archiveTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_ARCHIVE, 1);
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

    public void showHomeTimeline(boolean nextPage) {
        currentTimeline = HOME_TIMELINE;
        timeline.showDrawNextPageLink(true);
        if( homeTimeline==null || nextPage) {
            timeline.selectHomeTab( !nextPage );
            int page = 1;
            if(nextPage) {
                if(homeTimeline!=null) {
                    int pages = homeTimeline.size() / 20;
                    page += pages;
                }
            }
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_HOME, page);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading your timeline...");
            if(display.getCurrent()!=null || tweetsShownOnce==false) {
                display.setCurrent(wait);
            }
        } else {
            timeline.setTimeline( homeTimeline );
            timeline.resetScrolling();
            if(display.getCurrent()!=null || tweetsShownOnce==false) {
                display.setCurrent( timeline );
            }
            tweetsShownOnce = true;
        }
    }

    public void showFavouriteTimeline() {
        currentTimeline = FAVOURITE_TIMELINE;
        timeline.showDrawNextPageLink(false);
        if( favouriteTimeline==null) {
            RequestTimelineTask task = new RequestTimelineTask(
                this, api, RequestTimelineTask.FEED_FAVOURITE, 1);
            WaitCanvas wait = new WaitCanvas(this, task);
            wait.setWaitText("Loading your timeline...");
            display.setCurrent(wait);
        } else {
            timeline.setTimeline( favouriteTimeline );
            timeline.resetScrolling();
            display.setCurrent( timeline );
        }
    }
    
    public void showTimeline(Vector timelineFeed) {
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
    public void showSplash() {
        SplashCanvas splash = new SplashCanvas(this);
        display.setCurrent(splash);
    }

    /** Show empty timeline view */
    private void showEmptyTimeline() {
        Vector empty = new Vector();
        timeline.showDrawNextPageLink(false);
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
        timeline.showDrawNextPageLink(true);
        currentTimeline = SEARCH_TIMELINE;
        SearchTask task = new SearchTask(query, api);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Searching...");
        display.setCurrent(wait);
    }

    public void searchNextPage() {
        SearchTask task = new SearchTask(api);
        SearchTask.turnPage();
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading next page...");
        display.setCurrent(wait);
    }

    public void showTweets(Vector results, String string) {
        timeline.setTimeline(results);
        timeline.resetScrolling();
        timeline.resetMenuTab();
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

    public void translateToEnglish(Status status) {
        TranslateTask task = new TranslateTask( status );
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Translating...");
        display.setCurrent(wait);
    }

    public void showSingleStatus(Status stat) {
        Vector statuses = new Vector(1);
        statuses.addElement(stat);
        timeline.setTimeline(statuses);
        timeline.resetScrolling();
        display.setCurrent(timeline);
    }

    public void loadNextPage() {
        switch(currentTimeline) {
            case(PUBLIC_TIMELINE):
                break;
            case(HOME_TIMELINE):
                showHomeTimeline(true);
                break;
            case(ARCHIVE_TIMELINE):
                break;
            case(RESPONSES_TIMELINE):
                break;
            case(DIRECT_TIMELINE):
                break;
            case(FRIENDS_TIMELINE):
                break;
            case(FAVOURITE_TIMELINE):
                break;
            case(SEARCH_TIMELINE):
                searchNextPage();
                break;
            default:
                break;
        }
    }

    public void addToHomeTimeline(Vector timeline) {
        addToTimeline(homeTimeline, timeline);
    }

    private void addToTimeline(Vector original, Vector added) {
        if(added!=null) {
            Enumeration en = added.elements();
            while(en.hasMoreElements()) {
                Status stat = (Status)en.nextElement();
                original.addElement( stat );
            }
        }
    }

    public Vector getHomeTimeline() {
        return homeTimeline;
    }

    public void setRetweetsOfMeTimeline(Vector timeline) {
        this.retweetsOfMeTimeline = timeline;
    }

    public Vector getRetweetsOfMeTimeline() {
        return retweetsOfMeTimeline;
    }

    /** Show user lists */
    public void showLists(Vector lists) {
        MenuAction[] actions = new MenuAction[ lists.size() ];
        String[] names = new String[ lists.size() ];
        Enumeration elements = lists.elements();
        int i=0;
        while(elements.hasMoreElements()) {
            UserList list = (UserList)elements.nextElement();
            actions[i] = new OpenListMenuAction(list);
            names[i] = list.getName();
            i++;
        }
        ListsMenu listsMenu = new ListsMenu( names, actions );
        display.setCurrent(listsMenu);
    }

    /** Start loading user lists */
    public void showLists() {
        RequestListsTask task = new RequestListsTask(this, this.api);
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading your lists...");
        display.setCurrent(wait);
    }

    /** Show selected user list content */
    public void showList(UserList userList) {
        RequestTimelineTask task = new RequestTimelineTask(
                this,
                this.api,
                userList.getId());
        WaitCanvas wait = new WaitCanvas(this, task);
        wait.setWaitText("Loading list of " + userList.getName());
        display.setCurrent(wait);
    }

    /** Resets authentication token and secret */
    public void resetAuthenticationToken() {
        api.resetToken();
        showInfo("Authentication token was reset.");
    }

}
