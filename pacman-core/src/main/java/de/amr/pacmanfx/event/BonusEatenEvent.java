package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Bonus;

public record BonusEatenEvent(Bonus bonus) implements GameEvent {}
