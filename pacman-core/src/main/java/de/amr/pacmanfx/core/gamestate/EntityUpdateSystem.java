/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.List;

// Preliminary central place for calling entity updates
public class EntityUpdateSystem {

    public void updateEntities(GameContext game) {
        game.session().optLevel().ifPresent(level -> {
            updatePac(game, level, level.entities().pac());
            updateGhosts(game, level);
            level.entities().optBonus().ifPresent(bonus -> updateBonus(game, level, bonus));
            updateHeartbeat(level);
            // Handle entities with limited lifetime like ghost points, bonus points etc.
            game.variant().systems().lifetime().update(level.entities());
        });
        game.variant().systems().hudUpdateSystem().update(game.session().hud(), game);
    }

    public void updateHeartbeat(GameLevel level) {
        level.heartbeat().triggerPulse();
    }

    public void updatePac(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();
        final MovementSystem motor = systems.motor();

        switch (pac.state().enumValue()) {
            case SLEEPING, DEAD -> systems.navigator().setDisabled(pac, true);
            case ACTIVE -> systems.navigator().setDisabled(pac, false);
        }

        final ActorSpeedRules speedRules = rules.actorSpeedRules();
        final float speed = pac.power().isActive()
            ? speedRules.pacSpeedWhenHasPower(game, level)
            : speedRules.pacSpeed(game, level);

        systems.pacState().update(pac);
        systems.pacDigestion().update(pac);
        systems.pacPower().update(pac, rules.pacPowerFadingSeconds(level.number()));

        // Steering and movement
        systems.pacAutoSteering().update(session, pac);
        systems.navigator().setMoveDirSpeed(pac, speed);
        systems.navigator().tryMovingOrTeleporting(level, pac, motor, systems.pacWorldMovementPolicy());

        // Animation
        final ActorSpriteAnimController spriteAnimController = systems.actorSpriteAnimController();
        systems.pacAnimation().update(pac);
        spriteAnimController.select(pac, pac.animation().animationID());
        if (pac.animation().isDisabled()) {
            spriteAnimController.stopSelected(pac);
        } else {
            spriteAnimController.playSelected(pac);
        }
    }

    public void updateGhosts(GameContext game, GameLevel level) {
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
