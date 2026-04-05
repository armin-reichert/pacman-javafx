/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;

import java.util.Objects;

public class HeadsUpDisplay {

    private final CoinMechanism coinMechanism;

    private boolean visible = true;
    private boolean creditVisible;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int     visibleLifeCount;

    public HeadsUpDisplay(CoinMechanism coinMechanism) {
        this.coinMechanism = Objects.requireNonNull(coinMechanism);
    }

    public HeadsUpDisplay all(boolean visible) {
        return score(visible).levelCounter(visible).livesCounter(visible);
    }

    public HeadsUpDisplay levelCounter(boolean visible) {
        levelCounterVisible = visible;
        return this;
    }

    public HeadsUpDisplay livesCounter(boolean visible) {
        livesCounterVisible = visible;
        return this;
    }

    public HeadsUpDisplay score(boolean visible) {
        scoreVisible = visible;
        return this;
    }

    public HeadsUpDisplay credit(boolean visible) {
        creditVisible = visible;
        return this;
    }

    public IntegerProperty numCoinsProperty() {
        return coinMechanism.numCoinsProperty();
    }

    public int numCoins() {
        return coinMechanism.numCoins();
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