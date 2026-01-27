package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostEntersHouseEvent(Ghost ghost) implements GameEventNG {}
