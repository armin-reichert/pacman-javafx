/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.level.GameLevelMessage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

import java.util.Optional;

public class HUDState {

    private boolean visible;

    public final BooleanProperty creditShown = new SimpleBooleanProperty(false);

    public final BooleanProperty levelCounterShown = new SimpleBooleanProperty(true);

    public final BooleanProperty livesCounterShown = new SimpleBooleanProperty(true);

    public final BooleanProperty scoreShown = new SimpleBooleanProperty(true);

    private GameLevelMessage message;

    public HUDState() {}

    public void show() {
        visible = true;
        Logger.info("HUD is VISIBLE!");
    }

    public void hide() {
        visible = false;
        Logger.info("HUD is HIDDEN!");
    }

    public boolean isVisible() { return visible; }

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

    // Messages appearing over level ("READY", "GAME OVER")

    /**
     * Sets the message that should be displayed in the level (READY, GAME OVER, TESTING).
     * @param message the message
     */
    public void setMessage(GameLevelMessage message) {
        this.message = message;
    }

    /**
     * Clears the level message.
     */
    public void clearMessage() {
        message = null;
    }


    /**
     * @return (optional) the current level message
     */
    public Optional<GameLevelMessage> optMessage() {
        return Optional.ofNullable(message);
    }

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
}