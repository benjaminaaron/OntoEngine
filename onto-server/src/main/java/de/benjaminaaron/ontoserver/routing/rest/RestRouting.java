package de.benjaminaaron.ontoserver.routing.rest;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RestRouting extends BaseRouting {

    @RequestMapping(value = "/addStatement", method = POST)
    @ResponseBody
    public String addStatement(@RequestParam Map<String, String> params) {
        System.out.println("POST request received with params: " + params);
        jenaController.addStatement(params.get("subject"), params.get("predicate"), params.get("object"));
        return "Statement received and added";
    }
}
