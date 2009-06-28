/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.PhotoService;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.views.MenuAction;

/**
 *
 * @author tommi
 */
public class PhotoServiceChangeAction implements MenuAction {

    PhotoService service;
    byte[] media;
    String filename;

    public PhotoServiceChangeAction(PhotoService service, byte[] media, String filename) {
        this.service = service;
        this.media = media;
        this.filename = filename;
    }


    public void activate() {
        TwitterController.getInstance().setPhotoService( service );
        TwitterController.getInstance().commentMedia(media, filename);
    }

}
