/*
 * NullParser.java
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

import com.substanceofcode.twitter.model.UserList;
import com.substanceofcode.utils.CustomInputStream;
import com.substanceofcode.utils.ResultParser;
import com.substanceofcode.utils.XmlParser;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class ListsParser implements ResultParser {

    Vector userLists;

    public Vector getUserLists() {
        return userLists;
    }

    /**
     * Users parsing.
     * Sample XML
     * <?xml version="1.0" encoding="UTF-8"?>
     * <lists_list>
     * <lists type="array">
     * <list>
     *  <id>2029636</id>
     *  <name>firemen</name>
     *  <full_name>@twitterapidocs/firemen</full_name>
     *  <slug>firemen</slug>
     *  <member_count>0</member_count>
     *  <uri>/twitterapidocs/firemen</uri>
     * </list>
     * @param is
     */
    public void parse(CustomInputStream is) throws IOException {
        try {
            XmlParser xml = new XmlParser(is);
            userLists = new Vector();
            System.out.println("Parsing list XML");
            while (xml.parse() != XmlParser.END_DOCUMENT) {
                String elementName = xml.getName();
                if (elementName.equals("list")) {
                    String listXml = xml.getInnerXml();
                    System.out.println("list XML: " + listXml);
                    UserList userList = parseList( listXml );
                    if(userList!=null) {
                        userLists.addElement( userList );
                    }
                }
            }
        } catch (IOException ex) {
            throw new IOException("IOException in UserParser.parse(): " + ex.getMessage());
        }
    }

    private UserList parseList(String listXml) throws IOException {
        if(listXml==null) {
            return null;
        }
        String state = "";
        try {
            //System.out.println("LISTXML: " + userXml);
            XmlParser xml = new XmlParser(listXml);
            String name = "";
            String uri = "";
            String id = "";
            state = "starting parsing ";
            while (xml.parse() != XmlParser.END_DOCUMENT) {
                Thread.yield();
                String elementName = xml.getName();
                if(elementName.equals("name")) {
                    if(name.length()==0) {
                        name = xml.getText();
                    }
                } else if(elementName.equals("uri")) {
                    if(uri.length()==0) {
                        uri = xml.getText();
                    }
                } else if(elementName.equals("id")) {
                    if(id.length()==0) {
                        id = xml.getText();
                    }
                }
            }
            state = "creating new list instance";
            UserList list = new UserList(id, name, uri);
            return list;
        } catch (IOException ex) {
            String sample = listXml;
            throw new IOException("Err while " + state + " in ListsParser.parseList: " + ex.getMessage()
                    + "\nXML: " + sample);
        }
    }

}
