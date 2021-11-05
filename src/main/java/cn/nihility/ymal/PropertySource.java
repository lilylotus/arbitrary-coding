package cn.nihility.ymal;

import java.util.Map;
import java.util.Objects;

public class PropertySource {

    private String name;
    private Map<String, Object> source;

    public PropertySource(String name, Map<String, Object> source) {
        this.name = name;
        this.source = source;
    }

    public Object getProperty(String key) {
        return source.get(key);
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySource that = (PropertySource) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "PropertySource{" +
                "name='" + name + '\'' +
                '}';
    }

}
