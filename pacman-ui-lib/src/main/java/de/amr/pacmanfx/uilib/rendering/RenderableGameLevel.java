/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.basics.ui.rendering.Renderable;

/**
 * Renderable game level object.
 *
 * @param level the game level
 * @param renderInfo information for the game level renderer
 * @param layer the rendering layer e.g. {@link RenderingLayer#WORLD}
 * @param z the z-layer value
 */
public record RenderableGameLevel(GameLevel level, InfoMap renderInfo, RenderingLayer layer, int z) implements Renderable {}