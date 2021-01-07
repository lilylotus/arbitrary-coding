package cn.nihility.exec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HelloWorld {
    public static void main(String[] args) throws JsonProcessingException {
        System.out.println("Hello World.");

        String json = "{\"query\":{\"ORG_ID\":\"4fc87fee06754be48cc253049c581e82\",\"exportKey\":\"{\\\"USER_NAME\\\":\\\"姓名\\\",\\\"IDCARD_NO\\\":\\\"身份证号\\\",\\\"MOBILE\\\":\\\"手机号\\\",\\\"ORG_NAME\\\":\\\"所属机构名称\\\",\\\"REMARK\\\":\\\"备注\\\",\\\"EMAIL\\\":\\\"邮箱\\\",\\\"SEX\\\":\\\"性别\\\"}\"}}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);

        JsonNode query = jsonNode.get("query");
        String orgId = query.get("ORG_ID").asText();
        System.out.println("ORG_ID: [" + orgId + "]");

        String exportKey = query.get("exportKey").asText();
        JsonNode exportNode = objectMapper.readTree(exportKey);
        String userName = exportNode.get("USER_NAME").asText();
        System.out.println("exportKey -> USER_NAME [" + userName + "]");

        System.out.println("system property [" + System.getProperty("G_VARB") + "]");
        System.out.println("env [" + System.getenv("G_VARB") + "]");
    }
}
