package jvn.RmiServices;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

/**
 * This manager to get configuration from a json file
 */
public abstract class ConfigManager {
    private static String pathConfigFile = "src/main/java/jvn/RmiServices/config.json";

    /**
     * Allows server and services to get config from the config.json file
     * @param conf the key in String
     * @return return the value link to the key
     */
    public static String getConfig(String conf){
         JSONParser parser = new JSONParser();

        Object config = null;
        try {
            config = parser.parse(new FileReader(pathConfigFile));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        JSONObject jsonConfig = (JSONObject) config;

        assert jsonConfig != null;
        return (String) jsonConfig.get(conf);
    }
    /**
     * Build a rmi address for a distant object based on
     * a String name and String address
     * @param name name of the distant object
     * @param address address of the rmi server
     * @return
     */
    public synchronized static String buildRmiAddr(String name, String address, int port){
        return "rmi://" + address + ":" + port + "/"+ name;
    }
}
