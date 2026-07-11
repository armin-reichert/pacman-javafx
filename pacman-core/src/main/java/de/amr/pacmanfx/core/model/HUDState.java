/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class HUDState {

    private final IntegerProperty credit = new SimpleIntegerProperty(0);

    private boolean visible = true;

    private boolean creditOn;
    private boolean levelCounterOn = true;
    private boolean livesCounterOn = true;
    private int visibleLifeCount;
    private boolean scoreOn = true;

    public HUDState() {}

    public void showIt() { visible = true; }

    public void hideIt() {
        visible = false;
    }

    public boolean isVisible() { return visible; }

    // credit

    public boolean isCreditOn() { return creditOn; }

    public HUDState creditOn() {
        creditOn = true;
        return this;
    }

    public HUDState creditOff() {
        creditOn = false;
        return this;
    }

    // level counter

    public boolean isLevelCounterOn() {
        return levelCounterOn;
    }

    public HUDState levelCounterOn() {
        levelCounterOn = true;
        return this;
    }

    public HUDState levelCounterOff() {
        levelCounterOn = false;
        return this;
    }

    // lives counter

    public boolean isLivesCounterOn() {
        return livesCounterOn;
    }

    public HUDState livesCounterOn() {
        livesCounterOn = true;
        return this;
    }

    public HUDState livesCounterOff() {
        livesCounterOn = false;
        return this;
    }

    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    public void setVisibleLifeCount(int count) {
        visibleLifeCount = count;
    }

    public int maxLivesDisplayed() {
        return 5;
    }

    // scores

    public boolean isScoreOn() {
        return scoreOn;
    }

    public HUDState scoreOn() {
        scoreOn = true;
        return this;
    }

    public HUDState scoreOff() {
        scoreOn = false;
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