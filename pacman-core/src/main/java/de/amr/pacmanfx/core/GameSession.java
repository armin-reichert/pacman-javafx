/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.ValueMap;
import de.amr.pacmanfx.core.gamestate.Frame;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;

import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession extends ValueMap {

    private final HUD hud;

    private final GameCheats cheats;

    private GameLevel level;

    private int numLives;

    private Frame thisFrame;

    public GameSession(HUD hud, int numLives) {
        super(new HashMap<>());

        this.numLives = Validations.requireNonNegativeInt(numLives);
        this.hud = requireNonNull(hud);

        cheats = new GameCheats();
        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                hud.highScore().data().setEnabled(false);
            }
        });

        initFrame(0);
    }

    public void initFrame(long tick) {
        thisFrame = new Frame(tick);
    }

    public Frame thisFrame() {
        return thisFrame;
    }

    public void setLevel(GameLevel level) {
        this.level = requireNonNull(level);
    }

    public void removeLevel() {
        level = null;
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    public int numLives() {
        return numLives;
    }

    public void setNumLives(int numLives) {
        this.numLives = Validations.requireNonNegativeInt(numLives);
    }

    public HUD hud() {
        return hud;
    }

    public GameCheats cheats() {
        return cheats;
    }
}
