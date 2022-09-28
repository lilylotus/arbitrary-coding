package cn.nihility.local.mq.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author intel
 * @date 2022/09/27 16:33
 */
@Service
@Slf4j
public class MqTestReceiveService {

    public void receive(String exchange, String routingKey, String arg1, String arg2, Map<String, String> body) {
        log.info("receive [{}:{}:{}:{}]", exchange, routingKey, arg1, arg2);
        log.info("body [{}]", body);
    }

    public void receive2(String exchange, String routingKey, String arg1, String arg2, String arg3) {
        log.info("receive2 [{}:{}:{}:{}:{}]", exchange, routingKey, arg1, arg2, arg3);
    }

}
