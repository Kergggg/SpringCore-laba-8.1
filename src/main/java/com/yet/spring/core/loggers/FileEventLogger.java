package com.yet.spring.core.loggers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yet.spring.core.beans.Event;

public class FileEventLogger extends AbstractLogger {

    private static final Logger logger = LoggerFactory.getLogger(FileEventLogger.class);

    private File file;
    private String filename;

    public FileEventLogger(String filename) {
        this.filename = filename;
    }

    public void init() throws IOException {
        file = new File(filename);
        if (file.exists() && !file.canWrite()) {
            throw new IllegalArgumentException("Can't write to file " + filename);
        } else if (!file.exists()) {
            file.createNewFile();
        }
        logger.info("FileEventLogger initialized with file: {}", filename);
    }

    @Override
    public void logEvent(Event event) {
        try {
            FileUtils.writeStringToFile(file, event.toString() + "\n", true);
            logger.debug("Event written to file: {}", event.getId());
        } catch (IOException e) {
            logger.error("Error writing event to file", e);
            e.printStackTrace();
        }
    }
}