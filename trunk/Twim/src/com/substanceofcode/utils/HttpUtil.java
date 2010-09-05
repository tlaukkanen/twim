/*
 * HttpUtil.java
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

package com.substanceofcode.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author Tommi Laukkanen
 */
public class HttpUtil extends HttpAbstractUtil {

    /** Total bytes transfered */
    private static long totalBytes = 0;

    /** Last response code */
    private static int lastResponseCode = 0;

    /** HTTP Headers */
    private static String headers = "";
    private static String lastResponseContentType = "";

    public static String getHeaders() {
        return headers;
    }


    public static int getLastResponseCode() {
        return lastResponseCode;
    }
    
    /** Creates a new instance of HttpUtil */
    public HttpUtil() {
    }

    public static String doPost(String url) throws IOException, Exception {
        return HttpUtil.doPost(url, null);
    }

    public static String doGet(String url) throws IOException, Exception {
        return doRequest(url, null, HttpConnection.GET);
    }

    public static String doGet(String url, ResultParser parser) throws IOException, Exception {
        return doRequest(url, parser, HttpConnection.GET);
    }

    public static String doPost(String url, ResultParser parser) throws IOException, Exception {
        return doRequest(url, parser, HttpConnection.POST);
    }

    public static String doRequest(String url, ResultParser parser, String requestMethod) throws IOException, Exception {
        HttpConnection hc = null;
        DataInputStream dis = null;
        String response = "";
        lastResponseCode = 0;
        try {
            /**
             * Open an HttpConnection with the Web server
             * The default request method is GET
             */
            hc = (HttpConnection) Connector.open( url );
            hc.setRequestMethod(requestMethod);
            /** Some web servers requires these properties */
            //hc.setRequestProperty("User-Agent",
            //        "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            hc.setRequestProperty("Content-Length", "0");
            hc.setRequestProperty("Connection", "close");

            Log.add("Posting ("+ requestMethod +") to URL: " + url);

            // Cookie: name=SID; domain=.google.com; path=/; expires=1600000000; content=
            if (cookie != null && cookie.length() > 0) {
                hc.setRequestProperty("Cookie", cookie);
            }

            if (username!=null && username.length() > 0) {
                /**
                 * Add authentication header in HTTP request. Basic authentication
                 * should be formatted like this:
                 *     Authorization: Basic QWRtaW46Zm9vYmFy
                 */
                String userPass;
                Base64 b64 = new Base64();
                userPass = username + ":" + password;
                userPass = b64.encode(userPass.getBytes());
                hc.setRequestProperty("Authorization", "Basic " + userPass);
            }


            /**
             * Get a DataInputStream from the HttpConnection
             * and forward it to XML parser
             */
            InputStream his = hc.openInputStream();
            CustomInputStream is = new CustomInputStream(his);

            /** Check for the cookie */
            String sessionCookie = hc.getHeaderField("Set-cookie");
            if (sessionCookie != null) {
                int semicolon = sessionCookie.indexOf(';');
                cookie = sessionCookie.substring(0, semicolon);
                Log.debug("Using cookie: " + cookie);
            } else {
                Log.debug("No cookie found");
            }

            lastResponseCode = hc.getResponseCode();
            lastResponseContentType = hc.getHeaderField("Content-Type");
            
            String header = "";
            int headerIndex = 0;
            while((header=hc.getHeaderField(headerIndex))!=null) {
                headerIndex++;
                if(header==null || header.length()==0) {
                    break;
                }
                headers += header + "\n";
            }
            

            if (parser == null) {
                // Prepare buffer for input data
                StringBuffer inputBuffer = new StringBuffer();

                // Read all data to buffer
                int inputCharacter;
                try {
                    while ((inputCharacter = is.read()) != -1) {
                        inputBuffer.append((char) inputCharacter);
                    }
                } catch (IOException ex) {
                    Log.error("Error while reading response: " + ex.getMessage());
                }

                // Split buffer string by each new line
                response = inputBuffer.toString();
                totalBytes += response.length();
            } else {
                parser.parse(is);
            }
            if(his!=null) {
                his.close();
            }
            if(is!=null) {
                is.close();
            }
            // DEBUG_END
        } catch (IOException e) {
            throw new IOException("IOException: " + e.toString());
        } catch (Exception e) {
            throw new Exception("Error while posting: " + e.toString());
        } finally {
            if (hc != null) {
                hc.close();
            }
            if (dis != null) {
                dis.close();
            }
        }
        return response;
    }

    public static String parseParameter(String url, String parameter) {
        int parameterIndex = url.indexOf(parameter);
        if(parameterIndex<0) {
            // We didn't find parameter
            return "";
        }
        int equalIndex = url.indexOf("=", parameterIndex);
        if(equalIndex<0) {
            return "";
        }
        String value = "";
        int nextIndex = url.indexOf("&", equalIndex+1);
        if(nextIndex<0) {
            value = url.substring(equalIndex+1);
        } else {
            value = url.substring(equalIndex+1, nextIndex);
        }
        return value;
    }
}