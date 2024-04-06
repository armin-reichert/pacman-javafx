/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * @author Armin Reichert
 */
public class Score {

    public void loadFromFile(File file) {
        try (var in = new FileInputStream(file)) {
            var p = new Properties();
            p.loadFromXML(in);
            setPoints(Integer.parseInt(p.getProperty("points")));
            setLevelNumber(Integer.parseInt(p.getProperty("level")));
            setDate(LocalDate.parse(p.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE));
            Logger.info("Score loaded from file '{}'. Points: {} Level: {}",
                file, points(), levelNumber());
        } catch (Exception x) {
            Logger.error("Score could not be loaded from file '{}'. Error: {}", file, x.getMessage());
        }
    }

    public void saveToFile(File file, String description) {
        try (var out = new FileOutputStream(file)) {
            var p = new Properties();
            p.setProperty("points", String.valueOf(points()));
            p.setProperty("level",  String.valueOf(levelNumber()));
            p.setProperty("date",   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            p.setProperty("url",    "https://github.com/armin-reichert/pacman-javafx");
            p.storeToXML(out, description);
            Logger.info("Saved '{}' to file '{}'. Points: {} Level: {}",
                description, file, points(), levelNumber());
        } catch (Exception x) {
            Logger.error("Score could not be saved to file '{}'. Error: {}", file, x.getMessage());
        }
    }

    private int points;
    private int levelNumber;
    private LocalDate date;

    public Score() {
        reset();
    }

    public void reset() {
        points = 0;
        levelNumber = 1;
        date = LocalDate.now();
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int points() {
        return points;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int levelNumber() {
        return levelNumber;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate date() {
        return date;
    }
}