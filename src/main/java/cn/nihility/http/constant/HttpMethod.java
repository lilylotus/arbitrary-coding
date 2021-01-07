package cn.nihility.http.constant;

/**
 * HTTP 请求方法
 */
public enum HttpMethod {

    /**
     * 求获取 Request URI 所标识的资源
     * SELECT 对应数据库操作 SELECT
     */
    GET(0, "GET"),


    /**
     * 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据被包含在请求体中。
     * POST 请求可能会导致新的资源的建立和/或已有资源的修改
     * REST ful 类型 CREATE 对应数据库的 INSERT
     */
    POST(1, "POST"),

    /**
     * 向指定资源位置上传其最新内容（全部更新，操作幂等）
     * 在服务器更新资源（客户端提供改变后的完整资源）
     * 数据库 UPDATE
     */
    PUT(2, "PUT"),

    /**
     * 向服务器索要与GET请求相一致的响应，只不过响应体将不会被返回。
     * 这一方法可以在不必传输整个响应内容的情况下，就可以获取包含在响应消息头中的元信息
     * 只获取响应信息报头
     */
    HEAD(3, "HEAD"),

    /**
     * 请求服务器删除 Request-URI 所标识的资源
     */
    DELETE(4, "DELETE"),

    /**
     * 请求服务器回送收到的请求信息，主要用于测试或诊断
     */
    TRACE(5, "TRACE"),

    /**
     * 向指定资源位置上传其最新内容（部分更新，非幂等）
     * 在服务器更新资源（客户端提供改变的属性）
     * UPDATE 操作
     */
    PATCH(6, "PATCH"),

    /**
     * 返回服务器针对特定资源所支持的 HTTP 请求方法。
     * 也可以利用向 Web 服务器发送 '*' 的请求来测试服务器的功能性
     */
    OPTIONS(7, "OPTIONS");

    /* 方法编码 */
    private int code;
    /* 方法名称 */
    private String name;

    HttpMethod(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
