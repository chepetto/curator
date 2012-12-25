package org.curator.common.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern TRIM_PATTERN = Pattern.compile("^[\\W \n\t]+|[\\W \n\t]+$");

    public static String trim(String str) {

        if(StringUtils.isBlank(str)) {
            return null;

        } else {
            Matcher m = TRIM_PATTERN.matcher(str);
            StringBuffer sb = new StringBuffer(50);
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }
}
