/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.model.actors.Bonus;
import de.amr.pacmanfx.core.model.component.BonusMoveAndJumpAnimationComponent;
import de.amr.pacmanfx.core.model.component.bonus.BonusState;
import de.amr.pacmanfx.core.model.component.bonus.BonusStateComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    public void update(GameContext gameContext, Bonus bonus) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();
        final BonusStateComponent bonusState = bonus.requireComponent(BonusStateComponent.class);

        bonusState.timer().doTick();

        switch (bonusState.state()) {
            case EDIBLE -> {
                final Optional<BonusMoveAndJumpAnimationComponent> animationComp = bonus.optMoveAndJumpAnimation();
                if (animationComp.isPresent()) {
                    bonus.optComponent(BonusMoveAndJumpAnimationComponent.class).ifPresent(animation -> {
                        sys.bonusJumpAnimation().update(level, bonus);
                        bonusState.setEdibleStateExpired(animation.targetReached() || bonusState.timer().hasExpired());
                    });
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    bonusState.setEdibleStateExpired(bonusState.timer().hasExpired());
                }
                if (bonusState.edibleStateExpired()) {
                    setInactive(bonus, sys);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }
            case EATEN -> {
                if (bonusState.timer().hasExpired()) {
                    setInactive(bonus, sys);
                    gameContext.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }
            case INACTIVE -> {}
        }
    }

    public void setInactive(Bonus bonus, GameSystems sys) {
        final BonusStateComponent bonusState = bonus.requireComponent(BonusStateComponent.class);

        bonusState.setState(BonusState.INACTIVE);
        bonusState.timer().restartIndefinitely();
        bonus.hide();

        //TODO reconsider this:
        sys.navigator().setSpeed(bonus, 0);

        bonus.optMoveAndJumpAnimation().ifPresent(animation -> sys.bonusJumpAnimation().reset(animation));
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        final BonusStateComponent bonusState = bonus.requireComponent(BonusStateComponent.class);

        bonusState.setState(BonusState.EDIBLE);
        bonusState.timer().restartSeconds(seconds);
        bonus.show();
    }

    public void showEdibleAndStartWandering(GameSystems sys, Bonus bonus, float speed) {
        final BonusStateComponent bonusState = bonus.requireComponent(BonusStateComponent.class);

        bonusState.setState(BonusState.EDIBLE);
        bonusState.timer().restartIndefinitely();
        bonus.show();

        //TODO reconsider this:
        sys.navigator().setSpeed(bonus, speed);

        //TODO use system method:
        bonus.worldNavigation().setTargetTile(null);

        bonus.optMoveAndJumpAnimation().ifPresent(animation -> sys.bonusJumpAnimation().start(animation));
    }

    public void showEatenForSeconds(GameSystems sys, Bonus bonus, float seconds) {
        requireNonNull(sys);
        final BonusStateComponent bonusState = bonus.requireComponent(BonusStateComponent.class);

        bonusState.setState(BonusState.EATEN);
        bonusState.timer().restartSeconds(seconds);
        bonus.show();

        //TODO reconsider this:
        sys.navigator().setSpeed(bonus, 0);

        bonus.optMoveAndJumpAnimation().ifPresent(animation -> sys.bonusJumpAnimation().stop(animation));
    }
}
