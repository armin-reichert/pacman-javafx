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

public class Score {

    private final BooleanProperty enabledPy = new SimpleBooleanProperty(true);
    private final IntegerProperty pointsPy = new SimpleIntegerProperty();
    private final IntegerProperty levelNumberPy = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> datePy = new SimpleObjectProperty<>();

    public static Score fromFile(File scoreFile) {
        var score = new Score();
        try {
            score.read(scoreFile);
        } catch (IOException x) {
            Logger.error("Score could not be read from file '{}'", score);
        }
        return score;
    }

    public Score() {
        reset();
    }

    public void reset() {
        setPoints(0);
        setLevelNumber(1);
        setDate(LocalDate.now());
    }

    public BooleanProperty enabledProperty() { return enabledPy; }

    public boolean isEnabled() { return enabledPy.get(); }

    public void setEnabled(boolean enabled) { enabledPy.set(enabled); }

    public IntegerProperty pointsProperty() { return pointsPy; }

    public void setPoints(int points) { pointsProperty().set(points);  }

    public int points() {
        return pointsProperty().get();
    }

    public IntegerProperty levelNumberProperty() { return levelNumberPy; }

    public void setLevelNumber(int levelNumber) { levelNumberProperty().set(levelNumber); }

    public int levelNumber() {
        return levelNumberProperty().get();
    }

    public ObjectProperty<LocalDate> dateProperty() { return datePy; }

    public void setDate(LocalDate date) { dateProperty().set(date); }

    public LocalDate date() {
        return dateProperty().get();
    }

    public void read(File file) throws IOException {
        try (var in = new BufferedInputStream(new FileInputStream(file))) {
            var p = new Properties();
            p.loadFromXML(in);
            setPoints(Integer.parseInt(p.getProperty("points")));
            setLevelNumber(Integer.parseInt(p.getProperty("level")));
            setDate(LocalDate.parse(p.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    public void save(File file, String description) throws IOException {
        boolean created = file.getParentFile().mkdirs();
        if (created) {
            Logger.info("Folder {} has been created", file.getParentFile());
        }
        try (var out = new BufferedOutputStream(new FileOutputStream(file))) {
            var p = new Properties();
            p.setProperty("points", String.valueOf(points()));
            p.setProperty("level",  String.valueOf(levelNumber()));
            p.setProperty("date",   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            p.setProperty("url",    "https://github.com/armin-reichert/pacman-javafx");
            p.storeToXML(out, description);
            Logger.info("Saved '{}' to file '{}'. Points: {} Level: {}", description, file, points(), levelNumber());
        }
    }
}