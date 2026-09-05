/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.uilib.entities.messageview.comp.MessageViewStyleComp;
import de.amr.pacmanfx.uilib.rendering.MessageViewRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class TengenMsPacMan_MessageViewRenderer extends MessageViewRenderer {

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
        messageView.optComp(MessageViewStyleComp.class).ifPresent(style -> {
            ctx.save();
            //TODO this does not belong here
            ctx.translate(scaled(TengenMsPacMan_PlayScene2D_Renderer.CONTENT_INDENT), 0);
            switch (messageView.data().messageType()) {
                case GAME_OVER -> drawGameOverMessage(messageView, style);
                case READY -> drawReadyMessage(messageView, style);
            }
            ctx.restore();
        });
    }

    private void drawGameOverMessage(MessageView messageView, MessageViewStyleComp style) {
        final Vector2f pos = messageView.pos().asVector2f();
        final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());

        //TODO set all this into message view component and update this information at the right time
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

        final Color color = style.messageColor().apply(MessageType.GAME_OVER);
        fillTextCentered(GAME_OVER_TEXT, color, scaledFont, pos.x(), pos.y());
    }

    private void drawReadyMessage(MessageView messageView, MessageViewStyleComp style) {
        final Vector2f pos = messageView.pos().asVector2f();
        final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
        fillTextCentered(READY_TEXT,
            style.messageColor().apply(MessageType.READY),
            scaledFont,
            pos.x(), pos.y());
    }

    private Vector2f messagePosition(GameLevel level) {
        final House house = level.entities().house();
        final Vector2i houseSize = house.sizeInTiles();
        float cx = tilesPx(house.floorplan().minTile().x() + houseSize.x() * 0.5f);
        float cy = tilesPx(house.floorplan().minTile().y() + houseSize.y() + 1);
        return vec2_float(cx, cy);
    }
}
