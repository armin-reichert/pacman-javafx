/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;

public record BonusExpiredEvent(GameModel game, Bonus bonus) implements GameEvent {}
