package com.yet.spring.core.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yet.spring.core.beans.Event;

public class ConsoleEventLogger extends AbstractLogger {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleEventLogger.class);

    @Override
    public void logEvent(Event event) {
        String message = event.toString();
        System.out.println(message);
        logger.info("Console event logged: {}", message);
    }
}