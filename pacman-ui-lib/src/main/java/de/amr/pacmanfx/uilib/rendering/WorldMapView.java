/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

public record WorldMapView(
    WorldMap worldMap,
    RenderingLayer layer,
    int z,
    Vector2f offset)
    implements Renderable {}