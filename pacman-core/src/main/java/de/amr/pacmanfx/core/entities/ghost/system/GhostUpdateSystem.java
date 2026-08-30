/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;


import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.List;

public class GhostUpdateSystem {

    public void update(GameContext game, GameLevel level) {
        final boolean ghostEatenState = game.state().id().equals(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        final List<Ghost> ghostsToUpdate = ghostEatenState
            ? level.entities().ghostsInAnyOfStates(GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN).toList()
            : level.entities().ghosts();

        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();
        final ActorSpeedRules speedRules = rules.actorSpeedRules();

        ghostsToUpdate.forEach(ghost -> {
            final float speed = speedRules.ghostSpeed(game, ghost);
            systems.ghostHouseAccess().update(ghost, level, speed);
            systems.ghostHuntingSystem().update(game, level, ghost);
            systems.ghostState().update(game, ghost);
            systems.ghostAnimation().update(ghost, systems.actorSpriteAnimController());
        });
    }

}
