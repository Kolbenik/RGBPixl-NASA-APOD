package de.kolbenik.rgbpixlNasaWebhook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    public static ConfigurationObject loadConfig(String configPath) throws IOException {
        createConfig(configPath);
        return new Gson().fromJson(new FileReader(configPath), ConfigurationObject.class);
    }

    private static void createConfig(String configPath) throws IOException {
        Path path = Path.of(configPath);
        if (!Files.exists(path)) {
            WebhookConfig webhookConfig = new WebhookConfig(
                            "", "", "",0,  false);
            NasaApiConfig nasaApiConfig = new NasaApiConfig("https://api.nasa.gov/planetary/apod", "");
            ConfigurationObject config = new ConfigurationObject(webhookConfig, nasaApiConfig);

            GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
            String json = builder.create().toJson(config);
            if(Files.notExists(path)) {
                Files.createFile(path);
                Files.write(path, json.getBytes());
            }
        }
    }

    public static boolean checkConfig(ConfigurationObject config) {
        WebhookConfig webhookConfig = config.webhook();
        NasaApiConfig apiConfig = config.nasaApi();
        return !webhookConfig.webhookUrl().trim().isEmpty()
                && !webhookConfig.username().trim().isEmpty()
                && !webhookConfig.avatarUrl().trim().isEmpty()
                && webhookConfig.threadId() != 0
                && !apiConfig.apiUrl().trim().isEmpty()
                && !apiConfig.apiKey().trim().isEmpty();
    }
}
