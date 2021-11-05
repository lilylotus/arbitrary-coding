package cn.nihility.ymal;

import java.util.*;


public class PropertySources implements Iterable<PropertySource> {

    private List<PropertySource> propertySourcesList = new ArrayList<>();

    @Override
    public Iterator<PropertySource> iterator() {
        return this.propertySourcesList.iterator();
    }

    @Override
    public Spliterator<PropertySource> spliterator() {
        return Spliterators.spliterator(this.propertySourcesList, 0);
    }

    public boolean contains(final String name) {
        for (PropertySource source : propertySourcesList) {
            if (Objects.equals(name, source.getName())) {
                return true;
            }
        }
        return false;
    }

    public void addFirst(PropertySource propertySource) {
        removeIfPresent(propertySource);
        propertySourcesList.add(0, propertySource);
    }

    public void addLast(PropertySource propertySource) {
        removeIfPresent(propertySource);
        propertySourcesList.add(propertySource);
    }

    private void removeIfPresent(PropertySource propertySource) {
        this.propertySourcesList.remove(propertySource);
    }

}
