/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.bonus;

import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.model.actors.Bonus;

public record BonusExpiredEvent(Bonus bonus) implements GameEvent {}
