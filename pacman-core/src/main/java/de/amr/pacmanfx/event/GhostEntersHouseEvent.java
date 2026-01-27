package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostEntersHouseEvent(Ghost ghost) implements GameEvent {}
