package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameLevel;

public record LevelCreatedEvent(GameLevel level) implements GameEvent {}
