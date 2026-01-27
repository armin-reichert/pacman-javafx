package de.amr.pacmanfx.eventng;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Pac;

public record PacFoundFoodEvent(Pac pac, Vector2i tile) implements GameEventNG {}
