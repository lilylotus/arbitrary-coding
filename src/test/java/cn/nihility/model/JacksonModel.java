package cn.nihility.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JacksonModel implements Serializable {

    private static final long serialVersionUID = 42L;

    private String id;
    private String name;
    private int age;

    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "JacksonModel{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", age=" + age +
            ", localDate=" + localDate +
            ", localDateTime=" + localDateTime +
            ", date=" + date +
            '}';
    }
}
