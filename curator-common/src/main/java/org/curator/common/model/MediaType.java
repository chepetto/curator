package org.curator.common.model;

import org.apache.commons.lang.StringUtils;

public enum MediaType {
    AUDIO, VIDEO, TEXT;

    public static MediaType fromText(String mediaType) {
        for(MediaType type:values()) {
            if(StringUtils.equalsIgnoreCase(type.name(), mediaType)) {
                return type;
            }
        }
        return null;
    }
}
