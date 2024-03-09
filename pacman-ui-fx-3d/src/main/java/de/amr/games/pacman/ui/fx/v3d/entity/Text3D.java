/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
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
public class Text3D {

    private final Group root = new Group();
    private final Box box;
    private double quality = 3;
    private Font font = Font.font(8);
    private Color textColor = Color.RED;
    private String text = "";
    private boolean batchUpdate;

    public Text3D() {
        box = new Box(100, 10, 0.1);
        root.getChildren().add(box);
    }

    private void updateImage() {
        if (batchUpdate) {
            return;
        }
        if (text.isBlank()) {
            box.setWidth(0);
            box.setHeight(0);
            return;
        }

        int padding = 3;
        double width = text.length() * font.getSize() + padding;
        double height = font.getSize() + padding;
        box.setWidth(width);
        box.setHeight(height);

        var canvas = new Canvas(width * quality, height * quality);
        var g = canvas.getGraphicsContext2D();
        var canvasFontSize = font.getSize() * quality;
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(Color.WHITE);
        g.setLineWidth(5);
        g.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFont(Font.font(font.getFamily(), canvasFontSize));
        g.setFill(textColor);
        g.fillText(text, 0.5 * quality * padding, 0.8 * quality * height);

        var image = canvas.snapshot(null, null);
        var material = new PhongMaterial();
        material.setDiffuseMap(image);
        //material.setBumpMap(image);
        box.setMaterial(material);
        Logger.trace("New image produced");
    }

    public Node root() {
        return root;
    }

    public void rotate(Point3D axis, double angle) {
        requireNonNull(axis);
        box.setRotationAxis(axis);
        box.setRotate(angle);
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

        if (quality == this.quality) {
            return;
        }
        this.quality = quality;
        updateImage();
    }

    public void setFont(Font font) {
        requireNonNull(font);
        if (font.equals(this.font)) {
            return;
        }
        this.font = font;
        updateImage();
    }

    public void setTextColor(Color color) {
        requireNonNull(color);
        if (color.equals(this.textColor)) {
            return;
        }
        this.textColor = color;
        updateImage();
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }
        if (text.equals(this.text)) {
            return;
        }
        this.text = text;
        updateImage();
    }
}