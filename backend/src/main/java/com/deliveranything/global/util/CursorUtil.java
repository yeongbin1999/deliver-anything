package com.deliveranything.global.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public final class CursorUtil {

    private static final String DELIMITER = "_";

    private CursorUtil() {
        // Prevent instantiation
    }

    public static String encode(Object... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        String rawCursor = Arrays.stream(keys)
            .map(String::valueOf)
            .collect(Collectors.joining(DELIMITER));
        return Base64.getEncoder().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static String[] decode(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return decoded.split(DELIMITER);
        } catch (IllegalArgumentException e) {
            // Handle invalid Base64 format
            return null;
        }
    }
}