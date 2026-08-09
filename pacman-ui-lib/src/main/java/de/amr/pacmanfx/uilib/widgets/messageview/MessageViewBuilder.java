/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.messageview;

import de.amr.basics.util.Ufx;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static java.util.Objects.requireNonNull;

public class MessageViewBuilder {

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private Color borderColor = Color.BLUE;
    private Color backgroundColor = Color.grayRgb(88);
    private float displaySeconds = 2;
    private Font font = Font.font("SansSerif", FontWeight.BOLD, 10);
    private Color textColor = Color.WHITE;
    private String text = "Hello, World!";

    public MessageViewBuilder backgroundColor(Color color) {
        backgroundColor = requireNonNull(color);
        return this;
    }

    public MessageViewBuilder borderColor(Color color) {
        borderColor = requireNonNull(color);
        return this;
    }

    public MessageViewBuilder displaySeconds(float sec) {
        displaySeconds = sec;
        return this;
    }

    public MessageViewBuilder font(Font font) {
        this.font = requireNonNull(font);
        return this;
    }

    public MessageViewBuilder textColor(Color color) {
        this.textColor = requireNonNull(color);
        return this;
    }

    public MessageViewBuilder text(String text) {
        this.text = requireNonNull(text);
        return this;
    }

    public MessageView build() {
        final var messageView = new MessageView();
        messageView.setDisplaySeconds(displaySeconds);

        final Text dummy = new Text(text);
        dummy.setFont(font);
        double width = dummy.getLayoutBounds().getWidth() + MARGIN;
        double height = dummy.getLayoutBounds().getHeight() + MARGIN;

        final Image image = createImage(width, height, text, font,
            backgroundColor, textColor, borderColor);

        final var imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        messageView.setImageView(imageView);

        return messageView;
    }

    private static Image createImage(
        double width, double height, String text, Font font,
        Color backgroundColor, Color textColor, Color borderColor) {

        var canvas = new Canvas(width * QUALITY, height * QUALITY);
        double canvasFontSize = font.getSize() * QUALITY;
        var g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(backgroundColor);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Ufx.deriveFont(font, canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * height);
        return canvas.snapshot(null, null);
    }


}
