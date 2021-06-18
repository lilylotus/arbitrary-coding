package cn.nihility.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forward")
public class ForwardController {

    private static final Logger logger = LoggerFactory.getLogger(ForwardController.class);

    @PostMapping("/receive")
    public String forward(Model model) {

        String data = (String) model.getAttribute("data");
        logger.info("ForwardController -> data [{}]", data);

        return data;
    }


}
