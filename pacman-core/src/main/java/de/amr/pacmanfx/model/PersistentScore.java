/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import org.tinylog.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class PersistentScore extends Score {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final File file;

    public PersistentScore(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }

    public void load() throws IOException {
        if (!file.exists()) {
            save();
        }
        final var properties = new Properties();
        try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
            properties.loadFromXML(inputStream);
            setPoints(Integer.parseInt(properties.getProperty(ATTR_POINTS)));
            setLevelNumber(Integer.parseInt(properties.getProperty(ATTR_LEVEL)));
            setDate(LocalDate.parse(properties.getProperty(ATTR_DATE), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        Logger.info("Score loaded from file '{}': points={}, level={}", file, points(), levelNumber());
    }

    public void save() throws IOException {
        final boolean created = file.getParentFile().mkdirs();
        if (created) {
            Logger.info("Folder {} has been created", file.getParentFile());
        }
        final String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        final var properties = new Properties();
        try (var outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            properties.setProperty(ATTR_POINTS, String.valueOf(points()));
            properties.setProperty(ATTR_LEVEL,  String.valueOf(levelNumber()));
            properties.setProperty(ATTR_DATE,   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            properties.setProperty(ATTR_URL,    GITHUB_PACMAN_JAVAFX);
            properties.storeToXML(outputStream, "High Score updated at %s".formatted(dateTime));
            Logger.info("High score saved in file '{}', points: {}, level: {}", file, points(), levelNumber());
        }
    }
}
