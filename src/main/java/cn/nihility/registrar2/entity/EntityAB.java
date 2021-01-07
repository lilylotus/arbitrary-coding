package cn.nihility.registrar2.entity;

public class EntityAB {

    private String name = "EntityAB";

    private EntityA entityA;
    private EntityB entityB;

    private Class<?> mapperInterface;

    public EntityAB(Class<?> mapperInterface) {
        System.out.println("constructor ab, class [" + mapperInterface.getName() + "], hash [" + getClass().hashCode() + "]");
        this.mapperInterface = mapperInterface;
    }

    public EntityAB() {
        System.out.println("constructor ab, hash [" + getClass().hashCode() + "]");
    }

    public EntityAB(String name) {
        System.out.println("constructor ab, name [" + name + "， hash [" + getClass().hashCode() + "]");
        this.name = name;
    }

    public EntityAB(EntityA entityA) {
        System.out.println("constructor ab, entityA [" + entityA + "], hash [" + getClass().hashCode() + "]");
        this.entityA = entityA;
    }

    public EntityAB(EntityB entityB) {
        System.out.println("constructor ab, entityB [" + entityB + "], hash [" + getClass().hashCode() + "]");
        this.entityB = entityB;
    }

    public EntityAB(EntityA entityA, EntityB entityB) {
        System.out.println("constructor ab, entityA [" + entityA + "], entityB [" + entityB + "], hash [" + getClass().hashCode() + "]");
        this.entityA = entityA;
        this.entityB = entityB;
    }

    public EntityAB(String name, EntityA entityA, EntityB entityB) {
        System.out.println("constructor ab, name [" + name + "], entityA [" + entityA + "], entityB [" + entityB + "], hash [" + getClass().hashCode() + "]");
        this.name = name;
        this.entityA = entityA;
        this.entityB = entityB;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityA getEntityA() {
        return entityA;
    }

    public void setEntityA(EntityA entityA) {
        this.entityA = entityA;
    }

    public EntityB getEntityB() {
        return entityB;
    }

    public void setEntityB(EntityB entityB) {
        this.entityB = entityB;
    }

    public Class<?> getMapperInterface() {
        System.out.println("getter interface [" + mapperInterface + "]");
        return mapperInterface;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        System.out.println("setter interface [" + mapperInterface + "]");
        this.mapperInterface = mapperInterface;
    }

    @Override
    public String toString() {
        return "EntityAB{" +
                "hash [" + getClass().hashCode() + "]," +
                "name='" + name + '\'' +
                ", entityA=" + entityA +
                ", entityB=" + entityB +
                '}';
    }
}
