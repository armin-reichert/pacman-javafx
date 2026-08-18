/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.messageview.comp.MessageViewData;

public class MessageView extends GameEntity {

    public MessageView() {
        setComp(MessageViewData.class, new MessageViewData());
    }

    public MessageViewData data() {
        return reqComp(MessageViewData.class);
    }
}
