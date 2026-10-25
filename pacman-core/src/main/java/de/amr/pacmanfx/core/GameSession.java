/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.ValueMap;
import de.amr.pacmanfx.core.gamestate.FrameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;

import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSession extends ValueMap {

    private final HUD hud;

    private final GameCheats cheats;

    private GameLevel level;

    private FrameState thisFrame;

    public GameSession(String variantName, GameCheats cheats, int lifeCount) {
        super(new HashMap<>());

        requireNonNull(variantName);
        this.cheats = requireNonNull(cheats);

        hud = new HUD(variantName);

        setValue(SessionKey.NUM_LIVES, lifeCount);

        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                hud.highScore().data().setEnabled(false);
            }
        });

        newFrameState(0);
    }

    public void newFrameState(long tick) {
        thisFrame = new FrameState(tick);
    }

    public FrameState thisFrame() {
        return thisFrame;
    }

    public void setLevel(GameLevel level) {
        this.level = level;
    }

    public void removeLevel() {
        level = null;
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(level);
    }

    public HUD hud() {
        return hud;
    }

    public GameCheats cheats() {
        return cheats;
    }
}
