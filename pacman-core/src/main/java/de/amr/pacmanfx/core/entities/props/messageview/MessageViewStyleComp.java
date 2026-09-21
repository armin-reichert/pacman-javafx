/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.props.messageview;

import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.pacmanfx.core.level.MessageType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Function;

public class MessageViewStyleComp implements GameEntityComp {

    private Font messageFont;

    private Function<MessageType, Color> messageColor;

    public MessageViewStyleComp() {
    }

    public Font messageFont() {
        return messageFont;
    }

    public void setMessageFont(Font messageFont) {
        this.messageFont = messageFont;
    }

    public Function<MessageType, Color> messageColor() {
        return messageColor;
    }

    public void setMessageColor(Function<MessageType, Color> messageColor) {
        this.messageColor = messageColor;
    }
}
