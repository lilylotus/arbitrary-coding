package cn.nihility.mybatis.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author orchid
 * @date 2021-04-12 22:01:11
 */
public class OtherReflectorObject {

    private String name;
    private int age;
    private List<String> stringList;
    private Map<String, String> stringMap = new HashMap<>();
    private List<ReflectorObject> reflectorObjectList = new ArrayList<>();

    public void add(ReflectorObject obj) {
        reflectorObjectList.add(obj);
    }

}
