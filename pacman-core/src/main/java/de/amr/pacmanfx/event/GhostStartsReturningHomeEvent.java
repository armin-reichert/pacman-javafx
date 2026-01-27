package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostStartsReturningHomeEvent(Ghost ghost) implements GameEvent {}
