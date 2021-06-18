package cn.nihility.util;

import cn.nihility.exception.IllegalParseException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonUtil {

    private static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE = "yyyy-MM-dd";
    private static final String TIME = "HH:mm:ss";
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME);
        mapper.setDateFormat(dateFormat);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        mapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        //mapper.enable(SerializationFeature.INDENT_OUTPUT);

        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE)));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME)));

        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME)));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE)));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME)));
        mapper.registerModule(timeModule);
    }

    public static byte[] serialize(Object obj) throws IllegalParseException {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }

    public static <T> T deserialize(byte[] src, Class<T> type) throws IllegalParseException {
        try {
            return mapper.readValue(src, type);
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }

    public static String objectToJson(Object obj) throws IllegalParseException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }

    public static <T> List<T> jsonToObjectList(String json, Class<T> elementType) throws IllegalParseException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, elementType);
        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }

    public static <T> T jsonToObjectHashMap(String json, Class<?> keyType, Class<?> valueType) throws IllegalParseException {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(HashMap.class, keyType, valueType);
        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            throw new IllegalParseException(e.getMessage(), e);
        }
    }


    public static void main(String[] args) {
        Entity et = new Entity("名称", 20, LocalTime.now(), LocalDate.now(), LocalDateTime.now());
        Entity et2 = new Entity("名称2", 220, LocalTime.now(), LocalDate.now(), LocalDateTime.now());

        List<Entity> el = new ArrayList<>();
        Map<String, Entity> em = new HashMap<>();

        el.add(et);
        el.add(et2);

        em.put("key1", et);
        em.put("key2", et2);

        String els = objectToJson(el);
        String ems = objectToJson(em);

        System.out.println(els);
        System.out.println(ems);

        List<Entity> entities = jsonToObjectList(els, Entity.class);
        System.out.println(entities);

        Map<String, Entity> etm = jsonToObjectHashMap(ems, String.class, Entity.class);
        System.out.println(etm);
    }

    public static class Entity implements Serializable {
        private static final long serialVersionUID = -549384058604046666L;
        String name;
        Integer age;
        LocalTime localTime;
        LocalDate localDate;
        LocalDateTime localDateTime;

        public Entity() {
        }

        public Entity(String name, Integer age, LocalTime localTime, LocalDate localDate, LocalDateTime localDateTime) {
            this.name = name;
            this.age = age;
            this.localTime = localTime;
            this.localDate = localDate;
            this.localDateTime = localDateTime;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", localTime=" + localTime +
                    ", localDate=" + localDate +
                    ", localDateTime=" + localDateTime +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }
    }

}
