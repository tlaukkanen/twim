/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.twitter;

import com.substanceofcode.twitter.model.Status;
import java.io.IOException;

/**
 *
 * @author tommi
 */
public interface PhotoService {

    public String getResponse();

    public Status sendPhoto(
            byte[] photo,
            String comment,
            String username,
            String password) throws IOException, Exception;

}
