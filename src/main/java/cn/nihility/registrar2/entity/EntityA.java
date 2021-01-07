package cn.nihility.registrar2.entity;

public class EntityA {

    private String name = "EntityA";

    public EntityA() {
        System.out.println("constructor a");
    }

    public EntityA(String name) {
        System.out.println("constructor b with name [" + name + "]");
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
        return "name=[" + name + "] hash [" + getClass().hashCode() + "]";
    }

}
