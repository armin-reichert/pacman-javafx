/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.messageview.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.Objects;

public class MessageViewData implements EntityComponent {

    private MessageType messageType = MessageType.NO_MESSAGE;

    private String text;

    public MessageViewData() {}

    public MessageType messageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = Objects.requireNonNull(messageType);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
