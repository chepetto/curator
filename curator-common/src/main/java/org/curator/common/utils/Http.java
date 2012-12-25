package org.curator.common.utils;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         10:39, 23.07.12
 */
public class Http {
    
    public static DefaultHttpClient getHttpClient() throws Exception {
        return new DefaultHttpClient();
    }

    public static String getContent(HttpResponse response) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String line;
        while ((line = rd.readLine()) != null) {//process the line response}
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
