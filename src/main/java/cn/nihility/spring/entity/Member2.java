package cn.nihility.spring.entity;

public class Member2 implements IMember {
    @Override
    public void say() {
        System.out.println("Member2");
    }

    @Override
    public String toString() {
        return "Member2";
    }
}
