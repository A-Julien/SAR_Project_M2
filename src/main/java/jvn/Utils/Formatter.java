package jvn.Utils;

import java.util.HashMap;
import java.util.Map;

public abstract class Formatter {
    private static Map<lvl, String> tags =  createMap();

    public static String log(lvl lvl, String message){
       return tags.get(lvl) + " " + message + " " + ConsoleColors.RESET;
    }

    private static Map<lvl, String> createMap(){
        Map<lvl, String> t = new HashMap<>();
        t.put(lvl.INFO,  "[" + ConsoleColors.GREEN +  lvl.INFO + ConsoleColors.RESET + "] " + ConsoleColors.GREEN_BOLD_BRIGHT);
        t.put(lvl.ERROR, "[" + ConsoleColors.RED +  lvl.ERROR + ConsoleColors.RESET + "] " + ConsoleColors.RED_BOLD_BRIGHT);
        t.put(lvl.DEBUG, "[" + ConsoleColors.YELLOW +  lvl.DEBUG + ConsoleColors.RESET + "] ");
        return t;
    }
}
