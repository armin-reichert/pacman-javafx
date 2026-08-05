/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.house.House;

public record GhostEntersHouseEvent(GameContext gameContext, Ghost ghost, House house) implements GameEvent {}
