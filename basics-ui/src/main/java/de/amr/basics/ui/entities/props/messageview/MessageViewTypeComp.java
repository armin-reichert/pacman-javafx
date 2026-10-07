/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.props.messageview;

import java.util.Objects;

public class MessageViewTypeComp {

    private MessageType messageType = MessageType.NO_MESSAGE;

    public MessageViewTypeComp() {}

    public MessageType messageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = Objects.requireNonNull(messageType);
    }
}
