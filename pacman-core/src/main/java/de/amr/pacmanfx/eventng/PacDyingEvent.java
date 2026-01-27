package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Pac;

public record PacDyingEvent(Pac pac) implements GameEventNG {}
