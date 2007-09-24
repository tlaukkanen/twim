/*
 * TwitterApi.java
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
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.StringUtil;
import java.io.IOException;
import java.util.Vector;

/**
 * TwitterApi
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TwitterApi {

    private String username;
    private String password;
    private static final String FRIENDS_TIMELINE_URL = "http://www.twitter.com/statuses/friends_timeline.xml";
    private static final String USER_TIMELINE_URL = "http://www.twitter.com/statuses/user_timeline.xml";
    private static final String RESPONSES_TIMELINE_URL = "http://twitter.com/statuses/replies.format";
    private static final String STATUS_UPDATE_URL = "http://twitter.com/statuses/update.xml";

    /** Creates a new instance of TwitterApi */
    public TwitterApi() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Request public timeline from Twitter API.
     * @return Vector containing StatusEntry items.
     */
    public Vector requestPublicTimeline() {
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
     * Request responses timeline from Twitter API.{
     * @return Vector containing StatusEntry items.
     */
    public Vector requestResponsesTimeline() {
        return requestTimeline(RESPONSES_TIMELINE_URL);
    }  
    
    public Status updateStatus(String status) {
        try {
            StatusFeedParser parser = new StatusFeedParser();
            String url = STATUS_UPDATE_URL + "?status=" + StringUtil.urlEncode(status);
            HttpUtil.doPost( url, parser );
            Vector statuses = parser.getStatuses();
            if(status.length()==1) {
                return (Status)statuses.elementAt(0);
            }
        } catch(Exception ex) {
            Log.error("Error while updating status: " + ex.getMessage());
        }
        return null;
    }
    
    private Vector requestTimeline(String timelineUrl) {
        Vector entries = new Vector();
        try {
            HttpUtil.setBasicAuthentication(username, password);
            StatusFeedParser parser = new StatusFeedParser();
            HttpUtil.doGet(timelineUrl, parser);
            entries = parser.getStatuses();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;        
    }
    
    
    
}