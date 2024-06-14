package de.kolbenik.logging;

import de.kolbenik.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Logger {
    //public String FOLDER_PATH_LOGS = "logs/";
    public static final int LOG_FILE_DELETION_DAYS = 30;

    private static Logger instance;
    private final LoggerInstance logger;

    private Logger() {
        logger = new LoggerInstance();
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static void addLoggerToSystemOut(boolean displayMessages, String className) {
        if (className == null || className.isEmpty() || className.isBlank()) {
            throw new NullPointerException("Class name can not be null");
        }

        if (displayMessages) {
            Logger.getInstance().log("Adding Logger to console (System.out)...", LogLevel.INFO, className);
            System.out.println("Adding Logger to console (System.out)...");
        }

        System.setOut(new LoggerPrintWriter(System.out));
        //if (displayMessages) System.out.println("Added Logger to console (System.out)!");
    }

    public static void addLoggerToSystemErr(boolean displayMessages, String className) {
        if (className == null || className.isEmpty() || className.isBlank()) {
            throw new NullPointerException("Class name can't be null");
        }

        if (displayMessages) {
            Logger.getInstance().log("Adding Logger to console (System.err)...", LogLevel.INFO, className);
            System.out.println("Adding Logger to console (System.err)...");
        }

        System.setErr(new LoggerPrintWriter(System.err));
        //if (displayMessages) System.out.println("Added Logger to console (System.err)!");
    }

    public void log(String message, LogLevel level, String className) {
        logger.log(message, level, className);
    }

    public void logException(String personalMessage, Exception e, String className) {
        logger.logException(personalMessage, e, className);
    }


    private static class LoggerInstance {
        private BufferedWriter log;

        private LoggerInstance() {
            checkLogFile();
        }

        private void checkLogFile() {
            DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern("d-MM-yyyy");
            try {
                if (this.log != null) {
                    this.log.close();
                }

                if (!Files.exists(Path.of(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/"))) {
                    Files.createDirectory(Path.of(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/"));
                }

                this.log = new BufferedWriter(new FileWriter(new File(Path.of(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/", "log-" + dtfDay.format(LocalDateTime.now()) + ".log").toUri()), true));
                this.log.flush();

            } catch (IOException e) {
                System.err.println("Error loading the log file!");
                throw new RuntimeException(e);
            }
        }

        private void log(String log, LogLevel logLevel, String className) {
            checkLogFile();
            try {
                DateTimeFormatter dtfSeconds = DateTimeFormatter.ofPattern("HH:mm:ss d-MM-yyyy");
                String timestamp = "[" + dtfSeconds.format(LocalDateTime.now()) + "] ";
                String additionalInfo = "[" + className + "/" + logLevel + "] ";

                this.log.write(timestamp + additionalInfo + log);
                this.log.newLine();
                this.log.flush();
            } catch (IOException e) {
                System.err.println("Error on logging \"" + log + "\" in log-file! Exception: " + e);
            }
        }

        private void logException(String personalMessage, Exception e, String className) {

            if (personalMessage != null && !personalMessage.isEmpty()) this.log("Error: " + personalMessage, LogLevel.ERROR, className);
            this.log("Exception: " + e, LogLevel.ERROR, className);
            this.log("Stacktrace:", LogLevel.ERROR, className);
            for (StackTraceElement element : e.getStackTrace()) {
                this.log("  " + element, LogLevel.ERROR, className);
            }
            this.log("End of Stacktrace!", LogLevel.ERROR, className);
        }
    }

    public enum LogLevel {
        CONSOLE, INFO, WARNING, ERROR
    }
}
