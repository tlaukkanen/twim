/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.substanceofcode.utils;

import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Tommi Laukkanen
 */
public class VectorUtil {

    /**
     * Convert Vector of strings to string array.
     * @param strings
     * @return
     */
    public static String[] convertToStringArray(Vector strings) {
        Enumeration stringEnum = strings.elements();
        String[] array = new String[ strings.size() ];
        int i=0;
        while(stringEnum.hasMoreElements()) {
            array[i] = (String) stringEnum.nextElement();
            i++;
        }
        return array;
    }

}
