/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

/**
 * Message view.
 */
public class MessageView extends ImageView {

    public static class Builder {
        private Font font;
        private Color borderColor;
        private Color textColor;
        private String text;
        public Builder font(Font font) { this.font = requireNonNull(font); return this; }
        public Builder borderColor(Color color) { this.borderColor = requireNonNull(color); return this; }
        public Builder textColor(Color color) { this.textColor = requireNonNull(color); return this; }
        public Builder text(String text) { this.text = requireNonNull(text); return this; }
        public MessageView build() {
            MessageView message = new MessageView();
            double width = text.length() * font.getSize() + MARGIN;
            double height = font.getSize() + MARGIN;
            message.setImage(createImage(width, height, text, font, textColor, borderColor));
            message.setFitWidth(width);
            message.setFitHeight(height);
            message.setRotationAxis(Rotate.X_AXIS);
            message.setRotate(90);
            return message;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "{Message3D"
                + ": translateX=" + getTranslateX()
                + ", translateY=" + getTranslateY()
                + ", translateZ=" + getTranslateZ()
                + ", visible=" + isVisible()
                + ". bounds=" + getBoundsInLocal()
                + "}";
    }

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private static Image createImage(double width, double height, String text, Font font, Color textColor, Color borderColor) {
        var canvas = new Canvas(width * QUALITY, height * QUALITY);
        double canvasFontSize = font.getSize() * QUALITY;
        var g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Font.font(font.getFamily(), canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * height);
        return canvas.snapshot(null, null);
    }
}