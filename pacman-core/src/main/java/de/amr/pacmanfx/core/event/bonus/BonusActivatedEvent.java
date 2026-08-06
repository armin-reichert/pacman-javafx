/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.bonus;

import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.event.GameEvent;

public record BonusActivatedEvent(Bonus bonus) implements GameEvent {}
