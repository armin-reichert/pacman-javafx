/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.marquee.comp;


// Index 0 is the lower-left (south-west) corner, then index follows border in counter-clockwise order
public record MarqueeCorners(int sw, int se, int ne, int nw) {
}
