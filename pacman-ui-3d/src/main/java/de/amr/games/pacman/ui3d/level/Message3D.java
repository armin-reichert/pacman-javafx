/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Globals;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class Message3D extends ImageView {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final Message3D message = new Message3D();
        public Builder font(Font font) { message.font = Globals.assertNotNull(font); return this; }
        public Builder borderColor(Color color) { message.borderColor = Globals.assertNotNull(color); return this; }
        public Builder textColor(Color color) { message.textColor = Globals.assertNotNull(color); return this; }
        public Builder text(String text) { message.text = Globals.assertNotNull(text); return this; }
        public Message3D build() { message.updateImage(); return message; }
    }

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private Font font;
    private Color borderColor;
    private Color textColor;
    private String text;

    private Message3D() {
        setRotationAxis(Rotate.X_AXIS);
        setRotate(90);
    }

    private void updateImage() {
        double width = text.length() * font.getSize() + MARGIN;
        double height = font.getSize() + MARGIN;

        var canvas = new Canvas(width * QUALITY, height * QUALITY);
        var g = canvas.getGraphicsContext2D();
        var canvasFontSize = font.getSize() * QUALITY;
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Font.font(font.getFamily(), canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * QUALITY * MARGIN, 0.8 * QUALITY * height);

        setImage(canvas.snapshot(null, null));
        setFitWidth(width);
        setFitHeight(height);
        Logger.trace("New source produced");
    }
}