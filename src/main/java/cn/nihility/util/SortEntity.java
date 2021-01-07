package cn.nihility.util;

public class SortEntity<Value extends Comparable<Value>> implements Comparable<SortEntity<Value>> {

    private Integer index;
    private Value val;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Value getVal() {
        return val;
    }

    public void setVal(Value val) {
        this.val = val;
    }

    @Override
    public int compareTo(SortEntity<Value> o) {
        if (null == o) throw new NullPointerException("compareTo target can not be null");
        return val.compareTo(o.val);
    }

    @Override
    public String toString() {
        return index + ":" + val;
    }
}
