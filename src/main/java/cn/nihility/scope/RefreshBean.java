package cn.nihility.scope;

public class RefreshBean {

    private String name;

    public RefreshBean() {
    }

    public RefreshBean(String name) {
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
        return "RefreshBean{" +
            "name='" + name + "\'," +
            "hashCode='" + hashCode() + '\'' +
            '}';
    }
}
