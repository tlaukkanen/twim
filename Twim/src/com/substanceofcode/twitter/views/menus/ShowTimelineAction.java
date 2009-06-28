/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.views.MenuAction;

/**
 *
 * @author Tommi Laukkanen
 */
public class ShowTimelineAction implements MenuAction {

    public void activate() {
        TwitterController.getInstance().showTimeline();
    }

}
