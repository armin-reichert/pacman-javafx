package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Bonus;

public record BonusEatenEvent(Bonus bonus) implements GameEventNG {}
