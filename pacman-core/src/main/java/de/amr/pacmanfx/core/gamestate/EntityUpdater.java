/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.model.rules.GameRules;

// Preliminary central place for calling entity updates
public class EntityUpdater {

    public void updateEntities(GameContext game, GameLevel level) {
        updatePac(game, level, level.entities().pac());
        updateGhosts(game, level);
        level.entities().optBonus().ifPresent(bonus -> updateBonus(game, level, bonus));
    }

    public void updatePac(GameContext game, GameLevel level, Pac pac) {
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();
        final GameSession session = game.session();

        final ActorSpeedRules speedRules = game.variant().rules().actorSpeedRules();
        final float speed = pac.power().isActive()
            ? speedRules.pacSpeedWhenHasPower(game, level)
            : speedRules.pacSpeed(game, level);

        systems.worldNavigator().setSpeed(pac, speed);
        systems.worldNavigator().tryMovingOrTeleporting(
            systems.motor(), pac, level, systems.pacWorldMovementPolicy());

        systems.pacAutoSteering().update(session, pac);
        systems.pacDigestion().update(pac);
        systems.pacPower().update(pac, rules.pacPowerFadingSeconds(level.number()));
        systems.pacState().update(pac);
        systems.pacAnimation().update(pac);
    }

    public void updateGhosts(GameContext game, GameLevel level) {
        if (game.state().id().equals(CommonGameStateID.GAME_LEVEL_EATING_GHOST)) {
            level.entities().ghostsInAnyOfStates(GhostStateSystem.UPDATED_GHOST_STATES_WHILE_EATEN)
                .forEach(ghost -> updateGhost(game, level, ghost));
        } else {
            level.entities().ghosts().forEach(ghost -> updateGhost(game, level, ghost));
        }
    }

    private void updateGhost(GameContext game, GameLevel level, Ghost ghost) {
        final GameSystems systems = game.variant().systems();

        systems.ghostState().update(game, level, ghost);
        systems.ghostSpriteAnimation().update(
            ghost, level.entities().pac(), systems.spriteAnimController());
    }

    public void updateBonus(GameContext game, GameLevel level, Bonus bonus) {
        final GameSystems systems = game.variant().systems();
        systems.bonusState().update(level, bonus, game.eventManager(), systems.motor());
        switch (bonus.state().bonusState()) {
            case INACTIVE, EATEN -> {}
            case EDIBLE -> {
                if (bonus.hasComp(BonusMoveAndJumpComp.class)) {
                    systems.bonusMoveAndJump().wander(level, bonus, systems.motor());
                    systems.bonusMoveAndJump().jump(bonus);
                }
            }
        }
    }
}
