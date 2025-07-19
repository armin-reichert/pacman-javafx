/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Destroyable;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.List;

/**
 * 3D wall composed of a base and a top node.
 */
public record Wall3D(Node base, Node top) implements Destroyable {

    public static final double DEFAULT_BASE_HEIGHT = 4;
    public static final double DEFAULT_TOP_HEIGHT = 0.2;
    public static final double DEFAULT_WALL_THICKNESS = 2;

    public void addTo(Group group) {
        group.getChildren().addAll(base, top);
    }

    public void setBaseHeight(double baseHeight) {
        if (base instanceof Box box) {
            box.depthProperty().unbind();
            box.translateZProperty().unbind();
            top.translateZProperty().unbind();
            box.setDepth(baseHeight);
            box.setTranslateZ(baseHeight * (-0.5));
            top.setTranslateZ(-(baseHeight + 0.5 * DEFAULT_TOP_HEIGHT));
        } else if (base instanceof Cylinder cylinder) {
            cylinder.heightProperty().unbind();
            cylinder.translateZProperty().unbind();
            top.translateZProperty().unbind();
            cylinder.setHeight(baseHeight);
            cylinder.setTranslateZ(baseHeight * (-0.5));
            top.setTranslateZ(-(baseHeight + 0.5 * DEFAULT_TOP_HEIGHT));
        } else {
            Logger.warn("The base of a 3D wall should either be a box or a cylinder");
        }
    }

    public void bindBaseHeight(DoubleProperty baseHeightProperty) {
        if (base instanceof Box box) {
            box.depthProperty().bind(baseHeightProperty);
            base.translateZProperty().bind(baseHeightProperty.multiply(-0.5));
            top.translateZProperty().bind(baseHeightProperty.add(0.5 * DEFAULT_TOP_HEIGHT).negate());
        } else if (base instanceof Cylinder cylinder) {
            cylinder.heightProperty().bind(baseHeightProperty);
            base.translateZProperty().bind(baseHeightProperty.multiply(-0.5));
            top.translateZProperty().bind(baseHeightProperty.add(0.5 * DEFAULT_TOP_HEIGHT).negate());
        } else {
            Logger.warn("The base of a 3D wall should either be a box or a cylinder");
        }
    }

    @Override
    public void destroy() {
        List.of(base, top).forEach(child -> {
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
    }
}
