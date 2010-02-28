/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.services;

import com.substanceofcode.twitter.PhotoService;
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
public class GenericUploadAndPostService implements PhotoService {

    protected String url = "";
    private static String response = "";
    private static GenericUploadAndPostService instance;
    protected String photoContentPartName = "media";
    protected String statusContentPartName = "message";
    protected boolean includeSource = false;

    GenericUploadAndPostService() {
    }

    public static GenericUploadAndPostService getInstance(GenericUploadAndPostService service) {
        instance = service;
        return instance;
    }

    public static GenericUploadAndPostService getInstance() {
        if(instance==null) {
            instance = new GenericUploadAndPostService();
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
        String state = "posting";
        try {
            connection = (HttpConnection) Connector.open( url );
            connection.setRequestMethod( HttpConnection.POST );
            String boundary = "BoUnDaRy888";
            connection.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);
            DataOutputStream dos = connection.openDataOutputStream();

            // Media
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"" + photoContentPartName + "\"; filename=\"" + filename + "\"\r\n");
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

            // Message
            writeString(dos, "--" + boundary + "\r\n");
            writeString(dos, "Content-Disposition: form-data; name=\"" + statusContentPartName + "\"\r\n");
            writeString(dos, "\r\n");
            writeString(dos, comment + "\r\n");

            if(includeSource) {
                // Source
                writeString(dos, "--" + boundary + "\r\n");
                writeString(dos, "Content-Disposition: form-data; name=\"source\"\r\n");
                writeString(dos, "\r\n");
                writeString(dos, "Twim\r\n");

                // Source link
                writeString(dos, "--" + boundary + "\r\n");
                writeString(dos, "Content-Disposition: form-data; name=\"sourceLink\"\r\n");
                writeString(dos, "\r\n");
                writeString(dos, "http://www.substanceofcode.com/software/mobile-twitter-client-twim/\r\n");
            }

            writeString(dos, "--" + boundary + "--\r\n");
            dos.flush();
            dos.close();

            InputStream his = connection.openInputStream();
            CustomInputStream is = new CustomInputStream(his);

            state = "parsing response";

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
            state = "parsing response XML";
            boolean status = false;
            String err = "";
            String mediaUrl = response;
            if(response.startsWith("0") || response.startsWith("http://moby")) {
                // OK
                status = true;
            } else {
                // Parse response
                XmlParser parser = new XmlParser(response);
                while(parser.parse()!=XmlParser.END_DOCUMENT) {
                    String elementName = parser.getName();
                    if(elementName.equals("rsp")) {
                        String statusValue = parser.getAttributeValue("status");
                        if(statusValue!=null && statusValue.equals("ok")) {
                            status = true;
                        } else {
                            statusValue = parser.getAttributeValue("stat");
                            if(statusValue!=null && statusValue.equals("ok")) {
                                status = true;
                            }
                        }
                    } else if(elementName.equals("mediaurl")) {
                        mediaUrl = parser.getText();
                    } else if(elementName.equals("err")) {
                        err = parser.getAttributeValue("msg");
                    } else if(elementName.equals("success")) {
                        status = true;
                    } else if(elementName.equals("url")) {
                        mediaUrl = parser.getText();
                    }
                }
            }

            state = "creating status";

            // Create status based on response
            Status stat = null;
            Date now = Calendar.getInstance().getTime();
            if(status) {
                stat = new Status(username, comment + " - " + mediaUrl, now, "");
            } else {
                stat = new Status("Remote API", "Error in URL: " + url + " Err: " + err + " Response: " + response, now, "");
            }
            return stat;

        } catch (IOException e) {
            throw new IOException("IOException: " + e.toString());
        } catch (Exception e) {
            throw new Exception("Error while " + state + ": " + e.toString());
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
