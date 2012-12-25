package org.curator.common.configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         11:01, 29.02.12
 */
@Interceptor
@CuratorInterceptors
public class ConfigurationInterceptor {

    private static final Logger _log = Logger.getLogger(Configuration.class);

    @AroundInvoke
    public Object injectConfigurationProperties(InvocationContext invocationContext) throws Exception {
        try {
            inject(invocationContext.getMethod().getDeclaringClass(), invocationContext.getTarget());
        } catch (Throwable t) {
            _log.fatal("Configuration for class " + invocationContext.getTarget().getClass().getName() + "failed: " + t.getMessage());
            throw new CuratorException(CuratorStatus.CONFIGURATION_ERROR, "Configuration for class " + invocationContext.getTarget().getClass().getName() + "failed: " + t.getMessage());
        }
        return invocationContext.proceed();
    }

    public static void inject(Class targetClass, Object targetObject) throws Exception {
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigurationProperty.class)) {
                ConfigurationProperty setting = field.getAnnotation(ConfigurationProperty.class);
                if (setting.mandatory() && !Configuration.containsProperty(setting.value()))
                    throw new Exception("Configuration property not found: " + setting.value());
                try {
                    field.setAccessible(true);
                    // Get the type(class) of generics (e.g. List<String> is String)
                    Class generic = null;
                    if (field.getGenericType() instanceof ParameterizedTypeImpl) {
                        ParameterizedTypeImpl pt = (ParameterizedTypeImpl) field.getGenericType();
                        generic = (Class) pt.getActualTypeArguments()[0];
                    }
                    // Get the value and set it
                    Object _value = _getValue(field.getType(), generic, Configuration.getValue(setting.value(), setting.defaultValue()));
                    if (field.getType().isEnum()) {
                        Object[] defined_values = (Object[]) field.getType().getDeclaredMethod("values").invoke(field);
                        for (Object t : defined_values) {
                            if (t.toString().equalsIgnoreCase((String) _value)) {
                                field.set(targetObject, t);
                                break;
                            }
                        }
                    } else {
                        field.set(targetObject, _value);
                    }
                } catch (Throwable e) {
                    throw new Exception("Field not accessible: " + setting.value());
                }
            }
        }
    }

    private static Object _getValue(Class theClass, Class generic, String value) {
        if (StringUtils.isEmpty(value)) return null;
        if (theClass.isPrimitive() && theClass.toString().equals("double")) {
            return Double.valueOf(value);
        } else if (theClass.isPrimitive() && theClass.toString().equals("long")) {
            return Long.valueOf(value);
        } else if (theClass.isPrimitive() && theClass.toString().equals("int")) {
            return Integer.valueOf(value);
        } else if (theClass.isPrimitive() && theClass.toString().equals("boolean")) {
            return Boolean.valueOf(value);
        } else if (theClass.equals(Date.class)) {
            try {
                try {
                    return new SimpleDateFormat(Configuration.DATE_FORMAT_PATTERN).parse(value);
                } catch (Throwable t) {
                    return null;
                }
            } catch (Throwable t) {
                _log.error("Failed to parse date: " + t.getMessage());
                t.printStackTrace();
                return null;
            }
        } else if (theClass.equals(Long.class)) {
            return Long.valueOf(value);
        } else if (theClass.equals(Integer.class)) {
            return Integer.valueOf(value);
        } else if (theClass.equals(Double.class)) {
            return Double.valueOf(value);
        } else if (theClass.equals(Float.class)) {
            return Float.valueOf(value);
        } else if (theClass.isArray()) {
            Class type = theClass.getComponentType();

            String[] values = value.split(Configuration.ARRAY_DELIMITER);
            Object arr = Array.newInstance(type, values.length);
            for (int pos = 0; pos < values.length; pos++) {
                Object val = _getValue(type, null, values[pos]);
                //noinspection RedundantCast
                ((Object[]) arr)[pos] = val;
            }
            return arr;
        } else if (theClass.equals(Set.class)) {
            if (generic == null) {
                _log.error("Conversion failed! Make sure the set has an generic type defined!");
                return null;
            } else {
                String[] values = value.split(Configuration.ARRAY_DELIMITER);
                HashSet<Object> set = new HashSet<Object>(values.length);
                for (String value1 : values) {
                    set.add(_getValue(generic, null, value1));
                }
                return set;
            }
        } else {
            return value;
        }
    }
}
