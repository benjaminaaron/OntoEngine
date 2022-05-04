package de.benjaminaaron.ontoserver.routing.bot;

import de.benjaminaaron.ontoserver.routing.BaseRouting;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Controller
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(TelegramBot.class);

    @Autowired
    private BaseRouting baseRouting;

    @Value("${TELEGRAM_BOT_TOKEN}") // set for instance in the run configuration of IntelliJ under environment variables
    private String token;

    private Long chatId = null; // for now, we assume we only talk to one account

    @Override
    public String getBotUsername() {
        return "OntoEngineBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText())) {
            return;
        }
        String msg = update.getMessage().getText();
        if (msg.equals("/activate")) {
            chatId = update.getMessage().getChatId();
        }
        if (msg.equals("/deactivate")) {
            chatId = null;
        }
        logger.info("Received Telegram message (" + chatId + "): " + msg);

        baseRouting.handleCommand(msg);
    }

    @SneakyThrows
    public void sendMessage(String msg) {
        if (Objects.nonNull(chatId)) {
            execute(new SendMessage(chatId.toString(), msg));
            logger.info("Sent Telegram message (" + chatId + "): " + msg);
        }
    }
}
