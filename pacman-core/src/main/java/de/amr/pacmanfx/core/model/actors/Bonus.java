/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.BonusMoveAndJumpAnimationComponent;
import de.amr.pacmanfx.core.model.component.bonus.BonusState;
import de.amr.pacmanfx.core.model.component.bonus.BonusStateComponent;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.world.WorldNavigationComponent;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import org.tinylog.Logger;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends GameEntity implements UpdatableEntity {

    private final int symbolCode;
    private final int points;

    public Bonus(int symbolCode, int points) {
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);

        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(WorldNavigationComponent.class, new WorldNavigationComponent());
        setComponent(BonusStateComponent.class, new BonusStateComponent());
        // To add support for animated maze walking, the following component has to be added
        //setComponent(BonusJumpAnimation.class, new BonusJumpAnimation());

        reset();
        worldNavigation().setCanTeleport(false); // override default value (true)
    }

    @Override
    public void update(GameContext gameContext) {
        gameContext.systems().bonusState().update(gameContext, this);
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }

    public WorldNavigationComponent worldNavigation() {
        return requireComponent(WorldNavigationComponent.class);
    }

    public BonusStateComponent bonusState() {
        return requireComponent(BonusStateComponent.class);
    }

    public int symbolCode() {
        return symbolCode;
    }

    public int points() {
        return points;
    }

    public BonusState state() {
        return bonusState().state();
    }

    public void setInactive(GameContext gameContext) {
        gameContext.systems().bonusState().setInactive(this, gameContext.systems());
    }

    public void showEdibleForSeconds(GameContext gameContext, float seconds) {
        gameContext.systems().bonusState().showEdibleForSeconds(this, seconds);
    }

    public void showEatenForSeconds(GameContext gameContext, float seconds) {
        gameContext.systems().bonusState().showEatenForSeconds(gameContext.systems(), this, seconds);
    }

    public void showEdibleAndStartWandering(GameContext gameContext, float speed) {
        gameContext.systems().bonusState().showEdibleAndStartWandering(gameContext.systems(), this, speed);
    }

    public void setMazeRoute(GameContext gameContext, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(gameContext);

        if (supportsMoveAndJumpAnimation()) {
            final GameSystems sys = gameContext.systems();
            sys.bonusJumpAnimation().setMazeRoute(this, waypoints, leftToRight);
        }
        else {
            Logger.error("Cannot set bonus route: No bonus animation support!");
        }
    }

    public boolean supportsMoveAndJumpAnimation() {
        return hasComponent(BonusMoveAndJumpAnimationComponent.class);
    }
}