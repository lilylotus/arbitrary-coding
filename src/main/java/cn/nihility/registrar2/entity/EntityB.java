package cn.nihility.registrar2.entity;

public class EntityB {

    private String name = "EntityB";

    public EntityB() {
        System.out.println("constructor b, hash [" + getClass().hashCode() + "]");
    }

    public EntityB(String name) {
        System.out.println("constructor b with name [" + name + "], hash [" + getClass().hashCode() + "]");
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
