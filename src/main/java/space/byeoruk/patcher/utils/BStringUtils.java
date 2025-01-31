package space.byeoruk.patcher.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

public class BStringUtils {
    public static boolean isEmptyOrBlank(String str) {
        return StringUtils.isEmpty(str) || StringUtils.isBlank(str);
    }

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
