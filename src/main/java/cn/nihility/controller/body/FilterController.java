package cn.nihility.controller.body;

import cn.nihility.controller.result.ResponseResultBody;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/filter")
public class FilterController {

    @GetMapping("/hei")
    public Map<String, Object> hei() {
        Map<String, Object> map = new HashMap<>();
        map.put("requestBody", "1");
        map.put("requestEntity", "2");
        return map;
    }

    @GetMapping("/hei1")
    @ResponseResultBody
    public String hei1() {
        return "hei1";
    }

    @PostMapping("/hei")
    public Map<String, Object> hello(@RequestBody String requestBody, RequestEntity<String> requestEntity) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestBody", requestBody);
        map.put("requestEntity", requestEntity.getBody());
        return map;
    }

}
