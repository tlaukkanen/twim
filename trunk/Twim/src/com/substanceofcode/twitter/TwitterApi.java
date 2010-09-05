/*
 * TwitterApi.java
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

import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.StringUtil;
import java.io.IOException;
import java.util.Calendar;
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
    private static final String HOME_TIMELINE_URL = "http://api.twitter.com/1/statuses/home_timeline.xml"; // Old: "http://www.twitter.com/statuses/friends_timeline.xml";
    private static final String USER_TIMELINE_URL = "http://api.twitter.com/1/statuses/user_timeline.xml";
    private static final String RESPONSES_TIMELINE_URL = "http://api.twitter.com/1/statuses/mentions.xml";
    private static final String STATUS_UPDATE_URL = "http://api.twitter.com/1/statuses/update.xml"; // "http://twitter.com/statuses/update.xml";
    private static final String DIRECT_TIMELINE_URL = "http://api.twitter.com/1/direct_messages.xml";
    private static final String FRIENDS_URL = "http://api.twitter.com/1/statuses/friends_timeline.xml";
    private static final String FAVORITE_TIMELINE_URL = "http://api.twitter.com/1/favorites.xml";
    private static final String FAVORITE_CREATE_URL = "http://api.twitter.com/1/favorites/create/";
    private static final String FAVORITE_DESTROY_URL = "http://api.twitter.com/1/favorites/destroy/";
    private static final String FRIENDSHIPS_CREATE_URL = "http://api.twitter.com/1/friendships/create/";
    private static final String FRIENDSHIPS_DESTROY_URL = "http://api.twitter.com/1/friendships/destroy/";
    private static final String SEARCH_URL = "http://search.twitter.com/search.atom?rpp=20&q=";
    private static final String RETWEETS_OF_ME_URL = "http://api.twitter.com/1/statuses/retweets_of_me.xml";
    private static final String LISTS_URL = "http://api.twitter.com/1/@USERNAME@/lists.xml";
    private static final String LIST_STATUSES_URL = "http://api.twitter.com/1/@USERNAME@/lists/@LIST@/statuses.xml";

    // XAuth specific parameters
    private static final String OAUTH_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String OAUTH_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private static final String OAUTH_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

    private static boolean isAuthenticated = false;
    private static Status authErrStatus = null;
    private static XAuth xauth;

    /** Creates a new instance of TwitterApi */
    public TwitterApi() {
    }

    public void bypassAuthorization(String token, String tokenSecret) {
        if(token!=null && token.length()>0) {
            xauth = new XAuth(username, password);
            xauth.setTokenAndSecret(token, tokenSecret);
            isAuthenticated = true;
        } else {
            isAuthenticated = false;
        }
    }

    public boolean authorize() {
        if(isAuthenticated) {
            return true;
        }
        String token = "";
        try {
            System.out.println("Trying to authenticate");
            xauth = new XAuth(username, password);
            token = xauth.xAuthWebRequest(false, OAUTH_ACCESS_TOKEN_URL, null, null);
            System.out.println("ACCESS TOKEN: " + token);
            if(token.indexOf("oauth_token_secret")>0) {
                // Success
                String oauthToken = HttpUtil.parseParameter(token, "oauth_token");
                String oauthTokenSecret = HttpUtil.parseParameter(token, "oauth_token_secret");
                xauth.setTokenAndSecret(oauthToken, oauthTokenSecret);
                isAuthenticated = true;
                authErrStatus = null;
                return true;
            } else {
                // Failure
                authErrStatus = new Status("Twitter", "Couldn't find OAuth token from response: " + token, null, "");
                isAuthenticated = false;
                return false;
            }
        } catch (Exception ex) {
            authErrStatus = new Status("Twitter", "Couldn't authenticate. Exception: " + ex.getMessage(), null, "");
            isAuthenticated = false;
            return false;
        }
    }

    public String followUser(Status status) throws Exception {
        try {
            NullParser parser = new NullParser();
            String url = FRIENDSHIPS_CREATE_URL + status.getScreenName() + ".xml";
            xauth.xAuthWebRequest(true, url, null, parser);
            //HttpUtil.doPost( url, parser );
            return parser.getResponse();
        } catch(Exception ex) {
            throw ex;
        }
    }

    public String unfollowUser(Status status) throws Exception {
        try {
            NullParser parser = new NullParser();
            String url = FRIENDSHIPS_DESTROY_URL + status.getScreenName() + ".xml";
            xauth.xAuthWebRequest(true, url, null, parser);
            return parser.getResponse();
        } catch(Exception ex) {
            throw ex;
        }
    }

    public Status markAsFavorite(Status status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = FAVORITE_CREATE_URL + status.getId() + ".xml";
            xauth.xAuthWebRequest(true, url, null, parser);
            //HttpUtil.doPost( url, parser );
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
     * Request favorite tweets from Twitter API.
     * @return Vector containing favorite tweets.
     */
    public Vector requestFavouriteTimeline() {
        return requestTimeline(FAVORITE_TIMELINE_URL);
    }

    /**
     * Request home (following) timeline from Twitter API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestHomeTimeline(int page) {
        String url = HOME_TIMELINE_URL;
        if(page>1) {
            url += "?page=" + page;
        }
        return requestTimeline( url );
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

    public Vector requestRetweetsOfMe(int page) {
        String url = RETWEETS_OF_ME_URL;
        if(page>1) {
            url += "?page=" + page;
        }
        return requestTimeline( url );
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
            if(authorize()==false) {
                if(authErrStatus!=null) {
                    return authErrStatus;
                }
            }

            StatusFeedParser parser = new StatusFeedParser();
            String url = STATUS_UPDATE_URL;
            QueryParameter[] params = new QueryParameter[] {
                new QueryParameter("status", status),
                new QueryParameter("source", "twim")
            };
            // "?status=" + URLUTF8Encoder.encode(status) +
            // "&source=twim";
            xauth.xAuthWebRequest(true, STATUS_UPDATE_URL, params, parser);
            //HttpUtil.doPost( url, parser );
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
    public Vector requestFriendsTimeline() throws IOException, Exception{
        Vector entries = new Vector();
        authorize();
        try {
            HttpUtil.setBasicAuthentication("", "");
            StatusFeedParser parser = new StatusFeedParser();
            xauth.xAuthWebRequest(false, FRIENDS_URL, null, parser);
            //HttpUtil.doGet(FRIENDS_URL, parser);
            entries = parser.getStatuses();
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
        if(authorize()==false) {
            if(authErrStatus!=null) {
                entries.addElement(authErrStatus);
                return entries;
            }
        }
        try {
            boolean retry = false;
            do {
                //HttpUtil.setBasicAuthentication(username, password);
                HttpUtil.setBasicAuthentication("", "");
                StatusFeedParser parser = new StatusFeedParser();
                if(timelineUrl.equals(DIRECT_TIMELINE_URL)) {
                    parser.setDirect(true);
                }
                xauth.xAuthWebRequest(false, timelineUrl, null, parser);
                //HttpUtil.doGet(timelineUrl, parser);
                int lastResponseCode = HttpUtil.getLastResponseCode();
                entries = parser.getStatuses();
                if(entries.isEmpty() && parser.isReallyEmpty()==false) {
                    entries.addElement(
                        new Status("Twitter", "No statuses. API response from " +
                        timelineUrl + " (" + lastResponseCode + "): " +
                        HttpUtil.getHeaders() + " " +
                        parser.getRawData(),
                        Calendar.getInstance().getTime(), ""));
                    retry = !retry;
                } else if(entries.isEmpty() && parser.isReallyEmpty()==true) {
                    entries.addElement(
                        new Status("Twitter", "No Tweets found.",
                        Calendar.getInstance().getTime(), ""));
                } else {
                    retry = false;
                }
            } while(retry);
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
            entries.addElement(
                    new Status("Twitter", "API exception: " + ex.toString(),
                    Calendar.getInstance().getTime(), ""));
        }
        return entries;
    }

    public Status markAsUnfavorite(Status status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = FAVORITE_DESTROY_URL + status.getId() + ".xml";
            xauth.xAuthWebRequest(true, url, null, parser);
            //HttpUtil.doPost( url, parser );
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

    public Vector search(String query, int page) throws Exception {
        try {
            SearchResultsParser parser = new SearchResultsParser();
            String url = SEARCH_URL + StringUtil.urlEncode(query) + "&page=" + page;
            Log.debug("URL: " + url);
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            return statuses;
        } catch(Exception ex) {
            throw new Exception("Error while searching tweets: " + ex.getMessage());
        }
    }

    public Vector requestLists() throws Exception {
        authorize();

        Vector entries = new Vector();
        try {
            ListsParser parser = new ListsParser();
            String url = StringUtil.replace(LISTS_URL, "@USERNAME@", username);
            xauth.xAuthWebRequest(false, url, null, parser);
            //HttpUtil.doGet(url, parser);
            entries = parser.getUserLists();
        } catch (IOException ex) {
            throw new IOException("Error in TwitterApi.requestLists: "
                    + ex.getMessage());
        } catch (Exception ex) {
            throw new Exception("Error in TwitterApi.requestLists: "
                    + ex.getMessage());
        }
        return entries;
    }

    public Vector requestListStatuses(String listName) {
        String url = StringUtil.replace(LIST_STATUSES_URL, "@LIST@", StringUtil.urlEncode(listName));
        url = StringUtil.replace(url, "@USERNAME@", username);
        System.out.println("Loading custom URL: " + url);
        return requestTimeline(url);
    }

    void resetToken() {
        xauth.setTokenAndSecret("", "");
        isAuthenticated = false;
    }
    
    
    
}
