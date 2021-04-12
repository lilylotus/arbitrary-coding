package cn.nihility.mybatis.pojo;

import java.time.LocalDateTime;
import java.util.List;

public class ReflectorObject {

    private String stringObject;
    private Integer integerObject;
    private Double doubleObject;
    private Long longObject;
    private LocalDateTime localDateTime;

    private List<String> stringList;

    private OtherReflectorObject otherReflectorObject;


    public String getStringObject() {
        return stringObject;
    }

    public void setStringObject(String stringObject) {
        this.stringObject = stringObject;
    }

    public Integer getIntegerObject() {
        return integerObject;
    }

    public void setIntegerObject(Integer integerObject) {
        this.integerObject = integerObject;
    }

    public Double getDoubleObject() {
        return doubleObject;
    }

    public void setDoubleObject(Double doubleObject) {
        this.doubleObject = doubleObject;
    }

    public Long getLongObject() {
        return longObject;
    }

    public void setLongObject(Long longObject) {
        this.longObject = longObject;
    }

    public OtherReflectorObject getOtherReflectorObject() {
        return otherReflectorObject;
    }

    public void setOtherReflectorObject(OtherReflectorObject otherReflectorObject) {
        this.otherReflectorObject = otherReflectorObject;
    }
}
