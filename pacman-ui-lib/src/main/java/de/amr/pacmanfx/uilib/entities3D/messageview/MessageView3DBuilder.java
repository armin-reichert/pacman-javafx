/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.messageview.comp.MessageView3DComp;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static java.util.Objects.requireNonNull;

public class MessageView3DBuilder {

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    public static MessageView3DComp ensureView3DExists(MessageView messageView) {
        if (!messageView.hasComp(MessageView3DComp.class)) {
            messageView.setComp(MessageView3DComp.class, new MessageView3DComp());
        }
        return messageView.reqComp(MessageView3DComp.class);
    }

    public static void createAnim3D(MessageView messageView, AnimationRegistry registry) {
        messageView.removeComp(MessageView3DComp.class);
        messageView.setComp(MessageView3DComp.class, new MessageView3DComp());
        messageView.removeComp(MessageView3DAnimationComp.class);
        messageView.setComp(MessageView3DAnimationComp.class,
            new MessageView3DAnimationComp(registry, messageView.reqComp(MessageView3DComp.class)));
    }

    private Color borderColor = Color.BLUE;
    private Color backgroundColor = Color.grayRgb(88);
    private float displaySeconds = 2;
    private Font font = Font.font("SansSerif", FontWeight.BOLD, 10);
    private Color textColor = Color.WHITE;
    private String text = "Hello, World!";

    public MessageView3DBuilder backgroundColor(Color color) {
        backgroundColor = requireNonNull(color);
        return this;
    }

    public MessageView3DBuilder borderColor(Color color) {
        borderColor = requireNonNull(color);
        return this;
    }

    public MessageView3DBuilder displaySeconds(float sec) {
        displaySeconds = sec;
        return this;
    }

    public MessageView3DBuilder font(Font font) {
        this.font = requireNonNull(font);
        return this;
    }

    public MessageView3DBuilder textColor(Color color) {
        this.textColor = requireNonNull(color);
        return this;
    }

    public MessageView3DBuilder text(String text) {
        this.text = requireNonNull(text);
        return this;
    }

    public void build(MessageView messageView) {
        build3DView(messageView);
    }

    private void build3DView(MessageView messageView) {
        final MessageView3DComp view3D = ensureView3DExists(messageView);

        view3D.setDisplaySeconds(displaySeconds);

        // Create a 2D text control and take a snapshot to create an image
        final Text textControl = new Text(text);
        textControl.setFont(font);
        final double width = textControl.getLayoutBounds().getWidth() + MARGIN;
        final double height = textControl.getLayoutBounds().getHeight() + MARGIN;

        final Image image = createImageFromTextControl(width, height, text, font, backgroundColor, textColor, borderColor);

        final var imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        view3D.setImageView(imageView);
    }

    private static Image createImageFromTextControl(
        double unscaledWidth, double unscaledHeight,
        String text, Font font,
        Color backgroundColor, Color textColor, Color borderColor) {

        final double imageWidth  = unscaledWidth * QUALITY;
        final double imageHeight = unscaledHeight * QUALITY;

        // Draw image at larger size to achieve good image resolution!
        final var canvas = new Canvas(imageWidth, imageHeight);
        final double imageFontSize = font.getSize() * QUALITY;

        final GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setImageSmoothing(false);
        ctx.setFill(backgroundColor);
        ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setStroke(borderColor);
        ctx.setLineWidth(5);
        ctx.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFont(Ufx.deriveFont(font, imageFontSize));
        ctx.setFill(textColor);
        ctx.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * unscaledHeight);

        return canvas.snapshot(null, null);
    }
}
