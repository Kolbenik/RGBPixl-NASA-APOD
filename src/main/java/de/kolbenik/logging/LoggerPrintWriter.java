package de.kolbenik.logging;

import java.io.PrintStream;

public class LoggerPrintWriter extends PrintStream {
    private final Logger logger = Logger.getInstance();

    public LoggerPrintWriter(PrintStream writer) {
        super(writer, true);
    }

    public void println(String x) {
        super.println(x);
        this.logger.log(x, Logger.LogLevel.CONSOLE, "Console");
    }

    public void println(Object x) {
        super.println(x);
        this.logger.log(x.toString(), Logger.LogLevel.CONSOLE, "Console");
    }
}