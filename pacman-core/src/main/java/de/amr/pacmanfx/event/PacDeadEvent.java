package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Pac;

public record PacDeadEvent(Pac pac) implements GameEvent {}
