package org.curator.common.service;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.curator.common.configuration.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         09:52, 28.03.12
 */
public class AccurateDateSerializer extends JsonSerializer<Date> {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Configuration.getValue(Configuration.REST_TIME_PATTERN, "yyyy-MM-dd HH:mm.SSS"));

    @Override
    public void serialize(Date date, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(simpleDateFormat.format(date));
    }
}
