/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;

public class MessageViewRenderer extends BaseRenderer{

    //TODO This does not belong here, maybe use info object?
    public static final String GAME_OVER_TEXT = "GAME  OVER";
    public static final String READY_TEXT = "READY!";

    public MessageViewRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof MessageView messageView)) {
            return;
        }
        if (!messageView.isVisible()) {
            return;
        }
        messageView.optComp(MessageViewStyleComp.class).ifPresent(style -> {
            final Vector2f pos = messageView.pos().asVector2f();
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            switch (messageView.data().messageType()) {
                case GAME_OVER -> fillTextCentered(GAME_OVER_TEXT, ARCADE_RED, scaledFont, pos.x(), pos.y());
                case READY -> fillTextCentered(READY_TEXT, ARCADE_YELLOW, scaledFont, pos.x(), pos.y());
            }
        });
    }
}
