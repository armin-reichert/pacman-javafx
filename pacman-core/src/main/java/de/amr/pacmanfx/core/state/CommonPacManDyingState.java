/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.PacDeadEvent;
import de.amr.pacmanfx.core.event.PacDyingEvent;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;

import static java.util.Objects.requireNonNull;

public class CommonPacManDyingState extends GameState {

    private final int hideGhostsTick;
    private final int animationStartTick;
    private final int hidePacTick;
    private final int pacDeadTick;

    public CommonPacManDyingState(
        int hideGhostsTick,
        int animationStartTick,
        int hidePacTick,
        int pacDeadTick)
    {
        super(GameStateID.GAME_LEVEL_PACMAN_DYING);
        this.hideGhostsTick = hideGhostsTick;
        this.animationStartTick = animationStartTick;
        this.hidePacTick = hidePacTick;
        this.pacDeadTick = pacDeadTick;
    }

    @Override
    public void onEnter(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        gameContext.model().gateKeeper().resetCounterAndSetEnabled(true);

        level.huntingRules().stop();

        level.entities().ghosts().forEach(ghost -> ghost.optComponent(Elroy.class).ifPresent(elroy -> elroy.setEnabled(false)));
        level.entities().optBonus().ifPresent(bonus -> bonus.setInactive(gameContext));

        // Pac-Man stops moving and is prepared for "dying" animation
        sys.navigator().setSpeed(pac, 0);
        sys.pacPower().reset(pac);

        sys.spriteAnim().stopSelected(pac);

        pac.setState(Pac.State.DEAD);

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
                gameContext.flow().enterState(gameContext, GameStateID.GAME_OVER);
            } else {
                model.addLives(-1);
                gameContext.flow().enterState(gameContext, model.lifeCount() == 0
                    ? GameStateID.GAME_OVER : GameStateID.GAME_OR_LEVEL_STARTING);
            }
        }
        else if (tick == hideGhostsTick) {
            level.entities().ghosts().forEach(Actor::hide);
            sys.spriteAnim().select(pac, CommonAnimationID.PAC_DYING);
            sys.spriteAnim().resetSelected(pac);
        }
        else if (tick == animationStartTick) {
            sys.spriteAnim().playSelected(pac);
            gameContext.eventManager().publishGameEvent(new PacDyingEvent(pac));
        }
        else if (tick == hidePacTick) {
            pac.hide();
            level.optBonus().ifPresent(bonus -> bonus.setInactive(gameContext)); //TODO check this
        }
        else if (tick == pacDeadTick) {
            gameContext.eventManager().publishGameEvent(new PacDeadEvent(pac));
        }
        else {
            level.heartbeat().triggerPulse();
            pac.update(gameContext);
        }
    }
}
