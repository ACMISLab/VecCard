package org.ai4db.utils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class DingoLoggerUtils {
    public static Logger getSimpleLogger(String propertiesFilePath,String logFileName) throws IOException {
        Properties prop = DingoPropertiesUtils.getProperties(propertiesFilePath);
        Logger logger = Logger.getLogger("MyLogger");
        String  logFilePath = prop.getProperty("load.log.basePath")+ File.separator +logFileName+".log";
        FileHandler fileHandler = new FileHandler(logFilePath, true);
        logger.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        return logger;
    }
}
