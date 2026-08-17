package de.amr.pacmanfx.core.entities.score.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;

import java.time.LocalDate;

public class ScoreDataComp implements EntityComponent {

    private boolean enabled;

    private int points;

    private int levelNumber;

    private LocalDate date;

    public ScoreDataComp() {
        reset();
    }

    @Override
    public void reset() {
        enabled = true;
        points = 0;
        levelNumber = 1;
        date = LocalDate.now();
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void setPoints(int points) { this.points = points;  }

    public int points() {
        return points;
    }

    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }

    public int levelNumber() {
        return levelNumber;
    }

    public void setDate(LocalDate date) { this.date = date; }

    public LocalDate date() {
        return date;
    }
}
