/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.bonus;

import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.entities.bonus.Bonus;

public record BonusActivatedEvent(Bonus bonus) implements GameEvent {}
