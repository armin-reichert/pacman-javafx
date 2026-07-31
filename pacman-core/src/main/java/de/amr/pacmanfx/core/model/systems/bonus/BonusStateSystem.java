/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.comp.bonus.BonusState;
import de.amr.pacmanfx.core.model.comp.bonus.BonusStateComp;
import de.amr.pacmanfx.core.model.comp.bonus.MoveAndJumpComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    private final WorldNavigationSystem navigator;
    private final BonusMoveAndJumpSystem moveAndJumpSystem;

    public BonusStateSystem(WorldNavigationSystem navigator, BonusMoveAndJumpSystem moveAndJumpSystem) {
        this.navigator = requireNonNull(navigator);
        this.moveAndJumpSystem = requireNonNull(moveAndJumpSystem);
    }

    public void update(GameEventManager eventManager, GameLevel level, Bonus bonus) {
        final BonusStateComp stateComp = bonus.bonusStateComp();
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
    }

    public void setInactive(Bonus bonus) {
        bonus.hide();

        final BonusStateComp stateComp = bonus.bonusStateComp();
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

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(
        Bonus bonus, float speed,
        WorldNavigationSystem navigator,
        BonusMoveAndJumpSystem moveAndJumpSystem
    ) {
        requireNonNull(bonus);

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartIndefinitely();

        bonus.show();

        if (bonus.optMovement().isPresent()) {
            this.navigator.setSpeed(bonus, speed);
        }
        navigator.clearTargetTile(bonus);
        bonus.optMoveAndJump().ifPresent(moveAndJumpSystem::start);
    }

    public void showEatenForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.EATEN);
        stateComp.timer().restartSeconds(seconds);

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }
    }
}
