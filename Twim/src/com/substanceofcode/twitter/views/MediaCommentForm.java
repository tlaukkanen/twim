/*
 * PhotoCommentForm.java
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

import com.substanceofcode.twitter.TwitterController;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * Comment form for photo
 * @author Tommi Laukkanen
 */
public class MediaCommentForm extends TextBox implements CommandListener {

    private Command sendCommand;
    private Command cancelCommand;
    private byte[] media;
    private String filename;

    public MediaCommentForm(byte[] media, String filename) {
        super("Comment","", 124, TextField.ANY);
        sendCommand = new Command("Send", Command.SCREEN, 1);
        addCommand(sendCommand);
        cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        addCommand(cancelCommand);
        setCommandListener(this);
        this.media = media;
        this.filename = filename;
    }

    public void commandAction(Command c, Displayable d) {
        TwitterController controller = TwitterController.getInstance();
        if(c==sendCommand) {
            String comment = this.getString();
            controller.sendMedia(comment, filename, media);
        } else if(c==cancelCommand) {
            controller.showTimeline();
        }
    }

}
