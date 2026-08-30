/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.List;

public class GhostUpdateSystem {

    private final GhostHouseAccessSystem houseAccessSystem;
    private final GhostHuntingSystem huntingSystem;
    private final GhostStateSystem stateSystem;
    private final GhostAnimationSystem animationSystem;

    public GhostUpdateSystem(
        GhostHouseAccessSystem houseAccessSystem,
        GhostHuntingSystem huntingSystem,
        GhostStateSystem stateSystem,
        GhostAnimationSystem animationSystem)
    {
        this.houseAccessSystem = houseAccessSystem;
        this.huntingSystem = huntingSystem;
        this.stateSystem = stateSystem;
        this.animationSystem = animationSystem;
    }

    public void update(GameContext game, GameLevel level) {
        final boolean ghostEatenState = game.state().id().equals(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        final List<Ghost> ghostsToUpdate = ghostEatenState
            ? level.entities().ghostsInAnyOfStates(GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN).toList()
            : level.entities().ghosts();

        final GameRules rules = game.variant().rules();
        final ActorSpeedRules speedRules = rules.actorSpeedRules();

        ghostsToUpdate.forEach(ghost -> {
            final float speed = speedRules.ghostSpeed(game, ghost);
            houseAccessSystem.update(ghost, level, speed);
            //TODO this is ugly
            huntingSystem.update(game, level, ghost, game.variant().systems().ghostHuntingStrategy(ghost.personality()));
            stateSystem.update(game, ghost);
            animationSystem.update(ghost);
        });
    }
}
