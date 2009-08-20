/*
 * RefreshService.java
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

package com.substanceofcode.twitter.services;

import com.substanceofcode.twitter.TwitterController;
import com.substanceofcode.twitter.model.Status;
import java.util.Vector;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.ToneControl;

/**
 *
 * @author Tommi Laukkanen
 */
public class RefreshService implements Runnable {

    private Thread refreshThread;
    private TwitterController controller;
    private static RefreshService instance;
    private boolean active;

    private RefreshService() {
        controller = TwitterController.getInstance();
        active = false;
        refreshThread = new Thread(this);
        refreshThread.start();
    }

    public static RefreshService getInstance() {
        if(instance==null) {
            instance = new RefreshService();
        }
        return instance;
    }
    
    public void activate() {
        active = true;
    }
    
    public void deactivate() {
        active = false;
    }

    public void run() {
        String lastStatusId = "";
        while(true) {
            try {
                Thread.yield();
                Thread.sleep(300000); // 5 min
                lastStatusId = getLastStatusId();
                if(active) {
                    controller.clearTimelines();
                    controller.showHomeTimeline();
                    String newLastStatusId = getLastStatusId();
                    if(lastStatusId.equals( newLastStatusId )==false && newLastStatusId.length()>0) {
                        try {
                            Manager.playTone(ToneControl.C4, 100, 80);
                        } catch (MediaException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getLastStatusId() {
        String lastStatusId = "";
        Vector statuses = controller.getRecentStatuses();
        if(statuses!=null && statuses.isEmpty()==false) {
            Status status = (Status)statuses.lastElement();
            lastStatusId = status.getId();
        }
        return lastStatusId;
    }

}
