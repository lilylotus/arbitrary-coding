package cn.nihility.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/advice")
public class BodyAdviceController {

    @PostMapping("/post")
    public Map<String, Object> postAdvice(@RequestBody Map<String, Object> entity) {

        Map<String, Object> result = new HashMap<>();

        result.put("data", entity);
        result.put("msg", "request success");
        result.put("code", "200");

        return result;

    }

}
