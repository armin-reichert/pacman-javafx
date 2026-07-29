/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.world;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.ActorComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;

public interface WorldMovementPolicy extends ActorComponent {

    boolean canAccessTile(GameLevel level, Actor actor, Vector2i tile);

    boolean canTurnBack(Actor actor);
}
