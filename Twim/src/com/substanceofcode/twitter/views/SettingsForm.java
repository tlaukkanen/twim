/*
 * SettingsForm.java
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

package com.substanceofcode.twitter.views;

import com.substanceofcode.twitter.Settings;
import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.utils.Log;
import java.io.IOException;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

/**
 * SettingsForm for Twitter.
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class SettingsForm extends Form implements CommandListener {

    private TwitterController controller;
    private Command loginCommand;
    private Command exitCommand;
    private TextField usernameField;
    private TextField passwordField;
    private ChoiceGroup rememberValuesChoice;

    /**
     * Creates a new instance of SettingsForm
     * @param controller    Application controller.
     */
    public SettingsForm(TwitterController controller) {
        super("Settings");
        this.controller = controller;

        Settings settings = controller.getSettings();

        String username = settings.getStringProperty(Settings.USERNAME, "");
        usernameField = new TextField("Username", username, 32, TextField.ANY);
        append(usernameField);

        String password = settings.getStringProperty(Settings.PASSWORD, "");
        passwordField = new TextField("Password", password, 32, TextField.PASSWORD);
        append(passwordField);

        String[] labels = {"Save credentials", "Auto refresh", "Load tweets on startup", "Skip splash screen"};
        rememberValuesChoice = new ChoiceGroup("Options", ChoiceGroup.MULTIPLE, labels, null);
        boolean doRefresh = settings.getBooleanProperty(Settings.REFRESH, false);
        boolean loadOnStartup = settings.getBooleanProperty(Settings.LOAD_ON_STARTUP, false);
        boolean skipSplashScreen = settings.getBooleanProperty(Settings.SKIP_SPLASH_SCREEN, false);
        rememberValuesChoice.setSelectedFlags(new boolean[]{true, doRefresh, loadOnStartup, skipSplashScreen});
        append(rememberValuesChoice);

        loginCommand = new Command("Save", Command.ITEM, 1);
        this.addCommand(loginCommand);

        exitCommand = new Command("Exit", Command.EXIT, 2);
        this.addCommand(exitCommand);

        this.setCommandListener(this);
    }

    /**
     * Handle commands (Login/Logout)
     * @param cmd   Activated command.
     * @param disp  Displayable item.
     */
    public void commandAction(Command cmd, Displayable disp) {
        if (cmd == loginCommand) {
            String username = usernameField.getString();
            String password = passwordField.getString();
            Settings settings = controller.getSettings();
            boolean refresh = rememberValuesChoice.isSelected(1);
            settings.setBooleanProperty(Settings.REFRESH, refresh);
            boolean loadOnStartup = rememberValuesChoice.isSelected(2);
            settings.setBooleanProperty(Settings.LOAD_ON_STARTUP, loadOnStartup);
            boolean skipSplashScreen = rememberValuesChoice.isSelected(3);
            settings.setBooleanProperty(Settings.SKIP_SPLASH_SCREEN, skipSplashScreen);
            if (rememberValuesChoice.isSelected(0)) {
                /** Store username and password */
                Log.debug("Remember");
                settings.setStringProperty(Settings.USERNAME, username);
                settings.setStringProperty(Settings.PASSWORD, password);
            } else {
                /** Clear username and password */
                Log.debug("Clear");
                settings.setStringProperty(Settings.USERNAME, "");
                settings.setStringProperty(Settings.PASSWORD, "");
            }
            try {
                settings.save(true);
            } catch (IOException ex) {
                Log.error(ex.getMessage());
            } catch (RecordStoreException ex) {
                Log.error(ex.getMessage());
            }
            controller.login(username, password, loadOnStartup);
        } else if (cmd == exitCommand) {
            controller.exit();
        }
    }
}