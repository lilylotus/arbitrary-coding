package cn.nihility.controller;

import cn.nihility.registrar.User;
import cn.nihility.registrar.mapper.UserMapper;
import cn.nihility.selector.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/bean")
    public Map<String, Object> bean() {

        Map<String, Object> result = new HashMap<>();

        if (userMapper != null) {
            User user = userMapper.selectUserById(1);
            result.put("data", user);
        }

        result.put("userService", userService.hashCode());

        result.put("code", 200);
        result.put("message", "success");

        return result;
    }

}
