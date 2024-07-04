package de.kolbenik;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.kolbenik.logging.LogFileRemover;
import de.kolbenik.logging.Logger;
import de.kolbenik.rgbpixlNasaWebhook.DiscordWebhook;
import de.kolbenik.rgbpixlNasaWebhook.ScheduledTask;
import de.kolbenik.rgbpixlNasaWebhook.config.Configuration;
import de.kolbenik.rgbpixlNasaWebhook.config.ConfigurationObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.FileSystems;
import java.util.TimeZone;
import java.util.concurrent.*;

public class Main {

    public static ConfigurationObject config;
    public static DiscordWebhook webhook;

    public static String VERSION = "VERSION 1.5";

    static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws IOException, InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        Logger.addLoggerToSystemOut(true, Main.class.getSimpleName());

        LogFileRemover.initialize();
        config = Configuration.loadConfig(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/config.json");
        scheduler.scheduleAtFixedRate(new ScheduledTask(), 0, 24, TimeUnit.HOURS);

        Main main = new Main();
    }

    public Main() throws IOException, InterruptedException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine().toLowerCase().trim();
            if (line.equals("quit")) {
                System.out.println("Shutting down...");
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
                LogFileRemover.getInstance().stopScheduler();

                Logger.getInstance().log(
                        "NASA APOD sender stopped! Exiting...",
                        Logger.LogLevel.INFO, Main.class.getSimpleName());
                System.exit(0);
            }
        }
    }

    public static synchronized void getAPOD(DiscordWebhook webhook) {
        try {
            URL url = new URL(config.nasaApi().apiUrl() + "?api_key=" + config.nasaApi().apiKey());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            StringBuilder content = getContent(con);
            con.disconnect();

            if (content.isEmpty()) {
                Logger.getInstance().log("NASA API returned HTTP Status: " + con.getResponseCode(),
                        Logger.LogLevel.ERROR, Main.class.getSimpleName());
                ;
            } else {
                JsonObject object = new Gson().fromJson(content.toString(), JsonObject.class);
                String copyright = object.has("copyright") ? object.get("copyright").getAsString().replaceAll("\n", "") : "";
                String date = object.get("date").getAsString();
                String title = object.get("title").getAsString();

                String mediaType = object.get("media_type").getAsString();
                String mediaUrl = object.get("url").getAsString();

                if (mediaType.equals("image")) {
                    DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject()
                            .setTitle(title)
                            .setImage(mediaUrl)
                            .setFooter(date, "");
                    if (!copyright.isEmpty()) {
                        embed.setDescription(copyright);
                    }
                    webhook.addEmbed(embed);
                } else {
                    webhook.setContent(mediaUrl);
                }

            }

        } catch (IOException e) {
            Logger.getInstance().logException(
                    "Exception while retrieving APOD image from NASA",
                    e, Main.class.getSimpleName());
            webhook.addEmbed(new DiscordWebhook.EmbedObject().setDescription("Failed to retrieve data from NASA"));
        }
    }


    private static synchronized StringBuilder getContent(HttpURLConnection con) throws IOException {
        StringBuilder content = new StringBuilder();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content;
    }
}