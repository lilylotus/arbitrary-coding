package cn.nihility.util.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JacksonUtils {

    static ObjectMapper mapper = new ObjectMapper();

    public static final TypeReference<List<String>> LIST_STRING_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    public static final TypeReference<Map<String, String>> MAP_STRING_TYPE_REFERENCE = new TypeReference<Map<String, String>>() {
    };

    public static final TypeReference<Map<String, Object>> MAP_OBJECT_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };

    public static final TypeReference<List<Map<String, String>>> LIST_MAP_STRING_TYPE_REFERENCE = new TypeReference<List<Map<String, String>>>() {
    };

    public static final TypeReference<List<Map<String, Object>>> LIST_MAP_OBJECT_TYPE_REFERENCE = new TypeReference<List<Map<String, Object>>>() {
    };

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private JacksonUtils() {
    }

    /**
     * Object to json string.
     *
     * @param obj obj
     * @return json string
     * @throws JacksonSerializationException if transfer failed
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JacksonSerializationException(obj.getClass(), e);
        }
    }

    /**
     * Object to json string byte array.
     *
     * @param obj obj
     * @return json string byte array
     * @throws JacksonSerializationException if transfer failed
     */
    public static byte[] toJsonBytes(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new JacksonSerializationException(obj.getClass(), e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json json string
     * @param cls  class of object
     * @param <T>  General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(byte[] json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (Exception e) {
            throw new JacksonSerializationException(cls, e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json json string
     * @param cls  {@link Type} of object
     * @param <T>  General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(byte[] json, Type cls) {
        try {
            return mapper.readValue(json, mapper.constructType(cls));
        } catch (Exception e) {
            throw new JacksonSerializationException(e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param inputStream json string input stream
     * @param cls         class of object
     * @param <T>         General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(InputStream inputStream, Class<T> cls) {
        try {
            return mapper.readValue(inputStream, cls);
        } catch (IOException e) {
            throw new JacksonSerializationException(e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param inputStream json string input stream
     * @param type        {@link Type} of object
     * @param <T>         General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(InputStream inputStream, Type type) {
        try {
            return mapper.readValue(inputStream, mapper.constructType(type));
        } catch (IOException e) {
            throw new JacksonSerializationException(e);
        }
    }

    public static <T> T toObj(InputStream inputStream, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new JacksonSerializationException(e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json          json string byte array
     * @param typeReference {@link TypeReference} of object
     * @param <T>           General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(byte[] json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new JacksonSerializationException(e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json json string
     * @param cls  class of object
     * @param <T>  General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (IOException e) {
            throw new JacksonSerializationException(cls, e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json json string
     * @param type {@link Type} of object
     * @param <T>  General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (IOException e) {
            throw new JacksonSerializationException(e);
        }
    }

    /**
     * Json string deserialize to Object.
     *
     * @param json          json string
     * @param typeReference {@link TypeReference} of object
     * @param <T>           General type
     * @return object
     * @throws JacksonSerializationException if deserialize failed
     */
    public static <T> T toObj(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new JacksonSerializationException(typeReference.getClass(), e);
        }
    }


    /**
     * convert origin value to specific type value
     *
     * @param fromObj origin object value
     * @param toType  to object type
     * @param <T>     specific type
     * @return specific type value
     */
    public static <T> T convert(Object fromObj, Class<T> toType) {
        try {
            return mapper.convertValue(fromObj, toType);
        } catch (IllegalArgumentException ex) {
            throw new JacksonSerializationException(toType, ex);
        }
    }

    public static <T> T convert(Object fromObj, TypeReference<T> toValueTypeRef) {
        try {
            return mapper.convertValue(fromObj, toValueTypeRef);
        } catch (IllegalArgumentException ex) {
            throw new JacksonSerializationException(toValueTypeRef.getClass(), ex);
        }
    }

    public static <T> T convert(Object fromObj, JavaType toValueType) {
        try {
            return mapper.convertValue(fromObj, toValueType);
        } catch (IllegalArgumentException ex) {
            throw new JacksonSerializationException(ex);
        }
    }

    /**
     * construct java type -> Jackson Java Type.
     *
     * @param type java type
     * @return JavaType {@link JavaType}
     */
    public static JavaType constructJavaType(Type type) {
        return mapper.constructType(type);
    }

    public static class JacksonSerializationException extends RuntimeException {

        private static final long serialVersionUID = -3146001029476303091L;

        private static final String DEFAULT_MSG = "Jackson serialize failed. ";

        private static final String MSG_FOR_SPECIFIED_CLASS = "Jackson serialize for class [%s] failed. ";

        private Class<?> serializedClass;

        public JacksonSerializationException() {
            super(DEFAULT_MSG);
        }

        public JacksonSerializationException(Class<?> serializedClass) {
            super(String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()));
            this.serializedClass = serializedClass;
        }

        public JacksonSerializationException(Throwable throwable) {
            super(DEFAULT_MSG, throwable);
        }

        public JacksonSerializationException(Class<?> serializedClass, Throwable throwable) {
            super(String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()), throwable);
            this.serializedClass = serializedClass;
        }

        public Class<?> getSerializedClass() {
            return serializedClass;
        }
    }

}
