/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.House;

public record GhostEntersHouseEvent(GameModel game, Ghost ghost, House house) implements GameEvent {}
