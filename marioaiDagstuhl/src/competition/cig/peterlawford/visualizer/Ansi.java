/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package competition.cig.peterlawford.visualizer;

import java.io.PrintStream;

/**
 * Ansi coloring support is provided by this class
 * 
 * @author Santhosh Kumar T
 */
public class Ansi{
    /**
     * specifies whether ansi is supported or not.
     * when this is false, it doesn't colorize given strings, rather than
     * simply returns the given strings
     *
     * It tries best effort to guess whether ansi is supported or not. But
     * you can override this value using system property "Ansi" (-DAnsi=true/false)
     */
    public static final boolean SUPPORTED = true;
    	// Boolean.valueOf("Ansi") || (OS.get().isUnix() && System.console()!=null);

    public enum Attribute{
        NORMAL(0), // Reset All Attributes (return to normal mode)
        BRIGHT(1), // Usually turns on BOLD
        DIM(2),
        UNDERLINE(4),
        BLINK(5),
        REVERSE(7),
        HIDDEN(8);

        private String value;

        private Attribute(int value){
            this.value = String.valueOf(value);
        }

        public String toString(){
            return ""+value;
        }
    }

    public enum Color{ BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE }

    private static final String PREFIX = "\u001b["; //NOI18N
    private static final String SUFFIX = "m";
    private static final String SEPARATOR = ";";
    private static final String END = PREFIX + SUFFIX;

    private String start = "";

    public Ansi(Attribute attr, Color foreground, Color background){
        StringBuilder buff = new StringBuilder();

        if(attr!=null)
            buff.append(attr);

        if(foreground!=null){
            if(buff.length()>0)
                buff.append(SEPARATOR);
            buff.append(30+foreground.ordinal());
        }
        if(background!=null){
            if(buff.length()>0)
                buff.append(SEPARATOR);
            buff.append(40+background.ordinal());
        }
        buff.insert(0, PREFIX);
        buff.append(SUFFIX);
        
        start = buff.toString();
    }

    public String colorize(String message){
        if(SUPPORTED){
            StringBuilder buff = new StringBuilder(message);
            buff.insert(0, start);
            buff.append(END);
            return buff.toString();
        }else
            return message;
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/
    
    public void print(PrintStream ps, String message){
        if(SUPPORTED)
            ps.print(start);
        ps.print(message);
        if(SUPPORTED)
            ps.print(END);
    }

    public void println(PrintStream ps, String message){
        print(ps, message);
        System.out.println();
    }

    public void format(PrintStream ps, String message, Object... args){
        if(SUPPORTED)
            ps.print(start);
        ps.format(message, args);
        if(SUPPORTED)
            ps.print(END);
    }

    /*-------------------------------------------------[ System.out ]---------------------------------------------------*/

    public void out(String message){
        print(System.out, message);
    }

    public void outln(String message){
        println(System.out, message);
    }

    public void outFormat(String message, Object... args){
        format(System.out, message, args);
    }

    /*-------------------------------------------------[ System.err ]---------------------------------------------------*/
    
    public void err(String message){
        print(System.err, message);
    }

    public void errln(String message){
        print(System.err, message);
    }

    public void errFormat(String message, Object... args){
        format(System.err, message, args);
    }
}
