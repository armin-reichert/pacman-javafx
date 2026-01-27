package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Pac;

public record PacDyingEvent(Pac pac) implements GameEvent {}
