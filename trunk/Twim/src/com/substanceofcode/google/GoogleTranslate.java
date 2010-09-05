/*
 * GoogleTranslate.java
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

package com.substanceofcode.google;

import com.substanceofcode.utils.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen [at] gmail [dot] com)
 */
public class GoogleTranslate {

    private static final String SERVICE_URL = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=|en&q=";

    /**
     * Translate given text to English. Source language is automatically
     * identified.
     * @param text to be translated
     * @return text in English
     */
    public static String translate(String text) {
        HttpConnection hc = null;
        try {
            String url = SERVICE_URL + StringUtil.urlEncode(text);
            hc = (HttpConnection) Connector.open(url);
            InputStream his = hc.openInputStream();

            // Prepare buffer for input data
            StringBuffer inputBuffer = new StringBuffer();

            // Read all data to buffer
            int inputCharacter;
            while ((inputCharacter = his.read()) != -1) {
                inputBuffer.append((char) inputCharacter);
            }
            String response = inputBuffer.toString();
            String startText = "translatedText\":\"";
            int startIndex = response.indexOf( startText );
            if(startIndex>0) {
                int endIndex = response.indexOf("\"", startIndex + startText.length());
                if(endIndex>0) {
                    String translatedText = response.substring(startIndex+startText.length(), endIndex);
                    return translatedText;
                }
            }

        } catch (IOException ex) {
            return "Error " + ex.getMessage();
        } finally {
            if(hc!=null) {
                try {
                    hc.close();
                } catch (IOException ex) {
                    return "Error " + ex.getMessage();
                }
            }
        }
        return "";
    }

}
