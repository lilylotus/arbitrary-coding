package cn.nihility.util;

import cn.nihility.model.JacksonModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

class UtilsTest {

    @Test
    void testJacksonUtil() throws JsonProcessingException {
        JacksonModel model = new JacksonModel();
        model.setId(UUID.randomUUID().toString().replace("-", ""));
        model.setName("ModelJacksonTestName");
        model.setAge(20);
        model.setDate(new Date());
        model.setLocalDate(LocalDate.now());
        model.setLocalDateTime(LocalDateTime.now());

        String modelString = JacksonUtils.toJsonString(model);
        Assertions.assertNotNull(modelString);
        System.out.println(modelString);

        JacksonModel m2 = JacksonUtils.readJsonString(modelString, JacksonModel.class);
        Assertions.assertNotNull(m2);
        System.out.println(m2);
    }

}
