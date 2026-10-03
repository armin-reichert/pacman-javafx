/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.bonus;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.entities.world.WorldNavigationComp;

import java.util.Optional;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public final class Bonus extends GameEntity {

    public static Bonus createStaticBonus(int symbolCode) {
        return new Bonus(symbolCode);
    }

    public static Bonus createMovingBonus(int symbolCode) {
        final var bonus = new Bonus(symbolCode);
        bonus.setComponent(MovementComp.class, new MovementComp());
        bonus.setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        bonus.setComponent(BonusMoveAndJumpComp.class, new BonusMoveAndJumpComp());

        bonus.assertComponent(WorldNavigationComp.class).setCanTeleport(false);
        return bonus;
    }

    public Bonus(int symbolCode) {
        setComponent(BonusDataComp.class, new BonusDataComp(symbolCode));
        setComponent(BonusStateComp.class, new BonusStateComp());
    }

    public BonusDataComp data() {
        return assertComponent(BonusDataComp.class);
    }

    public BonusStateComp state() {
        return assertComponent(BonusStateComp.class);
    }

    public Optional<BonusMoveAndJumpComp> optMoveAndJump() {
        return optComponent(BonusMoveAndJumpComp.class);
    }
}