package cn.nihility.spring.entity;

import java.util.List;

public class DepartmentEntity {

    private PersonEntity major;
    private List<IMember> members;

    public DepartmentEntity(PersonEntity major, List<IMember> members) {
        this.major = major;
        this.members = members;
    }

    public PersonEntity getMajor() {
        return major;
    }

    public void setMajor(PersonEntity major) {
        this.major = major;
    }

    public List<IMember> getMembers() {
        return members;
    }

    public void setMembers(List<IMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "DepartmentEntity{" +
                "major=" + major +
                ", members=" + members +
                '}';
    }
}
