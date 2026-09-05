/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.messageview.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.Objects;

public class MessageViewTypeComp implements GameEntityComp {

    private MessageType messageType = MessageType.NO_MESSAGE;

    public MessageViewTypeComp() {}

    public MessageType messageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = Objects.requireNonNull(messageType);
    }
}
