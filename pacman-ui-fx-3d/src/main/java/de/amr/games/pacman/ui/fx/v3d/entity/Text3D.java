/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.requirePositive;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Text3D extends Group {

    public static Text3D create(String text, Color color, Font font) {
        Text3D text3D = new Text3D();
        text3D.beginBatch();
        text3D.setTextColor(color);
        text3D.setFont(font);
        text3D.setText(text);
        text3D.endBatch();
        return text3D;
    }

    private final Box blackboard;
    private double quality;
    private Font font;
    private Color borderColor;
    private Color textColor;
    private String text;
    private boolean batchUpdate;

    public Text3D() {
        blackboard = new Box(100, 10, 0.1);
        getChildren().add(blackboard);
        beginBatch();
        setQuality(3);
        setFont(Font.font("Sans", 8));
        setBorderColor(Color.grayRgb(200));
        setTextColor(Color.WHITE);
        setText("Hello, world!");
        endBatch();
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
        //material.setBumpMap(image);
        blackboard.setMaterial(material);
        Logger.trace("New image produced");
    }

    public void rotate(Point3D axis, double angle) {
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
        requirePositive(quality, "Text3D quality must be positive but is %f");
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