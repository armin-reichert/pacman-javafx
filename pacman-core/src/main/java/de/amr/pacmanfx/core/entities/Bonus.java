/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusDataComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;

import java.util.Optional;

import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;

/**
 * A bonus that either stays at a fixed position or jumps through the world, starting at some portal,
 * making one round around the ghost house and leaving the world at some portal at the other border.
 *
 * <p>TODO: That's not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 */
public final class Bonus extends GameEntity {

    public static Bonus createStaticBonus(int symbolCode, int points) {
        return new Bonus(false, symbolCode, points);
    }

    public static Bonus createMovingBonus(int symbolCode, int points) {
        return new Bonus(true, symbolCode, points);
    }

    private Bonus(boolean moving, int symbolCode, int points) {
        name = "Bonus-symbol:%d-points:%d".formatted(symbolCode, points);

        setComp(BonusDataComp.class, new BonusDataComp(
            requireNonNegativeInt(symbolCode),
            requireNonNegativeInt(points)
        ));
        setComp(BonusStateComp.class, new BonusStateComp());

        if (moving) {
            setComp(MovementComp.class, new MovementComp());
            setComp(WorldNavigationComp.class, new WorldNavigationComp());
            setComp(BonusMoveAndJumpComp.class, new BonusMoveAndJumpComp());
            reqComp(WorldNavigationComp.class).setCanTeleport(false);
        }
    }

    // Component access

    public BonusDataComp data() {
        return reqComp(BonusDataComp.class);
    }

    public BonusStateComp state() {
        return reqComp(BonusStateComp.class);
    }

    public BonusState bonusState() {
        return state().bonusState();
    }

    public Optional<BonusMoveAndJumpComp> optMoveAndJump() {
        return optComp(BonusMoveAndJumpComp.class);
    }

}