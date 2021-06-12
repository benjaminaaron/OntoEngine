package de.benjaminaaron.ontoserver.routing.rest;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
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
        System.out.println("addStatement via POST request received: " + params);
        return addStatementStringResponse(params.get("subject"), params.get("predicate"), params.get("object"),
                Boolean.parseBoolean(params.get("objectIsLiteral")));
    }

    @RequestMapping(value = "/command", method = POST)
    @ResponseBody
    public String command(@RequestParam Map<String, String> params) {
        System.out.println("command POST request received with params: " + params);
        CommandMessage command = new CommandMessage();
        String commandStr = params.get("command") + " " + String.join(" ", params.get("args").split(","));
        command.setCommand(commandStr);
        handleCommand(command);
        return "Command received";
    }
}
