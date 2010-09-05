/*
 * Mobypicture.java
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

package com.substanceofcode.twitter.services;

import com.substanceofcode.twitter.PhotoService;
import com.substanceofcode.twitter.VideoService;
import com.substanceofcode.twitter.model.Status;
import com.substanceofcode.utils.CustomInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author Tommi Laukkanen
 */
public class Mobypicture implements PhotoService, VideoService {

    private final static String MOBYPICTURE_URL = "http://api.mobypicture.com/";
    private final static String MOBYPICTURE_DEV_KEY = "cOWRTeoxvFKJipTY";
    private static String response = "";
    private static Mobypicture instance;

    private Mobypicture() {
    }

    public static Mobypicture getInstance() {
        if(instance==null) {
            instance = new Mobypicture();
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
            String password,
            String filename) throws IOException, Exception {
        HttpConnection connection = null;
        try {
            connection = (HttpConnection) Connector.open(MOBYPICTURE_URL);
            connection.setRequestMethod( HttpConnection.POST );
            String boundary = "BoUnDaRy888";
            connection.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);
            DataOutputStream dos = connection.openDataOutputStream();

            // Action
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"action\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, "postMediaUrl\r\n");

            // Media
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"i\"; filename=\"" + filename + "\"\r\n");
            writeString(dos, "Content-Transfer-Encoding: binary\r\n");
            writeString(dos, "\r\n");
            dos.write(photo,0,photo.length);
            writeString(dos, "\r\n");

            // Username
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"u\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, username + "\r\n");

            // Password
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"p\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, password + "\r\n");

            // Developer key
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"k\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, MOBYPICTURE_DEV_KEY + "\r\n");

            // Message
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"t\"\r\n");

            writeString(dos, "\r\n");
            dos.write(comment.getBytes("UTF-8"));
            writeString(dos, "\r\n");

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
            String err = "";
            if(response.startsWith("0") || response.startsWith("http://moby")) {
                // OK
                status = true;
            } else {
                // Error
                status = false;
                err = response;
            }

            // Create status based on response
            Status stat = null;
            Date now = Calendar.getInstance().getTime();
            if(status) {
                stat = new Status(username, comment + " - " + response, now, "");
            } else {
                stat = new Status("Mobypicture", "Error: " + err, now, "");
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

    public Status sendVideo(
            byte[] video,
            String comment,
            String username,
            String password,
            String filename) throws IOException, Exception {
        return sendPhoto(video, comment, username, password, filename);
    }

    private static void writeString(DataOutputStream dos, String string)
            throws IOException {
        byte[] b = string.getBytes();
        dos.write(b, 0, b.length);
    }

}
