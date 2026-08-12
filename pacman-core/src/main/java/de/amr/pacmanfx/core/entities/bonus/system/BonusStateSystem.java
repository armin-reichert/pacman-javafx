/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.gameplay.FrameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    private final WorldNavigationSystem navigator;
    private final BonusMoveAndJumpSystem moveAndJumpSystem;

    public BonusStateSystem(WorldNavigationSystem navigator, BonusMoveAndJumpSystem moveAndJumpSystem) {
        this.navigator = requireNonNull(navigator);
        this.moveAndJumpSystem = requireNonNull(moveAndJumpSystem);
    }

    public void update(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        level.entities().optBonus().ifPresent(bonus -> update(game.eventManager(), level, bonus, session.thisFrame()));
    }

    private void update(GameEventManager eventManager, GameLevel level, Bonus bonus, FrameState frame) {
        final BonusStateComp state = bonus.state();
        final BonusMoveAndJumpComp moveAndJumpComp = bonus.optMoveAndJump().orElse(null);

        state.timer().doTick();

        switch (state.bonusState()) {

            case EDIBLE -> {
                if (moveAndJumpComp != null) {
                    moveAndJumpSystem.update(level, bonus);
                    state.setEdibleStateExpired(moveAndJumpComp.targetReached() || state.timer().hasExpired());
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    state.setEdibleStateExpired(state.timer().hasExpired());
                }
                if (state.edibleStateExpired()) {
                    setInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                if (state.timer().hasExpired()) {
                    setInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case INACTIVE -> {}
        }

        Logger.debug("Bonus {} updated at tick {}", bonus.hashCode(), frame.tick());
    }

    public void setInactive(Bonus bonus) {
        bonus.hide();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.INACTIVE);
        state.timer().restartIndefinitely();

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }

        bonus.optMoveAndJump().ifPresent(moveAndJumpSystem::reset);
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(Bonus bonus, float speed) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartIndefinitely();

        if (bonus.optMoveAndJump().isPresent()) {
            navigator.setSpeed(bonus, speed);
        }
        navigator.clearTargetTile(bonus);
        bonus.optMoveAndJump().ifPresent(moveAndJump -> {
            moveAndJumpSystem.start(moveAndJump);
            navigator.setSpeed(bonus, speed);
        });
    }

    public void showEatenForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EATEN);
        state.timer().restartSeconds(seconds);

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }
    }
}
