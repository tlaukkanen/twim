/*
 * VideoServiceChangeAction.java
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

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.VideoService;
import com.substanceofcode.twitter.views.MenuAction;

/**
 *
 * @author tommi
 */
public class VideoServiceChangeAction implements MenuAction {

    private VideoService service;
    private static byte[] commonVideo;
    private String filename;

    public VideoServiceChangeAction(VideoService service, byte[] video, String filename) {
        this.service = service;
        commonVideo = video;
        this.filename = filename;
    }

    public void activate() {
        TwitterController.getInstance().setVideoService( service );
        TwitterController.getInstance().commentMedia(commonVideo, filename);
    }

}
