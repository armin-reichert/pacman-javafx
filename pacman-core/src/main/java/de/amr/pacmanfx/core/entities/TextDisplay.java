/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.textdisplay.comp.TextDisplayDataComp;

public class TextDisplay extends GameEntity implements Renderable {

    public TextDisplay() {
        setComp(TextDisplayDataComp.class, new TextDisplayDataComp());
        setComp(MovementComp.class, new MovementComp());
    }

    public TextDisplayDataComp data() {
        return reqComp(TextDisplayDataComp.class);
    }

    public MovementComp movement() {
        return reqComp(MovementComp.class);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}
