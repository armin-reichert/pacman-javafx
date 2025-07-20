/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * 3D wall composed of a base and a top node.
 */
public record Wall3D(Shape3D base, Shape3D top) {

    private static final PseudoClass WALL3D_BASE = PseudoClass.getPseudoClass("wall3d-base");
    private static final PseudoClass WALL3D_TOP = PseudoClass.getPseudoClass("wall3d-top");

    public static final double DEFAULT_BASE_HEIGHT = 4;
    public static final double DEFAULT_TOP_HEIGHT = 0.2;
    public static final double DEFAULT_WALL_THICKNESS = 2;

    public Wall3D {
        requireNonNull(base);
        base.pseudoClassStateChanged(WALL3D_BASE, true);
        top.pseudoClassStateChanged(WALL3D_TOP, true);
    }

    public static boolean isBase(Node node) {
        return node.getPseudoClassStates().contains(WALL3D_BASE);
    }

    public static boolean isTop(Node node) {
        return node.getPseudoClassStates().contains(WALL3D_TOP);
    }

    public static void destroyPart(Node part) {
        if (isBase(part) || isTop(part)) {
            switch (part) {
                case Box box -> destroyBox(box);
                case Cylinder cylinder -> destroyCylinder(cylinder);
                default -> Logger.error("Only Box and Cylinder are allowed as parts of 3D wall");
            }
        }
    }

    private static void destroyBox(Box box) {
        box.depthProperty().unbind();
        box.translateXProperty().unbind();
        box.translateYProperty().unbind();
        box.translateZProperty().unbind();
        box.setMaterial(null);
    }

    private static void destroyCylinder(Cylinder cylinder) {
        cylinder.heightProperty().unbind();
        cylinder.translateXProperty().unbind();
        cylinder.translateYProperty().unbind();
        cylinder.translateZProperty().unbind();
        cylinder.setMaterial(null);
    }

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
}
