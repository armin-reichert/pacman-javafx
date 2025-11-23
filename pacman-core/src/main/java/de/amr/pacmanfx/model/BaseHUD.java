/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class BaseHUD implements HUD {

    private boolean visible = true;
    private boolean creditVisible;
    private boolean livesCounterVisible = true;
    private boolean levelCounterVisible = true;
    private boolean scoreVisible = true;
    private int     visibleLifeCount;
    private final IntegerProperty numCoins = new SimpleIntegerProperty(0);

    public BaseHUD() {
    }

    @Override
    public void show(boolean b) { visible = b; }

    @Override
    public boolean isVisible() { return visible; }

    @Override
    public boolean isCreditVisible() { return creditVisible; }

    public void showCredit(boolean b) { creditVisible = b; }

    public IntegerProperty numCoinsProperty() {
        return numCoins;
    }

    public void setNumCoins(int numCoins) {
        this.numCoins.set(numCoins);
    }

    public int numCoins() {
        return numCoins.get();
    }

    @Override
    public boolean isLevelCounterVisible() {
        return levelCounterVisible;
    }

    @Override
    public void showLevelCounter(boolean b) {
        levelCounterVisible = b;
    }

    @Override
    public boolean isLivesCounterVisible() {
        return livesCounterVisible;
    }

    @Override
    public void showLivesCounter(boolean b) {
        livesCounterVisible = b;
    }

    @Override
    public int visibleLifeCount() {
        return visibleLifeCount;
    }

    @Override
    public int maxLivesDisplayed() {
        return 5;
    }

    @Override
    public void setVisibleLifeCount(int count) {
        visibleLifeCount = count;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void showScore(boolean b) {
        scoreVisible = b;
    }
}
