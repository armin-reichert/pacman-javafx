package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostStartsReturningHomeEvent(Ghost ghost) implements GameEventNG {}
