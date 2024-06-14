package de.kolbenik.logging;

import de.kolbenik.Main;
import de.kolbenik.exceptions.SingletonViolationException;

import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LogFileRemover implements Runnable {

    private static LogFileRemover instance;

    public final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = Logger.getInstance();

    private LogFileRemover() {
        this.logger.log("Starting " + LogFileRemover.class.getSimpleName() +" (Removes logs older than " + Logger.LOG_FILE_DELETION_DAYS + " days)...",
                Logger.LogLevel.INFO, this.getClass().getSimpleName());
        this.scheduler.scheduleWithFixedDelay(this, 0, 12, TimeUnit.HOURS);
    }

    public static synchronized LogFileRemover getInstance() {
        if (instance == null) {
            instance = new LogFileRemover();
        }
        return instance;
    }

    public static synchronized void initialize() throws SingletonViolationException {
        if (instance != null) {
            throw new SingletonViolationException("Only one instance of "+ LogFileRemover.class.getSimpleName() +" can be created.");
        }

        getInstance();
    }

    public void stopScheduler() {
        scheduler.shutdown();
        try {
            boolean selfTerminated = scheduler.awaitTermination(5, TimeUnit.SECONDS);
            if (!selfTerminated) Logger.getInstance().log( LogFileRemover.class.getSimpleName() +" failed to exit properly!", Logger.LogLevel.WARNING, this.getClass().getSimpleName());
        } catch (InterruptedException e) {
            Logger.getInstance().logException("Error closing the scheduler for the "+ LogFileRemover.class.getSimpleName() , e, this.getClass().getSimpleName());
        } finally {
            Logger.getInstance().log( LogFileRemover.class.getSimpleName() +" has stopped running tasks and has exited!", Logger.LogLevel.INFO, this.getClass().getSimpleName());
        }
    }

    private static long getDiffInDays(String timestamp_file, String timestamp_today) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d-MM-yyyy");

        Date date_file = dateFormat.parse(timestamp_file);
        Date date_today = dateFormat.parse(timestamp_today);

        long diffInMillis = Math.abs(date_today.getTime() - date_file.getTime());
        return diffInMillis / (24 * 60 * 60 * 1000);
    }

    @Override
    public void run() {
        List<Path> paths = new ArrayList<>();

        Path dir = Paths.get(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/");

        if (!Files.exists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                this.logger.logException("Error on creating folder '" + FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/" + "'",
                        e, this.getClass().getSimpleName());
            }
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    paths.add(path.getFileName().toAbsolutePath());
                }
            }
        } catch (IOException e) {
            this.logger.logException("Error deleting unnecessary log files!", e, this.getClass().getSimpleName());
        }


        for (Path p : paths) {
            String timestamp_file = p.getFileName().toString().replaceFirst("log-", "").replaceFirst(".log", "");
            DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern("d-MM-yyyy");
            String timestamp_today = dtfDay.format(LocalDateTime.now());

            try {
                long diffInDays = getDiffInDays(timestamp_file, timestamp_today);

                if (diffInDays >= Logger.LOG_FILE_DELETION_DAYS) {
                    Files.delete(Path.of(FileSystems.getDefault().getPath(".").toAbsolutePath().toString() + "/logs/", p.getFileName().toString()));
                    this.logger.log("The log file '" + p.getFileName() + "' was successfully deleted!",
                            Logger.LogLevel.INFO, this.getClass().getSimpleName());
                } else {
                    this.logger.log("The log file '" + p.getFileName() + "' is current.",
                            Logger.LogLevel.INFO, this.getClass().getSimpleName());
                }
            } catch (Exception e) {
                this.logger.logException("Error deleting the log file '" + p.getFileName() + "'",
                        e, this.getClass().getSimpleName());
            }
        }
    }
}
