package cn.nihility.registrar2.entity;

public class SelectEntity {

    private String name = "Select_Entity_Name";

    public SelectEntity() {
    }

    public SelectEntity(String name) {
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
        return "name=[" + name + ']';
    }
}
