package cn.nihility.http.entity;

public class HttpClientUser {

    private String name;
    private Integer age;

    public HttpClientUser() {
    }

    public HttpClientUser(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

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
        return "HttpClientUser{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
