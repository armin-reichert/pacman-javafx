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

    public GameSession(String variantName, GameCheats cheats, int lifeCount) {
        super(new HashMap<>());

        requireNonNull(variantName);
        requireNonNull(cheats);

        this.cheats = requireNonNull(cheats);

        hud = new HUD(variantName);

        setValue(SessionKey.NUM_LIVES, lifeCount);
        setValue(SessionKey.FRAME_STATE, new FrameState(0));

        cheats.cheatUsedProperty().addListener((_, _, cheated) -> {
            if (cheated) {
                hud.highScore().data().setEnabled(false);
            }
        });
    }

    public Optional<GameLevel> optLevel() {
        return Optional.ofNullable(value(SessionKey.GAME_LEVEL, GameLevel.class));
    }

    public HUD hud() {
        return hud;
    }

    public GameCheats cheats() {
        return cheats;
    }
}
