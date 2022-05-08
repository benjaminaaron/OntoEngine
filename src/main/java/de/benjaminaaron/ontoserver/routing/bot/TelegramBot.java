package de.benjaminaaron.ontoserver.routing.bot;

import de.benjaminaaron.ontoserver.model.ModelController;
import de.benjaminaaron.ontoserver.routing.BaseRouting;
import de.benjaminaaron.ontoserver.routing.ChangeListener;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Controller
public class TelegramBot extends TelegramLongPollingBot implements ChangeListener {

    private final Logger logger = LogManager.getLogger(TelegramBot.class);

    @Autowired
    private BaseRouting baseRouting;

    @Autowired
    protected ModelController modelController;

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
        if (!update.hasMessage()) return;
        if (update.getMessage().hasDocument() && Objects.isNull(update.getMessage().getCaption())) return;

        String msg = update.getMessage().hasDocument()
            ? update.getMessage().getCaption() : update.getMessage().getText();

        // deal with document TODO

        switch (msg) {
            case "/start":
                logger.info("Telegram user started the @OntoEngineBot: " + update.getMessage().getChatId());
                return;
            case "/activate":
                chatId = update.getMessage().getChatId();
                modelController.addChangeListener(this);
                logger.info("Telegram user activated: " + chatId);
                return;
            case "/deactivate":
                logger.info("Telegram user deactivated: " + chatId);
                chatId = null;
                modelController.removeChangeListener(this);
                return;
            case "/statistics":
            case "/suggestions": // TODO
                msg = msg.substring(1);
            default:
                if (Objects.isNull(chatId)) {
                    logger.warn("Ignoring message from non-activated Telegram user: " + msg);
                    return;
                }
        }

        logger.info("Received Telegram command message (" + chatId + "): " + msg);
        String result = baseRouting.handleCommand(msg);
        if (Objects.nonNull(result)) {
            sendMessage(result);
        }
    }

    @SneakyThrows
    public void sendMessage(String msg) {
        if (Objects.isNull(chatId)) return;
        execute(new SendMessage(chatId.toString(), msg));
        logger.info("Sent Telegram message (" + chatId + "): " + msg);
    }

    @SneakyThrows
    public void sendFile(Path path, String caption) {
        if (Objects.isNull(chatId)) return;
        SendDocument doc = new SendDocument(chatId.toString(), new InputFile(path.toFile()));
        if (Objects.nonNull(caption)) doc.setCaption(caption);
        execute(doc);
        logger.info("Sent Telegram message with file (" + chatId + "): " + path);
    }

    @Override
    public void broadcast(String msg) {
        sendMessage(msg);
    }
}
