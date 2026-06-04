/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.tinylog.Logger;

public abstract class GameCheats {

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final BooleanProperty pacImmune = new SimpleBooleanProperty(false);

    private final BooleanProperty pacUsingAutopilot = new SimpleBooleanProperty(false);

    public BooleanProperty cheatUsedProperty() {
        return cheatUsed;
    }

    public void notifyCheatUsed() {
        cheatUsed.set(true);
    }

    public BooleanProperty pacImmuneProperty() {
        return pacImmune;
    }

    public boolean isPacImmune() {
        return pacImmuneProperty().get();
    }

    public boolean isPacUsingAutopilot() {
        return pacUsingAutopilotProperty().get();
    }

    public BooleanProperty pacUsingAutopilotProperty() {
        return pacUsingAutopilot;
    }

    protected GameCheats() {
        cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                handleCheatDetected();
            }
        });
    }

    public void clear() {
        cheatUsedProperty().set(false);
        pacImmuneProperty().set(false);
        pacUsingAutopilotProperty().set(false);
    }

    public void handleCheatDetected() {
        Logger.info("Cheat detected!");
    }

    public abstract void update(GameLevel level);
}
