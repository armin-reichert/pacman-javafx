/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.ui.rendering.Renderable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RenderQueue {

    public static final Comparator<Renderable> RENDERING_ORDER = Comparator
        .comparing(Renderable::layer)
        .thenComparingInt(Renderable::z);

    private final List<Renderable> renderables = new ArrayList<>();

    public void clear() {
        renderables.clear();
    }

    public int size() {
        return renderables.size();
    }

    public void add(Renderable renderable) {
        renderables.add(renderable);
    }

    public void addAll(Stream<Renderable> renderables) {
        renderables.forEach(this::add);
    }

    public void sort() {
        renderables.sort(RENDERING_ORDER);
    }

    public Stream<Renderable> renderables() {
        return renderables.stream();
    }
}
