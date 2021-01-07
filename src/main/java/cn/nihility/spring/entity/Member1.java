package cn.nihility.spring.entity;

public class Member1 implements IMember {
    @Override
    public void say() {
        System.out.println("Member1");
    }

    @Override
    public String toString() {
        return "Member1";
    }
}
