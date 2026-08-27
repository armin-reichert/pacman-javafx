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
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.entities.ghost.system.GhostStateSystem;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.List;

// Preliminary central place for calling entity updates
public class EntityUpdater {

    public void updateEntities(GameContext game) {
        game.session().optLevel().ifPresent(level -> {
            updatePac(game, level, level.entities().pac());
            updateGhosts(game, level);
            level.entities().optBonus().ifPresent(bonus -> updateBonus(game, level, bonus));
            updateHeartbeat(level);
        });
        updateHUD(game);
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
            case SLEEPING, DEAD -> systems.worldNavigator().setDisabled(pac, true);
            case ACTIVE -> systems.worldNavigator().setDisabled(pac, false);
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
        systems.worldNavigator().setMoveDirSpeed(pac, speed);
        systems.worldNavigator().tryMovingOrTeleporting(level, pac, motor, systems.pacWorldMovementPolicy());

        // Animation
        systems.pacAnimation().update(pac);
        systems.actorSpriteAnimController().select(pac, pac.animation().animationID());
        if (!pac.animation().isDisabled()) {
            systems.actorSpriteAnimController().playSelected(pac);
        } else {
            systems.actorSpriteAnimController().stopSelected(pac);
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
            systems.ghostState().update(game, level, ghost);
            systems.ghostAnimation().update(ghost, level.entities().pac(), systems.actorSpriteAnimController());
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

    public void updateHUD(GameContext game) {
        final GameSession session = game.session();
        final LivesCounter livesCounter = session.hud().livesCounter();

        int displayedLivesCount = session.numLives() - 1;

        // When a new game starts or a level starts or continues, Pac-Man is invisible for some short time.
        // During that time, the level counter shows an additional entry and Pac-Man seems to hop from the lives
        // counter into the maze when the level starts.
        if (session.optLevel().isPresent()) {
            displayedLivesCount = adjustLiveCountOnStart(displayedLivesCount, game.state(), session.level());
        }
        displayedLivesCount = Math.clamp(displayedLivesCount, 0, livesCounter.data().maxLives());
        livesCounter.data().setNumLives(displayedLivesCount);
    }

    private int adjustLiveCountOnStart(int count, GameState gameState, GameLevel level) {
        final boolean starting = gameState.id() == CommonGameStateID.GAME_STARTING
                              || gameState.id() == CommonGameStateID.GAME_OR_LEVEL_STARTING;
        final Pac pac = level.entities().pac();
        return starting && !pac.isVisible() ? count + 1 : count;
    }
}
