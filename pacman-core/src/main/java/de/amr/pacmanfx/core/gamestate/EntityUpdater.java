/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.model.rules.GameRules;

import java.util.List;

// Preliminary central place for calling entity updates
public class EntityUpdater {

    public void updateEntities(GameContext game, GameLevel level) {
        updatePac(game, level, level.entities().pac());
        updateGhosts(game, level);
        level.entities().optBonus().ifPresent(bonus -> updateBonus(game, level, bonus));
        updateLevelHeartbeat(level);
    }

    public void updateLevelHeartbeat(GameLevel level) {
        level.heartbeat().triggerPulse();
    }

    public void updatePac(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();

        if (game.state().id() == CommonGameStateID.GAME_LEVEL_EATING_GHOST) {
            return; // Pac-Man is invisible and frozen
        }

        if (pac.getPacState() != PacState.DEAD) {
            final ActorSpeedRules speedRules = rules.actorSpeedRules();
            final float speed = pac.power().isActive()
                ? speedRules.pacSpeedWhenHasPower(game, level)
                : speedRules.pacSpeed(game, level);

            final MovementSystem motor = systems.motor();
            systems.worldNavigator().setMoveDirSpeed(pac, speed);
            systems.worldNavigator().tryMovingOrTeleporting(
                level, pac, motor, systems.pacWorldMovementPolicy());

            systems.pacAutoSteering().update(session, pac);

            systems.pacDigestion().update(pac);
            systems.pacPower().update(pac, rules.pacPowerFadingSeconds(level.number()));
        }

        systems.pacState().update(pac);
        systems.pacAnimation().update(pac);
    }

    public void updateGhosts(GameContext game, GameLevel level) {
        final boolean ghostEatenState = game.state().id().equals(CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        final List<Ghost> updatedGhosts = ghostEatenState
            ? level.entities().ghostsInAnyOfStates(GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN).toList()
            : level.entities().ghosts();

        final GameSystems systems = game.variant().systems();
        updatedGhosts.forEach(ghost -> {
            systems.ghostState().update(game, level, ghost);
            systems.ghostSpriteAnimation().update(ghost, level.entities().pac(), systems.spriteAnimController());
        });
    }

    public void updateBonus(GameContext game, GameLevel level, Bonus bonus) {
        final GameSystems systems = game.variant().systems();

        final BonusStateComp state = bonus.state();
        switch (state.enumValue()) {
            case INACTIVE -> {}
            case EDIBLE -> {
                systems.bonusState().update(game, bonus);
                systems.bonusMoveAndJump().update(level, bonus, systems.motor());
            }
            case EATEN -> systems.bonusState().update(game, bonus);
        }
    }
}
