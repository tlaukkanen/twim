/*
 * VideoServicesMenu
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

import com.substanceofcode.twitter.services.Mobypicture;
import com.substanceofcode.twitter.services.Twitvid;
import com.substanceofcode.twitter.services.YfrogService;
import com.substanceofcode.twitter.views.MenuAction;
import com.substanceofcode.twitter.views.MenuCanvas;

/**
 *
 * @author tommi
 */
public class VideoServicesMenu extends MenuCanvas {

    public VideoServicesMenu(byte[] media, String filename) {
        super("Video services",
                new String[]{"Mobypicture", "Twitvid", "yFrog", "Cancel"},
                new MenuAction[]{
                    new VideoServiceChangeAction( Mobypicture.getInstance(), media, filename ),
                    new VideoServiceChangeAction( Twitvid.getInstance(), media, filename ),
                    new VideoServiceChangeAction( YfrogService.getInstance(), media, filename ),
                    new ShowTimelineAction()
                }
            );
    }

}
