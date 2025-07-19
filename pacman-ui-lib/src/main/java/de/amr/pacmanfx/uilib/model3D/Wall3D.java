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

import static java.util.Objects.requireNonNull;

/**
 * 3D wall composed of a base and a top part. Expects the base part to be either a box or a cylinder.
 */
public class Wall3D extends Group implements Destroyable {

    public static final double DEFAULT_BASE_HEIGHT = 4;
    public static final double DEFAULT_TOP_HEIGHT = 0.2;
    public static final double DEFAULT_WALL_THICKNESS = 2;

    public Wall3D(Node base, Node top) {
        super(requireNonNull(base), requireNonNull(top));
    }

    public Node wallBase() { return getChildren().getFirst(); }
    public Node wallTop() { return getChildren().getLast(); }

    public void setBaseHeight(double baseHeight) {
        if (wallBase() instanceof Box base) {
            base.depthProperty().unbind();
            base.translateZProperty().unbind();
            wallTop().translateZProperty().unbind();
            base.setDepth(baseHeight);
            base.setTranslateZ(baseHeight * (-0.5));
            wallTop().setTranslateZ(-(baseHeight + 0.5 * DEFAULT_TOP_HEIGHT));
        } else if (wallBase() instanceof Cylinder base) {
            base.heightProperty().unbind();
            base.translateZProperty().unbind();
            wallTop().translateZProperty().unbind();
            base.setHeight(baseHeight);
            base.setTranslateZ(baseHeight * (-0.5));
            wallTop().setTranslateZ(-(baseHeight + 0.5 * DEFAULT_TOP_HEIGHT));
        } else {
            Logger.warn("The base of a 3D wall should either be a box or a cylinder");
        }
    }

    public void bindBaseHeight(DoubleProperty baseHeightProperty) {
        if (wallBase() instanceof Box box) {
            box.depthProperty().bind(baseHeightProperty);
            wallBase().translateZProperty().bind(baseHeightProperty.multiply(-0.5));
            wallTop().translateZProperty().bind(baseHeightProperty.add(0.5 * DEFAULT_TOP_HEIGHT).negate());
        } else if (wallBase() instanceof Cylinder cylinder) {
            cylinder.heightProperty().bind(baseHeightProperty);
            wallBase().translateZProperty().bind(baseHeightProperty.multiply(-0.5));
            wallTop().translateZProperty().bind(baseHeightProperty.add(0.5 * DEFAULT_TOP_HEIGHT).negate());
        } else {
            Logger.warn("The base of a 3D wall should either be a box or a cylinder");
        }
    }

    @Override
    public void destroy() {
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
