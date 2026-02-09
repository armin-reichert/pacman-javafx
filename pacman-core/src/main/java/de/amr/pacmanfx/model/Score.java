/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import javafx.beans.property.*;
import org.tinylog.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public Score() {
        reset();
    }

    public void reset() {
        setEnabled(true);
        setPoints(0);
        setLevelNumber(1);
        setDate(LocalDate.now());
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