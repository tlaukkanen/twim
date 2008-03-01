/*
 * Copyright 2008 Tommi Laukkanen
 * http://www.substanceofcode.com
 */

package com.substanceofcode.utils;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
 */
public class TimeUtil {

    public static String getTimeInterval(Date fromDate) {
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();
        return getTimeInterval(fromDate, currentDate);
    }
    
    
    /** 
     * 
     * @param startDate Interval start date time
     * @param endDate Interval end date time
     * @return Time interval in format hh:mm:ss
     */
    public static String getTimeInterval(Date startDate, Date endDate) {
        long intervalSeconds = (endDate.getTime() - startDate.getTime()) / 1000;
        long hours = intervalSeconds / 3600;
        long minutes = (intervalSeconds % 3600) / 60;
        long days = hours / 24;
        
        if(days>1) {
            return String.valueOf(days) + " days";
        }        
        else if(hours>0) {
            if(hours==1) {
                return String.valueOf(hours) + " hour";
            } else {
                return String.valueOf(hours) + " hours";
            }
        } else {
            return String.valueOf(minutes) + " mins";
        }
    }    
    
}
