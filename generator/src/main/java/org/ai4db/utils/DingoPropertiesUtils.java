package org.ai4db.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DingoPropertiesUtils {
    public static Properties getProperties(String path) throws IOException {
        Properties properties = new Properties();
        java.nio.file.Path candidate = Paths.get(path);
        InputStream inputStream;
        if (Files.exists(candidate)) {
            inputStream = Files.newInputStream(candidate);
        } else {
            inputStream = DingoPropertiesUtils.class.getClassLoader()
                    .getResourceAsStream(candidate.getFileName().toString());
        }
        if (inputStream == null) {
            candidate = Paths.get(PathUtils.resolvePath(path));
            inputStream = Files.newInputStream(candidate);
        }
        properties.load(inputStream);
        return properties;
    }

}
