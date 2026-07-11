/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.model.actors.Bonus;

public record BonusActivatedEvent(Bonus bonus) implements GameEvent {}
