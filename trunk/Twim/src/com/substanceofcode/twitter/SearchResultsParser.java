/*
 * SearchResultsParser
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
import com.substanceofcode.utils.CustomInputStream;
import com.substanceofcode.utils.DateTimeUtil;
import com.substanceofcode.utils.Log;
import com.substanceofcode.utils.ResultParser;
import com.substanceofcode.utils.XmlParser;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 *
 * <entry>
 *  <id>tag:search.twitter.com,2005:2619936198</id>
 *  <published>2009-07-13T20:27:58Z</published>
 *  <link type="text/html" rel="alternate" href="http://twitter.com/mikeful/statuses/2619936198"/>
 *  <title>Testing Twim on Nokia E63. Bit slow for some reason but still useable.</title>
 *  <content type="html">Testing &lt;b&gt;Twim&lt;/b&gt; on Nokia E63. Bit slow for some reason but still useable.</content>
 *  <updated>2009-07-13T20:27:58Z</updated>
 *  <link type="image/png" rel="image" href="http://s3.amazonaws.com/twitter_production/profile_images/58314882/lol_naama_normal.png"/>
 *  <twitter:source>&lt;a href="http://www.substanceofcode.com/software/mobile-twitter-client-twim/"&gt;Twim&lt;/a&gt;</twitter:source>
 *  <twitter:lang>en</twitter:lang>
 *  <author>
 *    <name>mikeful (Mikko Niemikorpi)</name>
 *    <uri>http://twitter.com/mikeful</uri>
 *  </author>
 * </entry>
 *
 * @author tommi
 */
public class SearchResultsParser implements ResultParser {

    Vector statuses;

    public SearchResultsParser() {
        statuses = new Vector();
    }

    public Vector getStatuses() {
        return statuses;
    }

    public void parse(CustomInputStream is) throws IOException {
/*
        int ch = is.read();
        while(ch>0) {
            System.out.print((char)ch);
            ch = is.read();
        }
*/
        try {
            XmlParser xml = new XmlParser(is);
            String text = "";
            String screenName = "";
            String id = "";
            Date date = null;
            boolean entryStarted = false;
            Log.debug("Starting parsing");
            while (xml.parse() != XmlParser.END_DOCUMENT) {
                String elementName = xml.getName();
                Thread.yield();
                //Log.debug("XML:" + elementName);

                if (elementName.equals("error")) {
                    // Parse error message
                    text = "Error from Twitter: " + xml.getText();
                    screenName = "Twitter";
                    date = new Date(System.currentTimeMillis());
                } else if (elementName.equals("entry")) {
                    // Parse normal status
                    if (entryStarted && text.length() > 0) {
                        Status status = new Status(screenName, text, date, id);
                        statuses.addElement(status);
                    }
                    entryStarted = true;
                    text = "";
                    screenName = "";
                    id = "";
                    date = null;
                
                } else if (elementName.equals("id") && id.equals("")) {
                    id = xml.getText();
                    int idIndex = id.indexOf(":", 5);
                    Log.debug("id: " + id + " index: " + idIndex);
                    if(idIndex>0) {
                        id = id.substring( idIndex+1 );
                    }
                } else if (elementName.equals("title")) {
                    text += xml.getText();
                } else if (elementName.equals("name")) {
                    screenName = xml.getText();
                    int space = screenName.indexOf(" ");
                    screenName = screenName.substring(0,space);
                } else if (elementName.equals("published")) {
                    if(date==null) {
                        String dateString = xml.getText();
                        date = DateTimeUtil.getDateFromUniversalDateStamp(dateString);
                    }
                    //date = new Date(System.currentTimeMillis());
                }

            }
            if (text.length() > 0 && entryStarted) {
                Status status = new Status(screenName, text, date, id);
                statuses.addElement(status);
            }
        } catch (Exception ex) {
            throw new IOException("Error in SearchResultsParser.parse(): " + ex.getMessage());
        }
    }

}
