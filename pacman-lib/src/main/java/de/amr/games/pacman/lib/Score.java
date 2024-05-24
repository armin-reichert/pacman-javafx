/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import org.tinylog.Logger;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * @author Armin Reichert
 */
public class Score {

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

    public void read(File file) {
        var p = new Properties();
        try (var in = new BufferedInputStream(new FileInputStream(file))) {
            p.loadFromXML(in);
            setPoints(Integer.parseInt(p.getProperty("points")));
            setLevelNumber(Integer.parseInt(p.getProperty("level")));
            setDate(LocalDate.parse(p.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("Score could not be loaded from file '{}'.", file);
        }
    }

    public void save(File file, String description) {
        var p = new Properties();
        try (var out = new BufferedOutputStream(new FileOutputStream(file))) {
            p.setProperty("points", String.valueOf(points()));
            p.setProperty("level",  String.valueOf(levelNumber()));
            p.setProperty("date",   date().format(DateTimeFormatter.ISO_LOCAL_DATE));
            p.setProperty("url",    "https://github.com/armin-reichert/pacman-javafx");
            p.storeToXML(out, description);
            Logger.info("Saved '{}' to file '{}'. Points: {} Level: {}",
                description, file, points(), levelNumber());
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("Score could not be saved to file '{}'.", file);
        }
    }
}