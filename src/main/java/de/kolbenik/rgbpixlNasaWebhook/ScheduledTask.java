package de.kolbenik.rgbpixlNasaWebhook;

import de.kolbenik.Main;
import de.kolbenik.logging.Logger;
import de.kolbenik.rgbpixlNasaWebhook.config.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;

import static de.kolbenik.Main.*;

public class ScheduledTask implements Runnable {
    @Override
    public void run() {
        System.out.println("running task");
        Logger.getInstance().log("Running scheduled task", Logger.LogLevel.INFO, ScheduledTask.class.getSimpleName());

        try {
            config = Configuration.loadConfig(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/config.json");

            if (Configuration.checkConfig(config)) {
                String thread = config.webhook().threadId() > 0 ?  "?thread_id=" + config.webhook().threadId() : "";

                webhook = new DiscordWebhook(config.webhook().webhookUrl() + thread);
                webhook.setTts(config.webhook().tts());
                webhook.setAvatarUrl(config.webhook().avatarUrl());
                webhook.setUsername(config.webhook().username());

                getAPOD(webhook);
                webhook.execute();
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            Logger.getInstance().logException(
                    "Exception while sending Embedded object to Discord!",
                    e, ScheduledTask.class.getSimpleName());
        }

        System.out.println("task completed");
    }
}
