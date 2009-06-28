/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.services.Mobypicture;
import com.substanceofcode.twitter.services.TwitPic;
import com.substanceofcode.twitter.services.Twitgoo;
import com.substanceofcode.twitter.services.YfrogService;
import com.substanceofcode.twitter.views.MenuAction;
import com.substanceofcode.twitter.views.MenuCanvas;

/**
 *
 * @author tommi
 */
public class PhotoServicesMenu extends MenuCanvas {

    private byte[] media;
    private String filename;

    public PhotoServicesMenu(byte[] media, String filename) {
        super("Photo services",
                new String[]{"Twitgoo", "TwitPic", "Mobypicture", "yFrog", "Cancel"},
                new MenuAction[]{
                    new PhotoServiceChangeAction( Twitgoo.getInstance(), media, filename ),
                    new PhotoServiceChangeAction( TwitPic.getInstance(), media, filename ),
                    new PhotoServiceChangeAction( Mobypicture.getInstance(), media, filename ),
                    new PhotoServiceChangeAction( YfrogService.getInstance(), media, filename ),
                    new ShowTimelineAction()
                }
            );
        this.media = media;
        this.filename = filename;
    }

}
