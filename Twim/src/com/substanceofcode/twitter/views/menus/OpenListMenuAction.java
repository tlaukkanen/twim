/*
 * OpenListMenuAction.java
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

package com.substanceofcode.twitter.views.menus;

import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.UserList;
import com.substanceofcode.twitter.views.MenuAction;

/**
 *
 * @author Tommi Laukkanen
 */
public class OpenListMenuAction implements MenuAction {

    private UserList userList;

    public OpenListMenuAction(UserList list) {
        this.userList = list;
    }

    public void activate() {
        TwitterController controller = TwitterController.getInstance();
        controller.showList(userList);
    }

}
