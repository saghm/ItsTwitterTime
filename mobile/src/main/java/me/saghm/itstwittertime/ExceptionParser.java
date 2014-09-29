package me.saghm.itstwittertime;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by saghm on 9/20/14.
 */
public class ExceptionParser {
    public static String parse(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        e.printStackTrace(pw);

        return sw.toString();
    }
}
