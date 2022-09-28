package cn.nihility.local.mq.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地消息队列消息保存
 *
 * @author yuanzx
 * @date 2022/09/27 11:16
 */
@Getter
@Setter
@ToString
public class LocalMessageHolder implements Serializable {

    private static final long serialVersionUID = -6416404438308238349L;

    /**
     * 消息队列序号
     */
    private long sequence;

    /**
     * 交互机
     */
    private String exchange;

    /**
     * 消息队列的路由 key
     */
    private String routingKey;

    /**
     * 发送的消息实体
     */
    private Object message;

    /**
     * 消息 header 参数
     */
    private Map<String, Object> headers = new HashMap<>(8);

    /**
     * 消息拓展数据保存
     */
    private Map<String, Object> extensions = new HashMap<>(8);

    public void setHeader(String headerName, Object value) {
        this.headers.put(headerName, value);
    }

    /**
     * Typed getter for a header.
     *
     * @param headerName the header name.
     * @param <T>        the type.
     * @return the header value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getHeader(String headerName) {
        return (T) this.headers.get(headerName);
    }


    public void setExtension(String key, Object value) {
        this.extensions.put(key, value);
    }

    /**
     * Typed getter for a header.
     *
     * @param key the header name.
     * @param <T> the type.
     * @return the header value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key) {
        return (T) this.headers.get(key);
    }

}
