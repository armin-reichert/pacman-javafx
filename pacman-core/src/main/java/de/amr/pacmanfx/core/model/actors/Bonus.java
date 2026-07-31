/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.comp.bonus.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.model.comp.bonus.BonusState;
import de.amr.pacmanfx.core.model.comp.bonus.BonusStateComp;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

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

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(BonusStateComp.class, new BonusStateComp());
        // To add support for animated maze walking, the following component has to be added
        //setComponent(BonusJumpAnimation.class, new BonusJumpAnimation());

        reset();
        worldNavigation().setCanTeleport(false); // override default value (true)
    }

    @Override
    public void update(GameContext gameContext) {
        gameContext.systems().bonusState().update(gameContext, this);
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return requireComponent(WorldNavigationComp.class);
    }

    public BonusStateComp bonusState() {
        return requireComponent(BonusStateComp.class);
    }

    public Optional<BonusMoveAndJumpComp> optMoveAndJumpAnimation() {
        return hasComponent(BonusMoveAndJumpComp.class)
            ? Optional.of(requireComponent(BonusMoveAndJumpComp.class))
            : Optional.empty();
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
        requireNonNull(gameContext);
        gameContext.systems().bonusState().setInactive(this, gameContext.systems());
    }

    public void showEdibleForSeconds(GameContext gameContext, float seconds) {
        requireNonNull(gameContext);
        gameContext.systems().bonusState().showEdibleForSeconds(this, seconds);
    }

    public void showEatenForSeconds(GameContext gameContext, float seconds) {
        requireNonNull(gameContext);
        gameContext.systems().bonusState().showEatenForSeconds(gameContext.systems().navigator(), this, seconds);
    }

    public void showEdibleAndStartWandering(GameContext gameContext, float speed) {
        requireNonNull(gameContext);
        gameContext.systems().bonusState().showEdibleAndStartWandering(gameContext.systems(), this, speed);
    }

    public void setRoute(GameContext gameContext, List<Vector2i> waypoints, boolean leftToRight) {
        requireNonNull(gameContext);
        requireNonNull(waypoints);

        if (optMoveAndJumpAnimation().isPresent()) {
            gameContext.systems().bonusJumpAnimation().setRoute(this, waypoints, leftToRight);
        }
        else {
            Logger.warn("Cannot set bonus route: No bonus animation support!");
        }
    }
}