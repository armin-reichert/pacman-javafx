/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.lib.Globals;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Message3D extends Group {

    private final Box blackboard;
    private double quality;
    private Font font;
    private Color borderColor;
    private Color textColor;
    private String text;
    private boolean batchUpdate;

    public Message3D(String text, Font font, Color textColor, Color borderColor) {
        blackboard = new Box(100, 10, 0.1);
        getChildren().add(blackboard);
        beginBatch();
        setQuality(3);
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
        if (text.isBlank()) {
            blackboard.setWidth(0);
            blackboard.setHeight(0);
            return;
        }

        int padding = 3;
        double width = text.length() * font.getSize() + padding;
        double height = font.getSize() + padding;
        blackboard.setWidth(width);
        blackboard.setHeight(height);

        var canvas = new Canvas(width * quality, height * quality);
        var g = canvas.getGraphicsContext2D();
        var canvasFontSize = font.getSize() * quality;
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(borderColor);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Font.font(font.getFamily(), canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * quality * padding, 0.8 * quality * height);

        var image = canvas.snapshot(null, null);
        var material = new PhongMaterial();
        material.setDiffuseMap(image);
        //material.setBumpMap(source);
        blackboard.setMaterial(material);
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

    public void setQuality(double quality) {
        Globals.assertNonNegative(quality, "Text3D quality must be positive but is %f");
        if (quality != this.quality) {
            this.quality = quality;
            updateImage();
        }
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