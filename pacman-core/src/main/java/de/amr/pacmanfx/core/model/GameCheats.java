/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameCheats {

    private final BooleanProperty cheatUsed = new SimpleBooleanProperty(false);

    private final BooleanProperty pacImmune = new SimpleBooleanProperty(false);

    private final BooleanProperty pacUsingAutopilot = new SimpleBooleanProperty(false);

    public GameCheats() {}

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

    public void clear() {
        cheatUsedProperty().set(false);
        pacImmuneProperty().set(false);
        pacUsingAutopilotProperty().set(false);
    }

    public void update(GameLevel level) {
        if (level.isDemoLevel() || !level.gameModel().isPlaying()) {
            return;
        }
        final Pac pac = level.entities().pac();
        pac.pacCheats().immuneProperty().set(isPacImmune());
        pac.pacCheats().usingAutopilotProperty().set(isPacUsingAutopilot());
        if (isPacImmune() || isPacUsingAutopilot()) {
            notifyCheatUsed();
        }
    }
}
