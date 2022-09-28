package cn.nihility.local.mq.test;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author intel
 * @date 2022/09/27 16:35
 */
@RestController
public class MqTestController {

    private MqTestSenderService senderService;

    public MqTestController(MqTestSenderService senderService) {
        this.senderService = senderService;
    }

    @PostMapping("/mq/send")
    public Map<String, Object> send(@RequestBody Map<String, String> body) {
        senderService.send(body.get("exchange"), body.get("routingKey"), body.get("arg1"), body.get("arg2"), body);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "ok send");
        return result;
    }

    @PostMapping("/mq/send2")
    public Map<String, Object> send2(@RequestBody Map<String, String> body) {
        senderService.send2(body.get("exchange"), body.get("routingKey"), body.get("arg1"), body.get("arg2"), body.get("arg3"));
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "ok send2");
        return result;
    }
}
