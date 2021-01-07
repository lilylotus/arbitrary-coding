package cn.nihility.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/dist")
public class DistController {

    private static final Logger logger = LoggerFactory.getLogger(DistController.class);


    @PostMapping("/receive")
    public ModelAndView forward(@RequestBody String data, ModelAndView model, @ModelAttribute("data") String dataAttr) {
        logger.info("DistController -> data [{}]", data);

        model.getModelMap().addAttribute("data", data);
        model.setViewName("redirect:/forward/receive");

        return model;
    }

}
