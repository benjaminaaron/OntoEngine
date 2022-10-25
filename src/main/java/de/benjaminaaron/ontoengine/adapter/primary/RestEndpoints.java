package de.benjaminaaron.ontoengine.adapter.primary;

import de.benjaminaaron.ontoengine.adapter.primary.messages.CommandMessage;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class RestEndpoints {

    private final Logger logger = LogManager.getLogger(RestEndpoints.class);

    @Autowired
    private BaseRouting baseRouting;

    @RequestMapping(value = "/addStatement", method = POST)
    @ResponseBody
    public String addStatement(@RequestParam Map<String, String> params) {
        logger.info("addStatement via POST request received: " + params);
        return baseRouting.addStatementStringResponse(params.get("subject"), params.get("predicate"), params.get("object"),
                Boolean.parseBoolean(params.get("objectIsLiteral")));
    }

    @RequestMapping(value = "/command", method = POST)
    @ResponseBody
    public String command(@RequestParam Map<String, String> params) {
        logger.info("command POST request received with params: " + params);
        CommandMessage commandMessage = new CommandMessage();
        String commandStr = params.get("command") + " " + String.join(" ", params.get("args").split(","));
        commandMessage.setCommand(commandStr);
        String response = baseRouting.handleCommand(commandMessage.getCommand());
        return "Command received" + (Objects.isNull(response) ? "" : ", response: " + response);
    }
}
