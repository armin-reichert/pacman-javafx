/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Message3D extends ImageView {

    private static final int MARGIN = 3;
    private static final int QUALITY = 3;

    private Font font;
    private Color borderColor;
    private Color textColor;
    private String text;
    private boolean batchUpdate;

    public Message3D(String text, Font font, Color textColor, Color borderColor) {
        beginBatch();
        setFont(font);
        setBorderColor(borderColor);
        setTextColor(textColor);
        setText(text);
        endBatch();
    }

    public Message3D() {
        this("Hello, world!", Font.font("Sans", 8), Color.WHITE, Color.grayRgb(200));
    }

    private void updateImage() {
        if (batchUpdate) {
            return;
        }
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

    public void setRotation(Point3D axis, double angle) {
        requireNonNull(axis);
        setRotationAxis(axis);
        setRotate(angle);
    }

    public void beginBatch() {
        batchUpdate = true;
    }

    public void endBatch() {
        batchUpdate = false;
        updateImage();
    }

    public void setFont(Font font) {
        requireNonNull(font);
        if (!font.equals(this.font)) {
            this.font = font;
            updateImage();
        }
    }

    public void setBorderColor(Color color) {
        requireNonNull(color);
        if (!color.equals(this.borderColor)) {
            this.borderColor = color;
            updateImage();
        }
    }

    public void setTextColor(Color color) {
        requireNonNull(color);
        if (!color.equals(this.textColor)) {
            this.textColor = color;
            updateImage();
        }
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        if (!text.equals(this.text)) {
            this.text = text;
            updateImage();
        }
    }
}