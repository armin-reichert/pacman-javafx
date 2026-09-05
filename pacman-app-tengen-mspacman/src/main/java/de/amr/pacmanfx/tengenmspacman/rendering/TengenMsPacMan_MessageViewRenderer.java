/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import de.amr.pacmanfx.uilib.rendering.MessageViewRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.EnumMap;
import java.util.Map;

public class TengenMsPacMan_MessageViewRenderer extends MessageViewRenderer {

    private static final Map<MessageType, String> MESSAGE_TEXT = new EnumMap<>(MessageType.class);
    static {
        MESSAGE_TEXT.put(MessageType.READY, "READY!");
        MESSAGE_TEXT.put(MessageType.GAME_OVER, "GAME  OVER");
        MESSAGE_TEXT.put(MessageType.NO_MESSAGE, "");
    }

    public TengenMsPacMan_MessageViewRenderer(Canvas canvas) {
        super(canvas);
    }

    public void render(Object r, long tick) {
        if (!(r instanceof MessageView messageView)) {
            return;
        }
        if (!messageView.isVisible()) {
            return;
        }

        //TODO translation of context does not belong here
        ctx.save();
        ctx.translate(scaled(TengenMsPacMan_PlayScene2D_Renderer.CONTENT_INDENT), 0);

        final MessageType messageType = messageView.type().messageType();
        messageView.optComp(MessageViewStyleComp.class).ifPresent(style -> {
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            final Color color = style.messageColor().apply(messageType);
            final Vector2f pos = messageView.pos().asVector2f();
            fillTextCentered(MESSAGE_TEXT.get(messageType), color, scaledFont, pos.x(), pos.y());
        });

        ctx.restore();
    }

    /*
        final MessageAnimation animation = session.value(
            TengenMsPacMan_Extras.GAME_OVER_MESSAGE_ANIMATION, MessageAnimation.class);

        final Vector2f pos = animation != null
            ? animation.pos().asVector2f()
            : messagePosition(level);


        final NES_WorldMapColorScheme colorScheme = level.worldMap()
            .getConfigValue(WorldMapConfigKey.COLOR_SCHEME);

        final Color color = session.isAttractMode()
            ? Color.valueOf(colorScheme.wallStroke())
            : style.messageColor().apply(MessageType.GAME_OVER);

         */
}
