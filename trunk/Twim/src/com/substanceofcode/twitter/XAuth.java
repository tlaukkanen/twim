/*
 * XAuth.java
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

import com.substanceofcode.utils.Base64;
import com.substanceofcode.utils.HttpUtil;
import com.substanceofcode.utils.ResultParser;
import com.substanceofcode.utils.StringUtil;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class XAuth {

    private String xauthUsername;
    private String xauthPassword;
    private String token;
    private String tokenSecret;
    private String verifier;

    private static final String OAuthVersion = "1.0";
    private static final String OAuthParameterPrefix = "oauth_";

    private static final String OAuthConsumerKeyKey = "oauth_consumer_key";
    private static final String OAuthCallbackKey = "oauth_callback";
    private static final String OAuthVersionKey = "oauth_version";
    private static final String OAuthSignatureMethodKey = "oauth_signature_method";
    private static final String OAuthSignatureKey = "oauth_signature";
    private static final String OAuthTimestampKey = "oauth_timestamp";
    private static final String OAuthNonceKey = "oauth_nonce";
    private static final String OAuthTokenKey = "oauth_token";
    private static final String OAuthTokenSecretKey = "oauth_token_secret";
    private static final String OAuthVerifier = "oauth_verifier";
    private static final String XAuthUsername = "x_auth_username";
    private static final String XAuthPassword = "x_auth_password";
    private static final String XAuthMode = "x_auth_mode";

    private static final String OAUTH_CONSUMER_TOKEN = "xxx";
    private static final String OAUTH_CONSUMER_SECRET = "xxx";

    private static final String HMACSHA1SignatureType = "HMAC-SHA1";

    private String normalizedUrl = "";
    private String normalizedRequestParameters = "";

    public XAuth(String username, String password) {
        this.xauthUsername = username;
        this.xauthPassword = password;
    }

    public void setTokenAndSecret(String token, String secret) {
        this.token = token;
        this.tokenSecret = secret;
    }

    public String xAuthWebRequest(
            boolean isPost,
            String url,
            QueryParameter[] parameters,
            ResultParser parser) throws Exception {
        String outUrl = "";
        String querystring = "";
        String ret = "";
        String postData = "";
        String method = "GET";

        //Setup postData for signing.
        //Add the postData to the querystring.
        if (isPost)
        {
            method = "POST";
            if (parameters!=null && parameters.length > 0)
            {
                //Decode the parameters and re-encode using the oAuth UrlEncode method.
                for(int i=0; i<parameters.length; i++) {
                    QueryParameter q = parameters[i];
                    if(postData.length()>0) {
                        postData += "&";
                    }
                    postData += q.getName() + "=" + encode(q.getValue());
                }
                if (url.indexOf("?") > 0)
                {
                    url += "&";
                }
                else
                {
                    url += "?";
                }
                url += postData;
            }
        }
        String nonce = this.generateNonce();
        String timeStamp = this.generateTimeStamp();

        //Generate Signature
        String sig = this.generateSignature(
            url,
            OAUTH_CONSUMER_TOKEN,
            OAUTH_CONSUMER_SECRET,
            this.token,
            this.tokenSecret,
            this.verifier,
            this.xauthUsername,
            this.xauthPassword,
            method,
            timeStamp,
            nonce);

        outUrl = normalizedUrl;
        querystring = normalizedRequestParameters;

        System.out.println("Signature: " + sig);

        if(querystring.length()>0) {
            querystring += "&";
        }
        querystring += "oauth_signature=" + encode(sig);

        //Convert the querystring to postData
        /*if (isPost)
        {
            postData = querystring;
            querystring = "";
        }*/
        if (querystring.length() > 0)
        {
            outUrl += "?";
        }

        ret = webRequest(method, outUrl +  querystring, postData, parser);

        return ret;
    }

    private String webRequest(
            String method,
            String url,
            String postData,
            ResultParser parser) throws Exception {
        String result = "";
        System.out.println("web request URL: " + url);
        if (method.equals("POST")) {
            if(parser!=null) {
                result = HttpUtil.doPost(url,parser);
            } else {
                result = HttpUtil.doPost(url);
            }
        } else {
            if(parser!=null) {
                result = HttpUtil.doGet(url,parser);
            } else {
                result = HttpUtil.doGet(url);
            }
        }
        return result;
    }

    private Vector getQueryParameters(String url)
    {
        int questionMarkIndex = url.indexOf("?");
        if(questionMarkIndex<0) {
            return new Vector();
        }

        String parameters = url.substring(questionMarkIndex+1);
        Vector params = new Vector();
        String[] para = StringUtil.split(parameters, "&");
        for(int i=0; i<para.length; i++) {
            if(para[i].startsWith(OAuthParameterPrefix)==false) {
                String[] nameValue = StringUtil.split(para[i], "=");
                QueryParameter q = new QueryParameter(nameValue[0], nameValue[1]);
                params.addElement(q);
            }
        }
        return params;
    }

    public String generateSignatureBase(
            String url,
            String consumerKey,
            String token,
            String tokenSecret,
            String verifier,
            String xAuthUsername,
            String xAuthPassword,
            String httpMethod,
            String timeStamp,
            String nonce,
            String signatureType) {
        if (token == null)
        {
            token = "";
        }

        if (tokenSecret == null)
        {
            tokenSecret = "";
        }

        //normalizedUrl = null;
        //normalizedRequestParameters = null;

        Vector parameters = getQueryParameters(url);
        parameters.addElement(new QueryParameter(OAuthVersionKey, OAuthVersion));
        parameters.addElement(new QueryParameter(OAuthNonceKey, nonce));
        parameters.addElement(new QueryParameter(OAuthTimestampKey, timeStamp));
        parameters.addElement(new QueryParameter(OAuthSignatureMethodKey, signatureType));
        parameters.addElement(new QueryParameter(OAuthConsumerKeyKey, consumerKey));

        if (token!=null && token.length()!=0)
        {
            parameters.addElement(new QueryParameter(OAuthTokenKey, token));
        } else {
            if ( xAuthUsername!=null && xAuthUsername.length()!=0)
            {
                parameters.addElement(new QueryParameter(XAuthUsername, xAuthUsername));
            }

            if ( xAuthPassword!=null && xAuthPassword.length()!=0)
            {
                parameters.addElement(new QueryParameter(XAuthPassword, xAuthPassword));
                parameters.addElement(new QueryParameter(XAuthMode, "client_auth"));
            }
        }

        if (verifier!=null && verifier.length()!=0)
        {
            parameters.addElement(new QueryParameter(OAuthVerifier, verifier));
        }

        sortParameters( parameters );

        normalizedUrl = getSchemeAndHost(url);
        normalizedUrl += getAbsolutePath(url);
        System.out.println("Normalized url: " + normalizedUrl);
        normalizedRequestParameters = normalizeRequestParameters(parameters);
        System.out.println("Normalized params: " + normalizedRequestParameters);

        StringBuffer signatureBase = new StringBuffer();
        signatureBase.append(httpMethod + "&");
        signatureBase.append(encode(normalizedUrl) + "&");
        signatureBase.append(encode(normalizedRequestParameters));

        String sigBase = signatureBase.toString();
        System.out.println("Signature base: " + sigBase);
        return sigBase;
    }

    private static String getSchemeAndHost(String url) {
        int startIndex = url.indexOf("//")+2;
        int endIndex = url.indexOf("/", startIndex);
        return url.substring(0,endIndex);
    }

    private static String getAbsolutePath(String url) {
        int startIndex = url.indexOf("//")+2;
        int endIndex = url.indexOf("/", startIndex);
        int questionMark = url.indexOf("?");
        if(questionMark>0) {
            return url.substring(endIndex, questionMark);
        } else {
            return url.substring(endIndex);
        }
    }

    private static void sortParameters(Vector items) {
        boolean unsorted = true;
        System.out.println("Sorting...");
        while(unsorted) {
            unsorted = false;
            for(int i=items.size()-1; i>0; i--) {
                System.out.println("Compare...");
                QueryParameter item1 = (QueryParameter)items.elementAt(i);
                QueryParameter item2 = (QueryParameter)items.elementAt(i-1);
                if(item1.getName().compareTo(item2.getName())<0) {
                    System.out.println("Change...");
                    items.setElementAt(item1, i-1);
                    items.setElementAt(item2, i);
                    unsorted = true;
                }
            }
        }
    }

    private String generateSignature(
            String url,
            String consumerKey,
            String consumerSecret,
            String token,
            String tokenSecret,
            String verifier,
            String xAuthUsername,
            String xAuthPassword,
            String httpMethod,
            String timeStamp,
            String nonce) {
        String signatureBase = generateSignatureBase(
                url,
                consumerKey,
                token,
                tokenSecret,
                verifier,
                xAuthUsername,
                xAuthPassword,
                httpMethod,
                timeStamp,
                nonce,
                HMACSHA1SignatureType);

        String tokenSec = "";
        if(tokenSecret!=null) {
            tokenSec = tokenSecret;
        }
        String key = encode(consumerSecret) + "&" + encode(tokenSec);
        return getSignature(signatureBase, key);
    }

    public String getSignature(String message, String key)  {
        try {
            HMac m=new HMac(new SHA1Digest());
            m.init(new KeyParameter(key.getBytes("UTF-8")));
            byte[] bytes=message.getBytes("UTF-8");
            m.update(bytes, 0, bytes.length);
            byte[] mac = new byte[m.getMacSize()];
            m.doFinal(mac, 0);
            String signature = new Base64().encode(mac);
            return signature;
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected String normalizeRequestParameters(Vector parameters)
    {
        StringBuffer sb = new StringBuffer();
        QueryParameter p = null;
        Enumeration en = parameters.elements();
        while(en.hasMoreElements()) {
            p = (QueryParameter)en.nextElement();
            sb.append(p.getName()).append("=").append(p.getValue());
            if (en.hasMoreElements())
            {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    public String generateTimeStamp() {
        Date d = new Date();
        String timestamp = Long.toString(d.getTime()/1000);
        return timestamp;
    }

    public String generateNonce() {
        Random random = new Random();
        String nonce = Long.toString(Math.abs(random.nextLong()), 60000);
        return nonce;
    }

    private String unreservedCharactersPattern = "[a-zA-Z0-9\\-\\._~]";
    private String unreservedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~";

    private String encode(String s) {
        if (s == null || "".equals(s)) {
            return "";
        }
        StringBuffer sb = new StringBuffer(s.length()*2);
        for (int i = 0; i < s.length(); i++) {
            if (unreservedCharacters.indexOf(s.charAt(i)) == -1) {
                // get byte values of the character
                // and turn them into percent encoding
                String t = String.valueOf(s.charAt(i));
                sb.append(StringUtil.urlEncode(t));
            } else {
                sb.append(s.charAt(i));
            }
        }

        return sb.toString();
    }

}
