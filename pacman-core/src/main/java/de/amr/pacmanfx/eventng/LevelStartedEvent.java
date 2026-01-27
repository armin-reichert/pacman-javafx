package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.GameLevel;

public record LevelStartedEvent(GameLevel level) implements GameEventNG {}
