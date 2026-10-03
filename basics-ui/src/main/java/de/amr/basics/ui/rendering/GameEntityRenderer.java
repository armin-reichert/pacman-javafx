/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.rendering;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.ui.entities.props.messageview.MessageViewStyleComp;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.basics.util.Ufx;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public class GameEntityRenderer extends BaseRenderer {

    public GameEntityRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameEntityView entityView -> renderGameEntity(entityView.entity(), tick);
            default -> super.render(r, tick);
        }
    }

    protected void renderGameEntity(GameEntity gameEntity, long tick) {
        requireNonNull(gameEntity);

        switch (gameEntity) {
            case ImageView imageView -> drawImageView(imageView);
            case TextView textView -> drawTextView(textView);
            case MessageView messageView -> drawMessageView(messageView);
            default -> {}
        }
    }

    protected void drawImageView(ImageView imageView) {
        if (imageView.isVisible()) {
            final Image imageFX = imageView.image().image();
            final double s = scaling();
            final double x = imageView.pos().x();
            final double y = imageView.pos().y();
            ctx.save();
            ctx.scale(s, s);
            ctx.setImageSmoothing(imageView.image().smoothing());
            ctx.drawImage(imageFX, x, y);
            ctx.restore();
        }
    }

    protected void drawTextView(TextView textView) {
        if (!textView.isVisible()) {
            return;
        }
        final var pos = textView.pos();
        final var data = textView.data();

        final Font scaledFont = Ufx.scaleFontBy(data.font(), scaling());
        if (data.center()) {
            fillTextCentered(data.text(), data.fillColor(), scaledFont, pos.x(), pos.y());
        } else {
            fillText(data.text(), data.fillColor(), scaledFont, pos.x(), pos.y());
        }
    }

    protected void drawMessageView(MessageView messageView) {
        messageView.optComp(MessageViewStyleComp.class).ifPresent(style -> {
            final MessageType messageType = messageView.type().messageType();
            final Font scaledFont = Ufx.scaleFontBy(style.messageFont(), scaling());
            final Color color = style.messageColor().apply(messageType);
            fillTextCentered(
                messageView.texts().texts().get(messageType),
                color, scaledFont,
                messageView.pos().x(), messageView.pos().y()
            );
        });
    }
}
