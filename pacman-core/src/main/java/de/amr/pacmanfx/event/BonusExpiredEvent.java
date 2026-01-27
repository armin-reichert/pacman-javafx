package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Bonus;

public record BonusExpiredEvent(Bonus bonus) implements GameEvent {}
