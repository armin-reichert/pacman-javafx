/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems.bonus;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.model.entities.bonus.Bonus;
import de.amr.pacmanfx.core.model.entities.bonus.BonusState;
import de.amr.pacmanfx.core.model.entities.bonus.BonusStateComp;
import de.amr.pacmanfx.core.model.entities.bonus.MoveAndJumpComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.ecs.systems.common.WorldNavigationSystem;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    private final WorldNavigationSystem navigator;
    private final BonusMoveAndJumpSystem moveAndJumpSystem;

    public BonusStateSystem(WorldNavigationSystem navigator, BonusMoveAndJumpSystem moveAndJumpSystem) {
        this.navigator = requireNonNull(navigator);
        this.moveAndJumpSystem = requireNonNull(moveAndJumpSystem);
    }

    public void update(GameContext gameContext) {
        requireNonNull(gameContext);

        final GameLevel level = gameContext.assertLevel();
        level.entities().optBonus().ifPresent(bonus -> update(gameContext.eventManager(), level, bonus, gameContext.thisFrame()));
    }

    private void update(GameEventManager eventManager, GameLevel level, Bonus bonus, FrameContext frame) {
        final BonusStateComp stateComp = bonus.bonusState();
        final MoveAndJumpComp moveAndJumpComp = bonus.optMoveAndJump().orElse(null);

        stateComp.timer().doTick();

        switch (stateComp.state()) {

            case EDIBLE -> {
                if (moveAndJumpComp != null) {
                    moveAndJumpSystem.update(level, bonus);
                    stateComp.setEdibleStateExpired(moveAndJumpComp.targetReached() || stateComp.timer().hasExpired());
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    stateComp.setEdibleStateExpired(stateComp.timer().hasExpired());
                }
                if (stateComp.edibleStateExpired()) {
                    setInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                if (stateComp.timer().hasExpired()) {
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

        final BonusStateComp stateComp = bonus.bonusState();
        stateComp.setState(BonusState.INACTIVE);
        stateComp.timer().restartIndefinitely();

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }

        bonus.optMoveAndJump().ifPresent(moveAndJumpSystem::reset);
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp stateComp = bonus.bonusState();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(Bonus bonus, float speed) {
        requireNonNull(bonus);

        final BonusStateComp stateComp = bonus.bonusState();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartIndefinitely();

        bonus.show();

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

        final BonusStateComp stateComp = bonus.bonusState();
        stateComp.setState(BonusState.EATEN);
        stateComp.timer().restartSeconds(seconds);

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }
    }
}
