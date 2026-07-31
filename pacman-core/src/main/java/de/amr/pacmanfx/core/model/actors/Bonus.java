/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.comp.bonus.BonusState;
import de.amr.pacmanfx.core.model.comp.bonus.BonusStateComp;
import de.amr.pacmanfx.core.model.comp.bonus.MoveAndJumpComp;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;

import java.util.Optional;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public class Bonus extends GameEntity implements UpdatableEntity {

    public static Bonus createStaticBonus(int symbolCode, int points) {
        return new Bonus(false, symbolCode, points);
    }

    public static Bonus createMovingBonus(int symbolCode, int points) {
        return new Bonus(true, symbolCode, points);
    }

    private final int symbolCode;
    private final int points;

    private Bonus(boolean moving, int symbolCode, int points) {
        this.symbolCode = Validations.requireNonNegativeInt(symbolCode);
        this.points = Validations.requireNonNegativeInt(points);
        this.name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);

        setComponent(BonusStateComp.class, new BonusStateComp());

        if (moving) {
            setComponent(MovementComp.class, new MovementComp());
            setComponent(WorldNavigationComp.class, new WorldNavigationComp());
            setComponent(MoveAndJumpComp.class, new MoveAndJumpComp());

            optWorldNavigation().ifPresent(worldNavigation -> worldNavigation.setCanTeleport(false));
        }
    }

    @Override
    public void update(GameContext gameContext) {
        gameContext.systems().bonusState().update(
            gameContext.eventManager(),
            gameContext.assertLevel(),
            this);
    }

    // Component access

    public BonusStateComp bonusStateComp() {
        return requireComponent(BonusStateComp.class);
    }

    public Optional<MovementComp> optMovement() {
        return optComponent(MovementComp.class);
    }

    public Optional<WorldNavigationComp> optWorldNavigation() {
        return optComponent(WorldNavigationComp.class);
    }

    public Optional<MoveAndJumpComp> optMoveAndJump() {
        return optComponent(MoveAndJumpComp.class);
    }

    // API

    public BonusState state() {
        return bonusStateComp().state();
    }

    public int symbolCode() {
        return symbolCode;
    }

    public int points() {
        return points;
    }
}