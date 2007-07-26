// Copyright (c) 2007, Carlo Teubner

public class Log {    
    public static void log(Object msg) {
        if (BuildOptions.LOG_ENABLED) {
            System.out.println(msg);
        }
    }
    
    public static void log(double d) {
        log(Double.toString(d));
    }
}
