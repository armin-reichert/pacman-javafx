/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.pacmanfx.arcade.pacman.model.Arcade_ActorSpeedSettings;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;

// In levels 3..., ghosts do not slow down in tunnel anymore!
public class ArcadeMsPacMan_ActorSpeedSettings extends Arcade_ActorSpeedSettings {

    @Override
    public float ghostSpeed(GameLevel level, Ghost ghost) {
        if (level.number() <= 2) {
            return super.ghostSpeed(level, ghost);
        }
        return switch (ghost.state()) {
            case HUNTING_PAC   -> ghostSpeedAttacking(level, ghost); // no tunnel slowdown
            case FRIGHTENED    -> ghostSpeedFrightened(level); // no tunnel slowdown
            default            -> super.ghostSpeed(level, ghost);
        };
    }
}
