package org.curator.common.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

public class Configuration {

    private static final Logger _log = Logger.getLogger(Configuration.class);

    private static Configuration singleton = null;
    private PropertyResourceBundle rb;
    public static final String CONFIG_FILE_NAME = "curator.properties";
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String ARRAY_DELIMITER = ",";

    public static final String CONTEXT_SUFFIX_KEY = "environment";

    public static final String SERVICE_TWITTER_REQ_DELAY_MSEC = "service.twitter.request.delay";
    public static final String LIMIT_MAX_COMMENT_COUNT = "limit.upper.comment.count";
    public static final String HTTP_CLIENT_LIMIT_MAX_CONNECTIONS = "http.client.limit.max.connections";
    public static final String HTTP_CLIENT_SO_TIMEOUT = "http.client.socket.timeout";
    public static final String HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT = "http.client.connection.manager.timeout";
    public static final String REQUEST_DELAY = "curator.request-delay";

    public static final String REST_TIME_PATTERN = "curator.rest.time_pattern";
    public static final String VERSION = "curator.version";
    public static final String RETRIEVE_SEEDS = "retrieve.seeds";


    private static Configuration getInstance() {
        if (singleton == null) {
            singleton = new Configuration();
        }
        return singleton;
    }

    /**
     * Constructor
     */
    Configuration() {
        //use hardcoded name
        _reload();
        if (rb == null) {
            throw new RuntimeException("Application startup failed");
        }
    }

    private void _reload() {
        _log.info("Reading Application Configuration, '" + CONFIG_FILE_NAME + "'");
        _initConfigFile(CONFIG_FILE_NAME);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        _log.info("Configuration is shutting down");
    }

    /**
     * Resolves the config file path and name, and loads its values into a resource bundle.
     *
     * @param configFileName the configuration file name
     * @return true if the PropertyResourceBundle was created successfully
     */
    private boolean _initConfigFile(String configFileName) {
        String configFilePath = null;
        try {
            _log.info("Using Configuration File '" + configFileName + "'");
//            configFilePath = System.getProperty("jboss.server.config.url");
            configFilePath = System.getProperty("jboss.server.config.dir");
            if (StringUtils.isBlank(configFilePath)) {
                throw new Throwable("jboss.server.config.dir is not set");
            }
            if (!configFilePath.endsWith("/") && !configFilePath.endsWith("\\")) {
                configFilePath += File.separatorChar;
            }
            configFilePath = StringUtils.trim(configFilePath) + configFileName;
            InputStream is;
//            FileURLConnection file = new FileURLConnection(new URL(configFilePath));
//            is = file.getInputStream();
            is = new FileInputStream(configFilePath);
            rb = new PropertyResourceBundle(is);
            return true;
        } catch (FileNotFoundException fnf) {
            _log.fatal("The configuration file '" + configFilePath + "' does not exist!");
            return false;
        } catch (Throwable t) {
            _log.fatal("Failed to access the configuration file '" + configFilePath, t);
            return false;
        }
    }

    private boolean _getBooleanValue(String key) {
        String value = _getValueWithContext(key);
        return !(StringUtils.isBlank(value) || value.equalsIgnoreCase("false"));
    }


    private String _getStringValue(String key, String fallback) {
        String value = _getValueWithContext(key);
        return StringUtils.isBlank(value) ? value : fallback;
    }

    private String _getValue(String key, String... values) {
        if (rb == null) return null;

        String result = _getValueWithContext(key);
        if (values != null && values.length > 0) {
            result = MessageFormat.format(result, values);
        }
        return result;
    }

    private String _getValueWithContext(String key) {
        String contextKey = _buildContextKey(key);
        String result = null;

        if (contextKey != null) {
            result = _getValue(contextKey);
        }

        if (result == null) {
            result = _getValue(key);
        }

        return result;
    }

    private String _buildContextKey(String key) {
        String result = key;

        String suffix = _getValue(CONTEXT_SUFFIX_KEY);

        if (suffix != null) result += "." + suffix;

        return result;
    }

    private String _getValue(String key) {
        if (rb == null) return null;
        try {
            return rb.getString(key);
        } catch (MissingResourceException mre) {
            if (!CONTEXT_SUFFIX_KEY.equals(key)) {
                _log.warn("The requested key '" + key + "' is not defined in the application configuration");
            }
            return null;
        }
    }

    /**
     * This method returns the value from the property file assigned to the key.
     * This value can be populated by values in the same way it is defined for java.text.MessageFormat.format(String, Object...)
     * <p/>
     * It will use the version of property (value) asked from it whose key ends with .{environment},
     * if it can not find it or there is no environment property specified it will default to the one without the suffix.
     * Production keys should have no suffix and environment property should be set to no value.
     *
     * @param key    for which to return value
     * @param values used to populate the returning value
     * @return property value assigned to the key
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static String getValue(String key, String... values) {
        try {
            String value = getInstance()._getValue(key, values);
            if (value == null) value = "??" + key + "??";
            return value;
        } catch (Throwable t) {
            _log.error("Failed to obtain key='" + key + "', errMsg=" + t.getMessage());
            return "??" + key + "??";
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static List<String> getValues(String key, String... values) {
        ArrayList<String> result = new ArrayList<String>(1);
        try {
            String value = getInstance()._getValue(key, values);
            if (value == null) {
                value = "??" + key + "??";
                result.add(value);
            } else {
                String[] valuesz = StringUtils.split(value, ",");
                for (String v : valuesz) {
                    result.add(v.trim());
                }
            }
            return result;
        } catch (Throwable t) {
            _log.error("Failed to obtain key='" + key + "', errMsg=" + t.getMessage());
            result.add("??" + key + "??");
            return result;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean getBooleanValue(String key) {
        try {
            return getInstance()._getBooleanValue(key);
        } catch (Throwable t) {
            _log.error("Failed to obtain key='" + key + "', errMsg=" + t.getMessage());
            return false;
        }
    }

//    @SuppressWarnings({"UnusedDeclaration"})
//    public static String getStringValue(String key, String fallback) {
//        try {
//            return getInstance()._getStringValue(key, fallback);
//        } catch (Throwable t) {
//            _log.error("Failed to obtain key='" + key + "', errMsg=" + t.getMessage());
//            return fallback;
//        }
//    }


    @SuppressWarnings("UnusedDeclaration")
    public static void reload() {
        getInstance()._reload();
    }

    public static int getIntValue(String key, int defaultValue) {
        try {
            String value = getInstance()._getValue(key);
            if (StringUtils.isBlank(value) || value.startsWith("?")) return defaultValue;
            return Integer.valueOf(value);
        } catch (Throwable t) {
            _log.error("Failed to obtain integer value for key='" + key + "', errMsg=" + t.getMessage());
            return defaultValue;
        }
    }

    public static String getStringValue(String key, String defaultValue) {
        try {
            String value = getInstance()._getValue(key);
            if (StringUtils.isBlank(value) || value.startsWith("?")) {
                _log.warn("The requested key '" + key + "' is not defined in the application configuration, using default value '" + defaultValue + "'");
                return defaultValue;
            }
            return value;
        } catch (Throwable t) {
            _log.error("Failed to obtain value for key='" + key + "', errMsg=" + t.getMessage());
            return defaultValue;
        }
    }

    public static long getLongValue(String key, long defaultValue) {
        try {
            String value = getInstance()._getValue(key);
            if (StringUtils.isBlank(value) || value.startsWith("?")) return defaultValue;
            return Long.valueOf(value);
        } catch (Throwable t) {
            _log.error("Failed to obtain long value for key='" + key + "', errMsg=" + t.getMessage());
            return defaultValue;
        }
    }

    public static double getDoubleValue(String key, double defaultValue) {
        try {
            String value = getInstance()._getValue(key);
            if (StringUtils.isBlank(value) || value.startsWith("?")) return defaultValue;
            return Double.valueOf(value);
        } catch (Throwable t) {
            _log.error("Failed to obtain long value for key='" + key + "', errMsg=" + t.getMessage());
            return defaultValue;
        }
    }

    public static Calendar getFreshnessExpiry() {
        final Calendar expiry = Calendar.getInstance();
        // one month?
        expiry.roll(Calendar.MONTH, -2);
//    expiry.roll(Calendar.DAY_OF_MONTH, -10);
        return expiry;
    }

    public static Date getBestBeforeDate() {
        return getFreshnessExpiry().getTime();
    }

    public boolean _containsKey(String key) {
        return rb.containsKey(key);
    }

    public static boolean containsProperty(String value) {
        return getInstance()._containsKey(value);
    }
}