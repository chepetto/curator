package org.curator.common.service;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.curator.common.configuration.Configuration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         10:28, 28.03.12
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Configuration.getStringValue(Configuration.REST_TIME_PATTERN, "yyyy-MM-dd HH:mm"));

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            return simpleDateFormat.parse(jsonParser.getText());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}
