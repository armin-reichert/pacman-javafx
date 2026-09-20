/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.core.event.GameEvent;

public record GhostEntersHouseEvent(GameContext game, Ghost ghost, House house) implements GameEvent {}
