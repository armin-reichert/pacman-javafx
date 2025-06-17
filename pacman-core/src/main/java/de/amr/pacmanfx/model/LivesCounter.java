/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class LivesCounter extends Actor {

    private final IntegerProperty lifeCountPy = new SimpleIntegerProperty(0);
    private int initialLifeCount;
    private int maxLivesDisplayed;

    public LivesCounter() {
        initialLifeCount = 3;
        maxLivesDisplayed = 5;
    }

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    public int lifeCount() { return lifeCountPy.get(); }

    public int maxLivesDisplayed() {
        return maxLivesDisplayed;
    }

    public void setMaxLivesDisplayed(int maxLivesDisplayed) {
        this.maxLivesDisplayed = maxLivesDisplayed;
    }

    public void setLifeCount(int n) {
        if (n >= 0) {
            lifeCountPy.set(n);
        } else {
            Logger.error("Cannot set life count to negative number");
        }
    }

    public void addLives(int n) {
        setLifeCount(lifeCount() + n);
    }
}
