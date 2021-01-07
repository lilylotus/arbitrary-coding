package cn.nihility.entity;

public class BodyAdviceEntity {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "BodyAdviceEntity{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
