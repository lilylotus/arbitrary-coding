package cn.nihility.entity;

import java.util.List;
import java.util.Map;

public class QueryParams {

    private Map<String, String> query;
    private List<String> listParam;
    private QueryPage page;
    private String uiListId;
    private Boolean enable;

    public QueryParams() {
    }

    public QueryParams(Map<String, String> query, List<String> listParam, QueryPage page, String uiListId, Boolean enable) {
        this.query = query;
        this.listParam = listParam;
        this.page = page;
        this.uiListId = uiListId;
        this.enable = enable;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public QueryPage getPage() {
        return page;
    }

    public void setPage(QueryPage page) {
        this.page = page;
    }

    public String getUiListId() {
        return uiListId;
    }

    public void setUiListId(String uiListId) {
        this.uiListId = uiListId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public List<String> getListParam() {
        return listParam;
    }

    public void setListParam(List<String> listParam) {
        this.listParam = listParam;
    }

    @Override
    public String toString() {
        return "QueryParams{" +
                "query=" + query +
                ", listParam=" + listParam +
                ", page=" + page +
                ", uiListId='" + uiListId + '\'' +
                ", enable=" + enable +
                '}';
    }
}
