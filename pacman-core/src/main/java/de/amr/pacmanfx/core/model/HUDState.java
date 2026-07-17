/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class HUDState {

    public final BooleanProperty visible = new SimpleBooleanProperty(true);

    public final IntegerProperty credit = new SimpleIntegerProperty(0);

    public final BooleanProperty creditShown = new SimpleBooleanProperty(false);

    public final BooleanProperty levelCounterShown = new SimpleBooleanProperty(true);

    public final BooleanProperty livesCounterShown = new SimpleBooleanProperty(true);

    public final BooleanProperty scoreShown = new SimpleBooleanProperty(true);

    private int livesCount;

    //TODO this is Tengen specific

    public final BooleanProperty levelNumberVisible = new SimpleBooleanProperty();

    public final BooleanProperty gameOptionsVisible = new SimpleBooleanProperty();

    public HUDState showGameOptions() {
        gameOptionsVisible.set(true);
        return this;
    }

    public HUDState hideGameOptions() {
        gameOptionsVisible.set(false);
        return this;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible.get();
    }

    public HUDState showLevelNumber() {
        levelNumberVisible.set(true);
        return this;
    }

    public HUDState hideLevelNumber() {
        levelNumberVisible.set(false);
        return this;
    }

    public boolean isLevelNumberVisible() {
        return levelNumberVisible.get();
    }

    // End of Tengen-specific part

    public HUDState() {}

    public void show() { visible.set(true); }

    public void hide() {
        visible.set(false);
    }

    public boolean isVisible() { return visible.get(); }

    // credit

    public boolean isCreditShown() { return creditShown.get(); }

    public HUDState showCredit() {
        creditShown.set(true);
        return this;
    }

    public HUDState hideCredit() {
        creditShown.set(false);
        return this;
    }

    // level counter

    public boolean isLevelCounterShown() {
        return levelCounterShown.get();
    }

    public HUDState showLevelCounter() {
        levelCounterShown.set(true);
        return this;
    }

    public HUDState hideLevelCounter() {
        levelCounterShown.set(false);
        return this;
    }

    // lives counter

    public boolean isLivesCounterShown() {
        return livesCounterShown.get();
    }

    public HUDState showLivesCounter() {
        livesCounterShown.set(true);
        return this;
    }

    public HUDState hideLivesCounter() {
        livesCounterShown.set(false);
        return this;
    }

    // visible lives count

    public int visibleLifeCount() {
        return livesCount;
    }

    public void setLivesCount(int count) {
        livesCount = count;
    }

    public int maxLivesShown() {
        return 5;
    }

    // scores

    public boolean isScoreShown() {
        return scoreShown.get();
    }

    public HUDState showScore() {
        scoreShown.set(true);
        return this;
    }

    public HUDState hideScore() {
        scoreShown.set(false);
        return this;
    }

    // credit

    public int credit() {
        return credit.get();
    }

    public void setCredit(int credit) {
        creditProperty().set(credit);
    }

    public IntegerProperty creditProperty() {
        return credit;
    }
}