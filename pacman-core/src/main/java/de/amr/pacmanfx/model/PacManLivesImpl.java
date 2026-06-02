package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PacManLivesImpl implements PacManLives {

    private final IntegerProperty initialCount = new SimpleIntegerProperty(3);

    private final IntegerProperty count = new SimpleIntegerProperty(0);

    @Override
    public int initialCount() {
        return initialCount.get();
    }

    @Override
    public void setInitialCount(int count) {
        initialCount.set(count);
    }

    @Override
    public void setCount(int numLives) {
        countProperty().set(numLives);
    }

    @Override
    public int count() {
        return countProperty().get();
    }

    @Override
    public void add(int n) {
        countProperty().set(count() + n);
    }

    public IntegerProperty countProperty() {
        return count;
    }
}
