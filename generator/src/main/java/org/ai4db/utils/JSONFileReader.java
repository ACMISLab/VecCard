package org.ai4db.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class JSONFileReader {
    public static JSONObject getJSONObejct(String filePath){
       JSONObject jsonObject = new JSONObject();
        // 使用 BufferedReader 读取 JSON 文件内容
        String resolvedPath = PathUtils.resolvePath(filePath);
        if (!Files.exists(Paths.get(resolvedPath))) {
            return jsonObject;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(resolvedPath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            // 将 JSON 字符串解析为 JSONObject
            jsonObject = JSON.parseObject(jsonContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
       return jsonObject;
    }
}
