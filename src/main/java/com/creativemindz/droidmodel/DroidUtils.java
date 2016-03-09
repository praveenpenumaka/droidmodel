package com.creativemindz.droidmodel;

/**
 * Created by praveen on 19/02/16.
 */
public class DroidUtils {
    public static boolean isStringValid(String string){
        if(string == null){ return false;}
        if( string.isEmpty() ){ return false;}
        if( "".equals(string) ){ return false;}
        return true;
    }

    public static String enclose(String value) {
        return " '"+value+"' ";
    }
}
