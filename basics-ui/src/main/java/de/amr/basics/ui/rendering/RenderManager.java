/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.RectShort;

public interface RenderManager {

    int renderQueueSize();

    void clearRenderQueue();

    void addRenderable(Renderable renderable);

    void setVariantRenderer(Renderer variantRenderer);

    Renderer variantRenderer();

    void setDebugRenderer(Renderer debugRenderer);

    Renderer debugRenderer();

    void setClipRect(RectShort rectShort);

    RectShort clipRect();

    void renderFrame(long l, boolean debugMode);
}
