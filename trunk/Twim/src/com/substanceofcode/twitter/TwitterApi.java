/*
 * TwitterApi.java
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

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.StringUtil;
import com.substanceofcode.utils.URLUTF8Encoder;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * TwitterApi
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TwitterApi {

    private String username;
    private String password;
    private static final String PUBLIC_TIMELINE_URL = "http://www.twitter.com/statuses/public_timeline.xml";
    private static final String FRIENDS_TIMELINE_URL = "http://www.twitter.com/statuses/friends_timeline.xml";
    private static final String USER_TIMELINE_URL = "http://www.twitter.com/statuses/user_timeline.xml";
    private static final String RESPONSES_TIMELINE_URL = "http://twitter.com/statuses/replies.xml";
    private static final String STATUS_UPDATE_URL = "http://twitter.com/statuses/update.xml";
    private static final String DIRECT_TIMELINE_URL = "http://twitter.com/direct_messages.xml";
    private static final String FRIENDS_URL = "http://twitter.com/statuses/friends.xml";
    private static final String FAVORITE_TIMELINE_URL = "http://twitter.com/favorites.xml";
    private static final String FAVORITE_CREATE_URL = "http://twitter.com/favorites/create/";
    private static final String FAVORITE_DESTROY_URL = "http://twitter.com/favorites/destroy/";
    private static final String FRIENDSHIPS_CREATE_URL = "http://twitter.com/friendships/create/";
    private static final String FRIENDSHIPS_DESTROY_URL = "http://twitter.com/friendships/destroy/";
    private static final String SEARCH_URL = "http://search.twitter.com/search.atom?q=";

    /** Creates a new instance of TwitterApi */
    public TwitterApi() {
    }

    public String followUser(Status status) throws Exception {
        try {
            NullParser parser = new NullParser();
            String url = FRIENDSHIPS_CREATE_URL + status.getScreenName() + ".xml";
            HttpUtil.doPost( url, parser );
            return parser.getResponse();
        } catch(Exception ex) {
            throw ex;
        }
    }

    public String unfollowUser(Status status) throws Exception {
        try {
            NullParser parser = new NullParser();
            String url = FRIENDSHIPS_DESTROY_URL + status.getScreenName() + ".xml";
            HttpUtil.doPost( url, parser );
            return parser.getResponse();
        } catch(Exception ex) {
            throw ex;
        }
    }

    public Status markAsFavorite(Status status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = FAVORITE_CREATE_URL + status.getId() + ".xml";
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            return new Status(
                    "Twim",
                    "Error while marking status as favorite: " + ex.getMessage(),
                    Calendar.getInstance().getTime(),
                    "0");
        }
        return null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Request direct messages from Twitter API
     * @return Vector containing direct messages.
     */
    public Vector requestDirectTimeline() {
        return requestTimeline( DIRECT_TIMELINE_URL );
    }

    /**
     * Request favourite tweets from Twitter API.
     * @return Vector containing favourite tweets.
     */
    public Vector requestFavouriteTimeline() {
        return requestTimeline(FAVORITE_TIMELINE_URL);
    }

    /**
     * Request public timeline from Twitter API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestFriendsTimeline() {
        return requestTimeline( FRIENDS_TIMELINE_URL );
    }    
    
    /**
     * Request public timeline from Twitter API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestUserTimeline() {
        return requestTimeline(USER_TIMELINE_URL);
    }

    /**
     * Request public timeline from Twitter API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestPublicTimeline() {
        return requestTimeline(PUBLIC_TIMELINE_URL);
    }

    /**
     * Request responses timeline from Twitter API.{
     * @return Vector containing StatusEntry items.
     */
    public Vector requestResponsesTimeline() {
        return requestTimeline(RESPONSES_TIMELINE_URL);
    }
    
    public Status updateStatus(String status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = STATUS_UPDATE_URL + 
                    "?status=" + URLUTF8Encoder.encode(status) +
                    "&source=twim";
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false && status.startsWith("d ")==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            return new Status(
                    "Twim",
                    "Error while updating status: " + ex.getMessage(),
                    Calendar.getInstance().getTime(),
                    "0");
        }
        return null;
    }

    /**
     * Request friends from Twitter API.
     * @return Vector containing friends.
     */
    public Vector requestFriends() throws IOException, Exception{
        Vector entries = new Vector();
        try {
            HttpUtil.setBasicAuthentication(username, password);
            UsersParser parser = new UsersParser();
            HttpUtil.doGet(FRIENDS_URL, parser);
            entries = parser.getUsers();
        } catch (IOException ex) {
            throw new IOException("Error in TwitterApi.requestFriends: "
                    + ex.getMessage());
        } catch (Exception ex) {
            throw new Exception("Error in TwitterApi.requestFriends: "
                    + ex.getMessage());
        }
        return entries;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    private Vector requestTimeline(String timelineUrl) {
        Vector entries = new Vector();
        try {
            HttpUtil.setBasicAuthentication(username, password);
            StatusFeedParser parser = new StatusFeedParser();
            if(timelineUrl.equals(DIRECT_TIMELINE_URL)) {
                parser.setDirect(true);
            }
            HttpUtil.doGet(timelineUrl, parser);
            entries = parser.getStatuses();
        } catch (IOException ex) {
            entries.addElement(
                    new Status("Twitter", "Error occured. Please check " +
                    "your connection or username and password.",
                    Calendar.getInstance().getTime(), ""));

            entries.addElement(
                    new Status("Twitter", "StackTrace: " + ex.toString(),
                    Calendar.getInstance().getTime(), ""));

            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    public Status markAsUnfavorite(Status status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = FAVORITE_DESTROY_URL + status.getId() + ".xml";
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            if(statuses!=null && statuses.isEmpty()==false) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            return new Status(
                    "Twim",
                    "Error while marking status as unfavorite: " + ex.getMessage(),
                    Calendar.getInstance().getTime(),
                    "0");
        }
        return null;
    }

    public Vector search(String query) throws Exception {
        try {
            SearchResultsParser parser = new SearchResultsParser();
            String url = SEARCH_URL + StringUtil.urlEncode(query);
            Log.debug("URL: " + url);
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            return statuses;
        } catch(Exception ex) {
            throw new Exception("Error while searching tweets: " + ex.getMessage());
        }
    }
    
    
    
}
