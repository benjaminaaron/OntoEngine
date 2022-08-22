package de.benjaminaaron.ontoengine.routing.bot;

import de.benjaminaaron.ontoengine.model.ModelController;
import de.benjaminaaron.ontoengine.routing.BaseRouting;
import de.benjaminaaron.ontoengine.routing.ChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Controller
@Profile({"!test"})
public class TelegramBot extends TelegramLongPollingBot implements ChangeListener {

    private final Logger logger = LogManager.getLogger(TelegramBot.class);

    @Autowired
    private BaseRouting baseRouting;

    @Autowired
    protected ModelController modelController;

    @Value("${TELEGRAM_BOT_TOKEN}") // set for instance in the run configuration of IntelliJ under environment variables
    private String token;
    @Value("${model.import.directory}")
    private Path IMPORT_DIRECTORY;

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

        if (msg.equals("/start")) return;
        if (msg.equals("/activate")) {
            chatId = update.getMessage().getChatId();
            modelController.addChangeListener(this);
            logger.info("Telegram user activated: " + chatId);
            return;
        }

        if (Objects.isNull(chatId)) {
            logger.warn("Ignoring message from non-activated Telegram user: " + msg);
            return;
        }

        boolean sendFileFlag = false;

        switch (msg) {
            case "/deactivate":
                logger.info("Telegram user deactivated: " + chatId);
                chatId = null;
                modelController.removeChangeListener(this);
                return;
            case "deposit": // also via reply to previous message?
                downloadFile(update.getMessage().getDocument());
                return;
            case "import":
                msg = "import rdf " + downloadFile(update.getMessage().getDocument());
                break;
            case "export":
                msg = "export rdf main";
                sendFileFlag = true;
                break;
            case "/statistics":
            case "/suggestions": // TODO
                msg = msg.substring(1);
        }

        logger.info("Received Telegram command message (" + chatId + "): " + msg);
        String response = baseRouting.handleCommand(msg);
        if (Objects.nonNull(response) && !sendFileFlag) {
            sendMessage(response);
        }
        if (sendFileFlag) {
            sendFile(Paths.get(response), null);
        }
    }

    @SneakyThrows
    private String downloadFile(Document doc) {
        IMPORT_DIRECTORY.toFile().mkdirs();
        GetFile getFile = new GetFile();
        getFile.setFileId(doc.getFileId());
        File target = IMPORT_DIRECTORY.resolve(doc.getFileName()).toFile();
        downloadFile(execute(getFile), target);
        sendMessage("Download successful");
        return doc.getFileName();
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
