package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameLevel;

public record LevelStartedEvent(GameLevel level) implements GameEvent {}
