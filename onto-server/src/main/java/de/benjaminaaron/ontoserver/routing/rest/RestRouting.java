package de.benjaminaaron.ontoserver.routing.rest;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.websocket.messages.CommandMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RestRouting extends BaseRouting {

    private final Logger logger = LogManager.getLogger(RestRouting.class);

    @RequestMapping(value = "/addStatement", method = POST)
    @ResponseBody
    public String addStatement(@RequestParam Map<String, String> params) {
        logger.info("addStatement via POST request received: " + params);
        return addStatementStringResponse(params.get("subject"), params.get("predicate"), params.get("object"),
                Boolean.parseBoolean(params.get("objectIsLiteral")));
    }

    @RequestMapping(value = "/command", method = POST)
    @ResponseBody
    public String command(@RequestParam Map<String, String> params) {
        logger.info("command POST request received with params: " + params);
        CommandMessage commandMessage = new CommandMessage();
        String commandStr = params.get("command") + " " + String.join(" ", params.get("args").split(","));
        commandMessage.setCommand(commandStr);
        String response = handleCommand(commandMessage);
        if (response == null) {
            return "Command received";
        }
        return "Command failed: " + response;
    }
}
