/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;

public interface WorldMovementPolicy extends EntityComponent {

    /**
     * @param gameContext the game context, asserts that level exists!
     * @param tile some tile inside or outside the world
     * @return if this actor can access the given tile in its game context
     */
    boolean canAccessTile(GameContext gameContext, Actor actor, Vector2i tile);

    /**
     * @return {@code true} if this actor can reverse ist direction in its current state
     */
    boolean canTurnBack(Actor actor);
}
