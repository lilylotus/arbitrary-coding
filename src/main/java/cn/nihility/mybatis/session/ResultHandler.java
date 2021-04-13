package cn.nihility.mybatis.session;

public interface ResultHandler<T> {

    void handleResult(ResultContext<? extends T> resultContext);

}
