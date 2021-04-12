package cn.nihility.mybatis.reflection.test;

import java.time.LocalDateTime;

public class ReflectorObjectExtend extends ReflectorObject {

    private String extendStringObject;
    private Integer extendIntegerObject;
    private Double extendDoubleObject;
    private Long extendLongObject;
    private LocalDateTime extendLocalDateTime;

    @Override
    public Integer getIntegerObject() {
        return super.getIntegerObject();
    }

    public String getExtendStringObject() {
        return extendStringObject;
    }

    public void setExtendStringObject(String extendStringObject) {
        this.extendStringObject = extendStringObject;
    }

    public Integer getExtendIntegerObject() {
        return extendIntegerObject;
    }

    public void setExtendIntegerObject(Integer extendIntegerObject) {
        this.extendIntegerObject = extendIntegerObject;
    }

    public Double getExtendDoubleObject() {
        return extendDoubleObject;
    }

    public void setExtendLongObject(Long extendLongObject) {
        this.extendLongObject = extendLongObject;
    }

    public LocalDateTime getExtendLocalDateTime() {
        return extendLocalDateTime;
    }

    public void setExtendLocalDateTime(LocalDateTime extendLocalDateTime) {
        this.extendLocalDateTime = extendLocalDateTime;
    }
}
