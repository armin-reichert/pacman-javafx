package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostEatenEvent(Ghost ghost) implements GameEventNG {}
