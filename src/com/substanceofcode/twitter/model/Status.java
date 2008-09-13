/*
 * StatusEntry.java
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

package com.substanceofcode.twitter.model;

import com.substanceofcode.utils.StringUtil;
import java.util.Date;
import javax.microedition.lcdui.Font;

/**
 * StatusEntry
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Status {

    private String screenName;
    private String statusText;
    private Date date;

    /** For optimizations */
    private int height;
    private String[] textLines;
    
    /** Creates a new instance of StatusEntry 
     * @param screenName 
     * @param statusText 
     * @param date 
     */
    public Status(String screenName, String statusText, Date date) {
        this.screenName = screenName;
        this.statusText = statusText;
        this.date = date;
        this.height = 0;
    }

    public void createTextLines(int textBoxWidth, Font textFont) {
        String[] text = { statusText };
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

    public String[] getTextLines() {
        return textLines;
    }

    public void setHeight(int h) {
        height = h;
    }

}
