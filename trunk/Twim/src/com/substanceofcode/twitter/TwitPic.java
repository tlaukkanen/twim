/*
 * TwitPicApi.java
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
import com.substanceofcode.utils.CustomInputStream;
import com.substanceofcode.utils.XmlParser;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author tommi
 */
public class TwitPic implements PhotoService {

    private final static String TWIT_PIC_URL = "http://twitpic.com/api/uploadAndPost";
    private static String response = "";
    private static TwitPic instance;

    private TwitPic() {

    }

    public static TwitPic getInstance() {
        if(instance==null) {
            instance = new TwitPic();
        }
        return instance;
    }

    public String getResponse() {
        return response;
    }

    public Status sendPhoto(
            byte[] photo,
            String comment,
            String username,
            String password) throws IOException, Exception {
        HttpConnection connection = null;
        try {
            connection = (HttpConnection) Connector.open(TWIT_PIC_URL);
            connection.setRequestMethod( HttpConnection.POST );
            String boundary = "BoUnDaRy888";
            connection.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);
            DataOutputStream dos = connection.openDataOutputStream();

            // Media
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"media\"; filename=\"photo.jpg\"\r\n");
            writeString(dos, "Content-Transfer-Encoding: binary\r\n");
            writeString(dos, "\r\n");
            dos.write(photo,0,photo.length);
            writeString(dos, "\r\n");
            
            // Username
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"username\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, username + "\r\n");

            // Password
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"password\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, password + "\r\n");

            // Password
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"source\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, "Twim\r\n");

            // Message
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"message\"\r\n");
            
            writeString(dos, "\r\n");
            writeString(dos, comment + "\r\n");

            writeString(dos, "--" + boundary + "--\r\n");
            dos.flush();
            dos.close();

            InputStream his = connection.openInputStream();
            CustomInputStream is = new CustomInputStream(his);

            // Prepare buffer for input data
            StringBuffer inputBuffer = new StringBuffer();

            // Read all data to buffer
            int inputCharacter;
            try {
                while ((inputCharacter = is.read()) != -1) {
                    inputBuffer.append((char) inputCharacter);
                }
            } catch (IOException ex) {
                return null;
            }
            //totalBytes += response.length();
            if(his!=null) {
                his.close();
            }
            if(is!=null) {
                is.close();
            }

            // Split buffer string by each new line
            response = inputBuffer.toString();

            // Parse response
            boolean status = false;
            String mediaUrl = "";
            String err = "";
            XmlParser parser = new XmlParser(response);
            while(parser.parse()!=XmlParser.END_DOCUMENT) {
                String elementName = parser.getName();
                if(elementName.equals("rsp")) {
                    String statusValue = parser.getAttributeValue("status");
                    if(statusValue.equals("ok")) {
                        status = true;
                    }
                } else if(elementName.equals("mediaurl")) {
                    mediaUrl = parser.getText();
                } else if(elementName.equals("err")) {
                    err = parser.getAttributeValue("msg");
                }
            }

            // Create status based on response
            Status stat = null;
            Date now = Calendar.getInstance().getTime();
            if(status) {
                stat = new Status(username, mediaUrl + " - " + comment, now, "");
            } else {
                stat = new Status("TwitPic", err, now, "");
            }
            return stat;
            
        } catch (IOException e) {
            throw new IOException("IOException: " + e.toString());
        } catch (Exception e) {
            throw new Exception("Error while posting: " + e.toString());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void writeString(DataOutputStream dos, String string)
            throws IOException {
        byte[] b = string.getBytes();
        dos.write(b, 0, b.length);
    }

}
