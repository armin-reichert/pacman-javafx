/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusDataComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;

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
        bonus.setComp(MovementComp.class, new MovementComp());
        bonus.setComp(WorldNavigationComp.class, new WorldNavigationComp());
        bonus.setComp(BonusMoveAndJumpComp.class, new BonusMoveAndJumpComp());

        bonus.reqComp(WorldNavigationComp.class).setCanTeleport(false);
        return bonus;
    }

    public Bonus(int symbolCode) {
        setComp(BonusDataComp.class, new BonusDataComp(symbolCode));
        setComp(BonusStateComp.class, new BonusStateComp());
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.ACTORS));
    }

    public BonusDataComp data() {
        return reqComp(BonusDataComp.class);
    }

    public BonusStateComp state() {
        return reqComp(BonusStateComp.class);
    }

    public Optional<WorldNavigationComp> optWorldNavigation() {
        return optComp(WorldNavigationComp.class);
    }

    public Optional<BonusMoveAndJumpComp> optMoveAndJump() {
        return optComp(BonusMoveAndJumpComp.class);
    }
}