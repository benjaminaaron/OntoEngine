package de.benjaminaaron.ontoserver.client;

import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

//@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final Logger logger = LogManager.getLogger(TelegramBot.class);

    @Value("${TELEGRAM_BOT_TOKEN}") // set for instance in the run configuration of IntelliJ under environment variables
    private String token;

    private Long chatId; // for now, we assume we only talk to one account

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
        chatId = update.getMessage().getChatId();
        String msg = update.getMessage().getText();
        logger.info("Received Telegram message (" + chatId + "): " + msg);

        // TODO
    }

    @SneakyThrows
    public void sendMessage(String msg) {
        if (Objects.nonNull(chatId)) {
            execute(new SendMessage(chatId.toString(), msg));
            logger.info("Sent Telegram message (" + chatId + "): " + msg);
        }
    }
}
