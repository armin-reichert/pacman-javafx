/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * Renderable game level object.
 *
 * @param level the game level
 * @param renderInfo information for the game level renderer
 * @param layer the rendering layer, here usually {@link RenderingLayer#LEVEL}
 * @param z the z-order value
 * @param offset the rendering offset
 */
public record GameLevelView(
    GameLevel level,
    InfoMap renderInfo,
    RenderingLayer layer,
    int z,
    Vector2f offset
) implements Renderable {}