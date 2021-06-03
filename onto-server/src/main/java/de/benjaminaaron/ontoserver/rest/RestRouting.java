package de.benjaminaaron.ontoserver.rest;

import de.benjaminaaron.ontoserver.jena.JenaController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RestRouting {

    @Autowired
    JenaController jenaController;

    @RequestMapping(value = "/addStatement", method = POST)
    @ResponseBody
    public String addStatement(@RequestParam Map<String, String> params) {
        System.out.println("POST request received with params: " + params);
        jenaController.addStatement(params.get("subject"), params.get("predicate"), params.get("object"));
        return "Statement received and added";
    }
}
