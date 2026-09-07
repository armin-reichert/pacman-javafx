/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.entities.messageview.comp.MessageViewTypeComp;

public class MessageView extends GameEntity implements Renderable {

    public MessageView() {
        setComp(MessageViewTypeComp.class, new MessageViewTypeComp());
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.MESSAGE;
    }

    public MessageViewTypeComp type() {
        return reqComp(MessageViewTypeComp.class);
    }
}
