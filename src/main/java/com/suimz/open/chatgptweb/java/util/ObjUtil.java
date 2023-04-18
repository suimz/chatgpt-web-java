package com.suimz.open.chatgptweb.java.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Object Tools
 *
 * @author https://github.com/suimz
 */
@Slf4j
@Component
public class ObjUtil  {

    public static String getNotBlankValSequential(String defaultValue, String ...strings) {
        for (String str : strings) {
            if (str != null && str.length() > 0) {
                return str;
            }
        }
        return defaultValue;
    }

    public static int getNotNullValSequential(int defaultValue, Integer ...objects) {
        for (Integer obj : objects) {
            if (obj != null) {
                return obj;
            }
        }
        return defaultValue;
    }

    public static String getNotNullValSequential(String defaultValue, Object ...objects) {
        for (Object obj : objects) {
            if (obj != null) {
                return String.valueOf(obj);
            }
        }
        return defaultValue;
    }

}
