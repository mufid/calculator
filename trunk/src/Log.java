// Copyright (c) 2007, Carlo Teubner

public class Log {
    
    public static void log(Object msg) {
        // Comment this out to disable all logging
        System.out.println(msg);
    }
    
    public static void log(double d) {
        log(Double.toString(d));
    }
}
