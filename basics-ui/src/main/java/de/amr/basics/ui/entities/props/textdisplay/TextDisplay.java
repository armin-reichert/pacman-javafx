/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.textdisplay;

import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.MovementComp;

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
