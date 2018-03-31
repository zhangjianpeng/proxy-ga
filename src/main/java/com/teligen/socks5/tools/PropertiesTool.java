package com.teligen.socks5.tools;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class PropertiesTool {
    private static final Logger LOGGER=Logger.getLogger(PropertiesTool.class);
    private static Properties properties ;
    public static Properties getInstance()  {
        if (properties==null){
            synchronized (PropertiesTool.class){
                if(properties==null){
                    properties=new Properties();
                    try {
                        properties.load(PropertiesTool.class.getResourceAsStream("/config.properties"));
                    }catch (IOException e){
                        LOGGER.warn("load config.properties error.");
                    }
                }
            }
        }
        return properties;
    }
    public static String getString(String key){
       return getInstance().getProperty(key);
    }

}
