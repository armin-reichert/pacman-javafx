/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.time.LocalDate;

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
}