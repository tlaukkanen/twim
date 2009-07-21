/*
 * StatusEntry.java
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
package com.substanceofcode.twitter.model;

import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.StringUtil;
import java.util.Date;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Font;
import javax.microedition.midlet.MIDlet;

/**
 * StatusEntry
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Status {

    private String screenName;
    private String statusText;
    private Date date;
    private String id;
    private String inReplyToId;
    private boolean isDirect;
    private boolean isFavorite;
    private boolean isFollowing;
    /** For optimizations */
    private int height;
    private String[] textLines;

    /** Creates a new instance of StatusEntry 
     * @param screenName 
     * @param statusText 
     * @param date 
     */
    public Status(String screenName, String statusText, Date date, String id) {
        this.screenName = screenName;
        this.statusText = statusText;
        if(date!=null) {
            this.date = date;
        } else {
            this.date = new Date(System.currentTimeMillis());
        }
        this.height = 0;
        this.id = id;
        this.isDirect = false;
        this.isFavorite = false;

        Log.debug("Date: " + date);
    }

    public void setFollowing(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setInReplyToId(String replyToId) {
        this.inReplyToId = replyToId;
    }

    public String getInReplyToId() {
        return this.inReplyToId;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }

    public boolean isFavorite() {
        return this.isFavorite;
    }

    public void setDirect(boolean isDirect) {
        this.isDirect = isDirect;
    }

    public boolean isDirect() {
        return this.isDirect;
    }

    public void createTextLines(int textBoxWidth, Font textFont) {
        String[] text = {statusText};
        textLines = StringUtil.formatMessage(text, textBoxWidth, textFont);
    }

    public String getText() {
        return statusText;
    }

    public String getScreenName() {
        return screenName;
    }

    public Date getDate() {
        return date;
    }

    public int getHeight() {
        return height;
    }

    public String getId() {
        return id;
    }

    public String[] getTextLines() {
        return textLines;
    }

    public void setHeight(int h) {
        height = h;
    }

    /** Check if status has a link */
    public boolean hasLink() {
        if (statusText.indexOf("http://") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /** Launch link */
    public void openInBrowser(MIDlet midlet) {
        String url = "http://twitter.com/" + screenName + "/statuses/" + id;
        try {
            /** Open link in browser */
            if (midlet.platformRequest(url)) {
                //midlet.destroyApp(false);
                midlet.notifyDestroyed();
            }
        } catch (ConnectionNotFoundException ex) {
            // Don't open
        }
    }

    public void openIncludedLink(MIDlet midlet) {
        String url = "";
        int startIndex = statusText.indexOf("http://");
        if (startIndex >= 0) {
            int endIndex = statusText.indexOf(" ", startIndex);
            if (endIndex > 0) {
                url = statusText.substring(startIndex, endIndex);
            } else {
                url = statusText.substring(startIndex);
            }
        } else {
            url = "http://twitter.com/" + screenName + "/statuses/" + id;
        }
        try {
            /** Open link in browser */
            if (midlet.platformRequest(url)) {
                //midlet.destroyApp(false);
                midlet.notifyDestroyed();
            }
        } catch (ConnectionNotFoundException ex) {
            // Don't open
        }
    }
}
