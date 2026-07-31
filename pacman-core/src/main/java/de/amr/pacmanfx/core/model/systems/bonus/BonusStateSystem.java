/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.comp.bonus.MoveAndJumpComp;
import de.amr.pacmanfx.core.model.comp.bonus.BonusState;
import de.amr.pacmanfx.core.model.comp.bonus.BonusStateComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    private final WorldNavigationSystem navigator;

    public BonusStateSystem(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    public void update(GameContext gameContext, Bonus bonus) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();

        final BonusStateComp stateComp = bonus.bonusStateComp();
        final MoveAndJumpComp moveAndJumpComp = bonus.optMoveAndJump().orElse(null);

        stateComp.timer().doTick();

        switch (stateComp.state()) {

            case EDIBLE -> {
                if (moveAndJumpComp != null) {
                    sys.bonusMoveAndJump().update(level, bonus);
                    stateComp.setEdibleStateExpired(moveAndJumpComp.targetReached() || stateComp.timer().hasExpired());
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    stateComp.setEdibleStateExpired(stateComp.timer().hasExpired());
                }
                if (stateComp.edibleStateExpired()) {
                    setInactive(bonus, sys.bonusMoveAndJump());
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                if (stateComp.timer().hasExpired()) {
                    setInactive(bonus, sys.bonusMoveAndJump());
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case INACTIVE -> {}
        }
    }

    public void setInactive(Bonus bonus, BonusMoveAndJumpSystem bonusMoveAndJumpSystem) {
        requireNonNull(bonusMoveAndJumpSystem);

        bonus.hide();

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.INACTIVE);
        stateComp.timer().restartIndefinitely();

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, 0);
        }

        bonus.optMoveAndJump().ifPresent(bonusMoveAndJumpSystem::reset);
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(GameSystems sys, Bonus bonus, float speed) {
        requireNonNull(sys);
        requireNonNull(bonus);

        final BonusStateComp stateComp = bonus.bonusStateComp();
        stateComp.setState(BonusState.EDIBLE);
        stateComp.timer().restartIndefinitely();

        bonus.show();

        if (bonus.optMovement().isPresent()) {
            navigator.setSpeed(bonus, speed);
        }

        //TODO use system method:
        bonus.optWorldNavigation().ifPresent(worldNavigation -> worldNavigation.setTargetTile(null));

        bonus.optMoveAndJump().ifPresent(moveAndJumpComp -> sys.bonusMoveAndJump().start(moveAndJumpComp));
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
