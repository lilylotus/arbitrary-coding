package cn.nihility.selector2.entity;

public class SelectorEntity {

    private String name = "Init_Entity_Name";

    public SelectorEntity() {
    }

    public SelectorEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'';
    }
}
