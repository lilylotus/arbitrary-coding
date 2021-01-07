package cn.nihility.entity;

public class QueryPage {

    private Boolean enablePage;
    private Integer page;
    private Integer size;

    public Boolean getEnablePage() {
        return enablePage;
    }

    public void setEnablePage(Boolean enablePage) {
        this.enablePage = enablePage;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "QueryPage{" +
                "enablePage=" + enablePage +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
