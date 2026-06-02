package de.amr.pacmanfx.model;

public interface PacManLives {

    int initialCount();

    void setInitialCount(int numLives);

    void setCount(int numLives);

    int count();

    void add(int numLives);
}
