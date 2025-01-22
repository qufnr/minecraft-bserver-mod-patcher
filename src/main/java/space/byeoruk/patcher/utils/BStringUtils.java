package space.byeoruk.patcher.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class BStringUtils {
    public static Map<String, Object> toMap(String str) {
        try {
            var om = new ObjectMapper();
            return om.readValue(str, new TypeReference<Map<String, Object>>() {});
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert string to Map object.");
        }
    }
}
