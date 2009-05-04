/*
 * FileBrowserCanvas.java
 *
 * Copyright (C) 2008-2009 Tommi Laukkanen
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
import com.substanceofcode.twitter.model.FileSelect;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Tommi Laukkanen
 */
public class FileBrowserCanvas extends Canvas implements FileSystemListener {

    private int width, height;
    private Menu fileMenu;
    private String folder;
    private Vector rootFolders;
    private boolean showRoot;
    private FileSelect fileSelect;


    public FileBrowserCanvas(FileSelect select) {
        setFullScreenMode(true);
        width = getWidth();
        height = getHeight();
        folder = "";
        this.fileSelect = select;
        fileMenu = new Menu(null, width, height);
        FileSystemRegistry.addFileSystemListener(this);
        rootFolders = new Vector();
    }

    public void showRoots() {
        Enumeration roots = FileSystemRegistry.listRoots();
        rootFolders.removeAllElements();
        while(roots.hasMoreElements()) {
            String root = (String)roots.nextElement();
            rootFolders.addElement(root);
        }
        showRoot = true;
        String[] folders = new String[ rootFolders.size() ];
        for(int i=0; i<rootFolders.size(); i++) {
            folders[i] = (String)rootFolders.elementAt(i);
        }
        fileMenu.setLabels(folders);
        fileMenu.setTitle("Select device");
    }

    protected void paint(Graphics g) {
        width = getWidth();
        height = getHeight();

        /** Background */
        g.setColor(Theme.TWITTER_BLUE_COLOR);
        g.fillRect(0, 0, width, height);

        /** Draw the file directory */
        fileMenu.draw(g);
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        switch(gameAction) {
            case(Canvas.UP):
                fileMenu.selectPrevious();
                repaint();
                break;
            case(Canvas.DOWN):
                fileMenu.selectNext();
                repaint();
                break;
            case(Canvas.FIRE):
                selectFileEntry();
                break;
            default:
                repaint();
                break;
        }
    }

    public void rootChanged(int state, String rootName) {
        if(state==FileSystemListener.ROOT_ADDED) {

        } else if(state==FileSystemListener.ROOT_REMOVED) {
            
        }
    }

    private void browseToDirectory(FileConnection fc) throws IOException {
        Enumeration items = fc.list("*.jpg", false);
        rootFolders.removeAllElements();
        while(items.hasMoreElements()) {
            String path = (String)items.nextElement();
            rootFolders.addElement(path);
        }
        showRoot = false;
        String[] folders = new String[ rootFolders.size() ];
        for(int i=0; i<rootFolders.size(); i++) {
            folders[i] = (String)rootFolders.elementAt(i);
        }
        fileMenu.setLabels(folders);
        fileMenu.setTitle("Select photo");
    }

    private void selectFileEntry() {
        try {
            String label = fileMenu.getSelectedLabel();
            if (label == null) {
                return;
            }
            FileConnection fc = (FileConnection) Connector.open("file:///" + label);
            if(fc.isDirectory()) {
                /** move to directory */
                browseToDirectory(fc);
                repaint();
            } else {
                /** Select file */
                this.fileSelect.select(fc.getURL());
            }
        } catch (IOException ex) {
            TwitterController.getInstance().showError("Error: " + ex.toString()
                    + " " + ex.getMessage());
        }
    }

}
