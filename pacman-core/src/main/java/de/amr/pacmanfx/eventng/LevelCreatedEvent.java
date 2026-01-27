package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.GameLevel;

public record LevelCreatedEvent(GameLevel level) implements GameEventNG {}
