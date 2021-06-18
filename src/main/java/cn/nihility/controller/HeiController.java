package cn.nihility.controller;

import cn.nihility.controller.result.ResponseResultBody;
import cn.nihility.controller.result.ResultResponse;
import cn.nihility.entity.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/hei")
public class HeiController {

    private static final Map<String, Object> RESULT_DATA;
    private static final Logger log = LoggerFactory.getLogger(HeiController.class);

    static {
        RESULT_DATA = new HashMap<>();
        RESULT_DATA.put("status", 200);
        RESULT_DATA.put("message", "自定义 Hei 信息");
        RESULT_DATA.put("name", "anonymous");
        try {
            RESULT_DATA.put("remote", Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/headers")
    public Map<String, String> headers(@RequestHeader Map<String, String> headers) {
        Map<String, String> ret = new HashMap<>();
        log.info("-------- /headers start");
        if (headers != null && headers.size() > 0) {
            headers.forEach(ret::put);
            headers.forEach(this::recordLog);
        }
        ret.put("message", "请求成功");
        ret.put("url", "/headers");
        log.info("-------- /headers end");
        return ret;
    }

    @RequestMapping("/multi-headers")
    public Map<String, String> multiHeaders(@RequestHeader MultiValueMap<String, String> headers) {
        final Map<String, String> ret = new HashMap<>();
        log.info("-------- /multi-headers start");
        if (headers != null && headers.size() > 0) {
            headers.forEach((k, v) -> {
                String value = String.join("/", v);
                ret.put(k, value);
                recordLog(k, value);
            });
        }
        ret.put("message", "请求成功");
        ret.put("url", "/multi-headers");
        log.info("-------- /multi-headers end");
        return ret;
    }

    @PostMapping("/multi-headers")
    public Map<String, String> multiHeadersPost(@RequestHeader MultiValueMap<String, String> headers) {
        final Map<String, String> ret = new HashMap<>();
        log.info("-------- post /multi-headers start");
        if (headers != null && headers.size() > 0) {
            headers.forEach((k, v) -> {
                String value = String.join("/", v);
                ret.put(k, value);
                recordLog(k, value);
            });
        }
        ret.put("message", "请求成功");
        ret.put("url", "post /multi-headers");
        log.info("-------- post /multi-headers end");
        return ret;
    }

    @GetMapping("/httpHeaders")
    public Map<String, String> httpHeaders(@RequestHeader HttpHeaders headers) {
        final Map<String, String> ret = new HashMap<>();
        log.info("-------- /httpHeaders start");
        final InetSocketAddress host = headers.getHost();
        assert host != null;
        String url = "http://" + host.getHostName() + ":" + host.getPort();
        log.info("httpHeaders request url [{}]", url);

        final Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
        if (entries.size() > 0) {
            entries.forEach(e -> {
                String value = String.join("/", e.getValue());
                ret.put(e.getKey(), value);
                recordLog(e.getKey(), value);
            });
        }
        ret.put("message", "请求成功");
        ret.put("url", url);
        log.info("-------- /httpHeaders end");
        return ret;
    }

    private void recordLog(Object key, Object value) {
        log.info("key [{}] : Value [{}]", key, value);
    }

    @RequestMapping("/hello")
    public Map<String, Object> hello(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Success");
        result.put("remote", request.getRemoteAddr());
        return result;
    }

    @RequestMapping("/param")
    public Map<String, Object> requestParam(QueryParams params) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Success");
        result.put("data", params);

        System.out.println(params);

        return result;
    }

    @RequestMapping("/data")
    public Map<String, Object> data() {
        return RESULT_DATA;
    }

    @RequestMapping("/result")
    @ResponseBody
    public ResultResponse<Map<String, Object>> result() {
        return ResultResponse.success(RESULT_DATA);
    }

    @RequestMapping("/resultBody")
    @ResponseResultBody
    public Map<String, Object> resultBody() {
        return RESULT_DATA;
    }

}
