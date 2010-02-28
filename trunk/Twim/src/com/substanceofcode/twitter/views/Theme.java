/*
 * Theme.java
 *
 * Copyright (C) 2005-2008 Tommi Laukkanen
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

package com.substanceofcode.twitter.views;

import javax.microedition.lcdui.Font;

/**
 * Theme
 * 
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class Theme {

    private static int currentTheme = 0;

    public static final int THEME_DEFAULT = 0;
    public static final int THEME_UNORIGINAL = 1;
    public static final int THEME_NIGHT = 2;
    
    public static final int TWITTER_BLUE_COLOR = 0x9ae4e8;
    public static int COLOR_BACKGROUND = 0x9ae4e8;
    public static int COLOR_TEXT = 0x000000;
    public static int COLOR_TEXT_BG = 0xffffff;
    public static int COLOR_SELECTED_BG = 0xEEEEEE;
    public static int COLOR_USER_TEXT = 0x0000aa;

    public static final Font FONT_TEXT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

    /** Creates a new instance of Theme */
    private Theme() {
    }

    public static void setTheme(int theme) {
        currentTheme = theme;
        switch(currentTheme) {
            case(THEME_DEFAULT):
                COLOR_BACKGROUND = 0x9ae4e8;
                COLOR_TEXT = 0x000000;
                COLOR_TEXT_BG = 0xffffff;
                COLOR_SELECTED_BG = 0xEEEEEE;
                COLOR_USER_TEXT = 0x0000aa;
                StatusList.setTalkBalloon(new ComicTalkBalloon(10, 10));
                break;
            case(THEME_UNORIGINAL):
                COLOR_BACKGROUND = 0xdddddd;
                COLOR_TEXT = 0x000000;
                COLOR_TEXT_BG = 0xffffff;
                COLOR_SELECTED_BG = 0xEEEEEE;
                COLOR_USER_TEXT = 0x666699;
                StatusList.setTalkBalloon(new ListTalkBalloon(10, 10));
                break;
            case(THEME_NIGHT):
                COLOR_BACKGROUND = 0x333333;
                COLOR_TEXT = 0xffffff;
                COLOR_TEXT_BG = 0x555555;
                COLOR_SELECTED_BG = 0x444444;
                COLOR_USER_TEXT = 0xaaaaff;
                StatusList.setTalkBalloon(new ComicTalkBalloon(10, 10));
                break;
        }
    }

    public static int getTheme() {
        return currentTheme;
    }
    
}
