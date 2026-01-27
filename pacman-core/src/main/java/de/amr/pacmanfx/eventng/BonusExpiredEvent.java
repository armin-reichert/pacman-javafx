package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Bonus;

public record BonusExpiredEvent(Bonus bonus) implements GameEventNG {}
