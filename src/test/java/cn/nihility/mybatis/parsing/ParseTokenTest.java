package cn.nihility.mybatis.parsing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParseTokenTest {

    @Test
    public void testTokenParse() {
        String context = "SELECT * FROM table WHERE ID = ${id} AND name = ${name}";
        String parseContext = "SELECT * FROM table WHERE ID = param0 AND name = param1";
        GenericTokenParser tokenParser = new GenericTokenParser("${", "}");
        String parse = tokenParser.parse(context);

        Assertions.assertEquals(parseContext, parse);

    }

}
