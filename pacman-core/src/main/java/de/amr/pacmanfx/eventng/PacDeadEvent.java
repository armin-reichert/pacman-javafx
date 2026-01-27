package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Pac;

public record PacDeadEvent(Pac pac) implements GameEventNG {}
