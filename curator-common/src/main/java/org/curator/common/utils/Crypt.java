package org.curator.common.utils;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         00:31, 21.07.12
 */
public class Crypt {

    private static final Logger _log = Logger.getLogger(Crypt.class);


    public static String crypt(String str) throws CuratorException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(str.getBytes("UTF-8"));

            byte raw[] = messageDigest.digest();
            return (new BASE64Encoder().encode(raw));
        } catch (Throwable t) {
            CuratorException exception = new CuratorException(CuratorStatus.CRYPT_ERROR, t);
            _log.error(exception);
            throw exception;
        }
    }
}
