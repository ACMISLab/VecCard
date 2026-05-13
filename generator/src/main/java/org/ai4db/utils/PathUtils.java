package org.ai4db.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    private static final Path WORKING_DIR = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

    private static Path dataRoot() {
        String dataDir = System.getenv("VECCARD_DATA_DIR");
        if (dataDir == null || dataDir.isBlank()) {
            return WORKING_DIR.resolve("data").normalize();
        }
        return Paths.get(dataDir).toAbsolutePath().normalize();
    }

    public static String resolvePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return rawPath;
        }

        String expanded = rawPath
                .replace("${VECCARD_DATA_DIR}", dataRoot().toString())
                .replace("$VECCARD_DATA_DIR", dataRoot().toString());

        Path direct = Paths.get(expanded);
        if (direct.isAbsolute() || Files.exists(direct)) {
            return direct.toString();
        }

        String normalized = expanded.replace('\\', '/');
        int lceIndex = normalized.indexOf("LCE/");
        if (lceIndex >= 0) {
            normalized = normalized.substring(lceIndex + "LCE/".length());
        } else {
            int driveIndex = normalized.indexOf(":/");
            if (driveIndex >= 0) {
                normalized = normalized.substring(driveIndex + 2);
            }
        }

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("data/")) {
            normalized = normalized.substring("data/".length());
        }
        return dataRoot().resolve(normalized).normalize().toString();
    }
}
