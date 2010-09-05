/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.views;

import com.substanceofcode.utils.ImageUtil;
import javax.microedition.lcdui.Image;

/**
 *
 * @author tommi
 */
public class Images {

    private static Image user;

    public static Image getUser() {
        if(user==null) {
            user = ImageUtil.loadImage("/images/user-silhouette.png");
        }
        return user;
    }

}
