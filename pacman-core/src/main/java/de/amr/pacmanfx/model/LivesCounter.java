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

    private int maxLivesDisplayed;
    private int visibleLifeCount;

    public LivesCounter() {
        maxLivesDisplayed = 5;
    }

    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    public void setVisibleLifeCount(int visibleLifeCount) {
        this.visibleLifeCount = visibleLifeCount;
    }

    public int maxLivesDisplayed() {
        return maxLivesDisplayed;
    }

    public void setMaxLivesDisplayed(int maxLivesDisplayed) {
        this.maxLivesDisplayed = maxLivesDisplayed;
    }
}
