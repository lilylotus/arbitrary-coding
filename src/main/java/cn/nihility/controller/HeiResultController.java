package cn.nihility.controller;

import cn.nihility.controller.result.ResponseResultBody;
import cn.nihility.controller.result.ResultException;
import cn.nihility.controller.result.ResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/hei-result")
@ResponseResultBody
public class HeiResultController {

    private static final Map<String, Object> RESULT_DATA;

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

    @GetMapping("hello")
    public Map<String, Object> hello() {
        return RESULT_DATA;
    }

    @GetMapping("result")
    public ResultResponse<Map<String, Object>> helloResult() {
        return ResultResponse.success(RESULT_DATA);
    }

    @GetMapping("helloError")
    public HashMap<String, Object> helloError() throws Exception {
        throw new Exception("helloError");
    }

    @GetMapping("helloMyError")
    public HashMap<String, Object> helloMyError() throws Exception {
        throw new ResultException();
    }
}
