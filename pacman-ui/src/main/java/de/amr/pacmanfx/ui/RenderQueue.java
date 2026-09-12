/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.rendering.Renderable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RenderQueue {

    public static final Comparator<Renderable> RENDERING_ORDER = Comparator
        .comparing(Renderable::layer)
        .thenComparingInt(Renderable::z);

    private final List<Renderable> queue = new ArrayList<>();

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    public void add(Renderable renderable) {
        queue.add(renderable);
    }

    public void addAll(Stream<Renderable> renderables) {
        renderables.forEach(this::add);
    }

    public Stream<Renderable> entriesInOrder() {
        queue.sort(RENDERING_ORDER);
        return queue.stream();
    }
}
