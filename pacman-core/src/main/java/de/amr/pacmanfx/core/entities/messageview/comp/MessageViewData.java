/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.messageview.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.level.LevelMessageType;

import java.util.Objects;

public class MessageViewData implements EntityComponent {

    private LevelMessageType messageType = LevelMessageType.NO_MESSAGE;

    private String text;

    public MessageViewData() {}

    public LevelMessageType messageType() {
        return messageType;
    }

    public void setMessageType(LevelMessageType messageType) {
        this.messageType = Objects.requireNonNull(messageType);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
