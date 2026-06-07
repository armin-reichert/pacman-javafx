/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class HUDState {

    private boolean visible = true;
    private boolean creditVisible;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int     visibleLifeCount;
    private IntegerProperty credit = new SimpleIntegerProperty(0);

    public HUDState() {}

    public HUDState all(boolean visible) {
        return score(visible).levelCounter(visible).livesCounter(visible);
    }

    public HUDState levelCounter(boolean visible) {
        levelCounterVisible = visible;
        return this;
    }

    public HUDState livesCounter(boolean visible) {
        livesCounterVisible = visible;
        return this;
    }

    public HUDState score(boolean visible) {
        scoreVisible = visible;
        return this;
    }

    public HUDState credit(boolean visible) {
        creditVisible = visible;
        return this;
    }

    public int credit() {
        return credit.get();
    }

    public void setCredit(int credit) {
        creditProperty().set(credit);
    }

    public IntegerProperty creditProperty() {
        return credit;
    }

    public boolean isCreditVisible() { return creditVisible; }

    public void show() { visible = true; }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() { return visible; }

    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
    }

    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    public int maxLivesDisplayed() {
        return 5;
    }

    public void setVisibleLifeCount(int count) {
        visibleLifeCount = count;
    }

    public boolean isScoreVisible() {
        return scoreVisible;
    }
}