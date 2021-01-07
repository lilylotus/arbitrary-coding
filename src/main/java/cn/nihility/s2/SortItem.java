package cn.nihility.s2;

public class SortItem implements Comparable<SortItem> {

    private int index;
    private Integer value;

    public SortItem(int index, Integer value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int compareTo(SortItem o) {
        if (null == value) return -1;
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return "{" + index +
                "|" + value +
                '}';
    }
}
