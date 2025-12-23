/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class Score {

    public static final String GITHUB_PACMAN_JAVAFX = "https://github.com/armin-reichert/pacman-javafx";

    public static final String ATTR_DATE = "date";
    public static final String ATTR_LEVEL = "level";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_URL = "url";

    private final BooleanProperty enabled = new SimpleBooleanProperty();
    private final IntegerProperty points = new SimpleIntegerProperty();
    private final IntegerProperty levelNumber = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    public static Score fromFile(File file) throws IOException {
        final var score = new Score();
        score.read(file);
        return score;
    }

    public Score() {
        reset();
    }

    public void reset() {
        setEnabled(true);
        setPoints(0);
        setLevelNumber(1);
        setDate(LocalDate.now());
    }

    public void read(File file) throws IOException {
        requireNonNull(file);
        if (!file.exists()) {
            save(file, "High score");
        }
        final var properties = new Properties();
        try (var inputStream = new BufferedInputStream(new FileInputStream(file))) {
            properties.loadFromXML(inputStream);
            setPoints(Integer.parseInt(properties.getProperty(ATTR_POINTS)));
            setLevelNumber(Integer.parseInt(properties.getProperty(ATTR_LEVEL)));
            setDate(LocalDate.parse(properties.getProperty(ATTR_DATE), DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    public void save(File file, String description) throws IOException {
        requireNonNull(file);
        requireNonNull(description);
        final boolean created = file.getParentFile().mkdirs();
        if (created) {
            Logger.info("Folder {} has been created", file.getParentFile());
        }
        final var properties = new Properties();
        try (var outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            properties.setProperty(ATTR_POINTS, String.valueOf(points()));
            properties.setProperty(ATTR_LEVEL,  String.valueOf(levelNumber()));
            properties.setProperty(ATTR_DATE,   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            properties.setProperty(ATTR_URL,    GITHUB_PACMAN_JAVAFX);
            properties.storeToXML(outputStream, description);
            Logger.info("Saved '{}' to file '{}'. Points: {} Level: {}", description, file, points(), levelNumber());
        }
    }

    public BooleanProperty enabledProperty() { return enabled; }

    public boolean isEnabled() { return enabledProperty().get(); }

    public void setEnabled(boolean enabled) { enabledProperty().set(enabled); }

    public IntegerProperty pointsProperty() { return points; }

    public void setPoints(int points) { pointsProperty().set(points);  }

    public int points() {
        return pointsProperty().get();
    }

    public IntegerProperty levelNumberProperty() { return levelNumber; }

    public void setLevelNumber(int levelNumber) { levelNumberProperty().set(levelNumber); }

    public int levelNumber() {
        return levelNumberProperty().get();
    }

    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public void setDate(LocalDate date) { dateProperty().set(date); }

    public LocalDate date() {
        return dateProperty().get();
    }
}