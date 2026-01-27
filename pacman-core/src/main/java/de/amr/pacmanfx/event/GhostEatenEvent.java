package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostEatenEvent(Ghost ghost) implements GameEvent {}
