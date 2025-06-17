package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

public class LivesCounter extends Actor {
    private int initialLifeCount;
    private final IntegerProperty lifeCountPy = new SimpleIntegerProperty(0);

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    public int lifeCount() { return lifeCountPy.get(); }

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
