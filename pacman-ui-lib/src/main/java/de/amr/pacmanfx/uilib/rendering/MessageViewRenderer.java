/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MessageViewRenderer extends BaseRenderer {

    protected final Map<MessageType, String> texts;

    public MessageViewRenderer(Canvas canvas, Map<MessageType, String> texts) {
        super(canvas);
        this.texts = requireNonNull(texts);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof MessageView messageView)) {
            return;
        }
        if (!messageView.isVisible()) {
            return;
        }
        if (!translate.equals(Vector2f.ZERO)) {
            ctx.save();
            ctx.translate(scaled(translate.x()), scaled(translate.y()));
        }
        messageView.optComp(MessageViewStyleComp.class).ifPresent(style -> {
            final MessageType messageType = messageView.type().messageType();
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            final Color color = style.messageColor().apply(messageType);
            final Vector2f pos = messageView.pos().asVector2f();
            fillTextCentered(texts.get(messageType), color, scaledFont, pos.x(), pos.y());
        });
        ctx.restore();
    }
}
