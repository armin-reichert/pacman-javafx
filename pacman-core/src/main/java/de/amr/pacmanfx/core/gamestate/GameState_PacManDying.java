/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.pac.PacDeadEvent;
import de.amr.pacmanfx.core.event.pac.PacDyingEvent;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.Pac;
import de.amr.pacmanfx.core.model.comp.ghost.ElroyComp;
import de.amr.pacmanfx.core.model.comp.pac.PacState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;

import static java.util.Objects.requireNonNull;

public final class GameState_PacManDying extends GameState {

    public record Timing(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick) {}

    private final Timing timing;

    public GameState_PacManDying(Timing timing)
    {
        super(CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        this.timing = requireNonNull(timing);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        gameContext.model().gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingRules().stop();

        level.entities().ghosts().forEach(ghost -> ghost.optComponent(ElroyComp.class).ifPresent(elroy -> elroy.setEnabled(false)));
        level.entities().optBonus().ifPresent(bonus -> sys.bonusState().setInactive(bonus));

        // Pac-Man stops moving and is prepared for "dying" animation
        sys.worldNavigator().setSpeed(pac, 0);
        sys.pacPower().reset(pac);

        sys.spriteAnim().stopSelected(pac);

        sys.pacState().setState(pac, PacState.DEAD);

        waitForTimeout();

        gameContext.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        final long tick = timer().tickCount();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                gameContext.flow().enterState(gameContext, CommonGameStateID.GAME_OVER);
            } else {
                model.addLives(-1);
                gameContext.flow().enterState(gameContext, model.lifeCount() == 0
                    ? CommonGameStateID.GAME_OVER : CommonGameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == timing.hideGhostsTick()) {
            level.entities().ghosts().forEach(GameEntity::hide);
            sys.spriteAnim().select(pac, ActorAnimationID.PAC_DYING);
            sys.spriteAnim().resetSelected(pac);
        }
        else if (tick == timing.animationStartTick()) {
            sys.spriteAnim().playSelected(pac);
            gameContext.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == timing.hidePacTick()) {
            pac.hide();
            level.optBonus().ifPresent(bonus -> sys.bonusState().setInactive(bonus));
        }
        else if (tick == timing.pacDeadTick()) {
            gameContext.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.heartbeat().triggerPulse();
            sys.pacState().update(gameContext);
        }
    }
}
