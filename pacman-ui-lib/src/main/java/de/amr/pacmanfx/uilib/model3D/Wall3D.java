/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;

import static java.util.Objects.requireNonNull;

public class Wall3D extends Group implements Destroyable {

    public static final double DEFAULT_BASE_HEIGHT = 4;
    public static final double DEFAULT_TOP_HEIGHT = 0.2;
    public static final double DEFAULT_WALL_THICKNESS = 2;

    private final DoubleProperty baseHeightProperty = new SimpleDoubleProperty(DEFAULT_BASE_HEIGHT);

    public Wall3D(Node base, Node top) {
        requireNonNull(base);
        requireNonNull(top);
        getChildren().setAll(base, top);
        if (base instanceof Box box) {
            box.depthProperty().bind(baseHeightProperty);
        } else if (base instanceof Cylinder cylinder) {
            cylinder.heightProperty().bind(baseHeightProperty);
        }
        base.translateZProperty().bind(baseHeightProperty.multiply(-0.5));
        top.translateZProperty().bind(baseHeightProperty.add(0.5 * DEFAULT_TOP_HEIGHT).negate());
    }

    public DoubleProperty baseHeightProperty() {
        return baseHeightProperty;
    }

    @Override
    public void destroy() {
        baseHeightProperty.unbind();
        getChildren().forEach(child -> {
            switch (child) {
                case Box box -> {
                    box.depthProperty().unbind();
                    box.setMaterial(null);
                    box.translateXProperty().unbind();
                    box.translateYProperty().unbind();
                    box.translateZProperty().unbind();
                }
                case Cylinder cylinder -> {
                    cylinder.heightProperty().unbind();
                    cylinder.setMaterial(null);
                    cylinder.translateXProperty().unbind();
                    cylinder.translateYProperty().unbind();
                    cylinder.translateZProperty().unbind();
                }
                case Shape3D shape3D -> {
                    shape3D.setMaterial(null);
                    shape3D.translateXProperty().unbind();
                    shape3D.translateYProperty().unbind();
                    shape3D.translateZProperty().unbind();
                }
                default -> {}
            }
        });
        getChildren().clear();
    }
}
