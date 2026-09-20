package de.amr.pacmanfx.core.entities.bonus;

import de.amr.basics.math.Vector2i;

import java.util.List;

public record BonusRouteInfo(boolean leftToRight, List<Vector2i> waypoints) {
}
