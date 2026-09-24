/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import org.tinylog.Logger;

import java.util.stream.Stream;

public interface Renderable {

    static Stream<Renderable> createRenderableStream(Object... things) {
        return Ufx.streamOf(things)
            .peek(thing -> {
                if (!(thing instanceof Renderable)) {
                    Logger.error("This is no renderable: {}", thing);
                }
            })
            .filter(thing -> thing instanceof Renderable)
            .map(thing -> (Renderable) thing);
    }

    RenderingLayer layer();

    default int z() {
        return 0;
    }

    default Vector2f offset() {
        return Vector2f.ZERO;
    }
}
