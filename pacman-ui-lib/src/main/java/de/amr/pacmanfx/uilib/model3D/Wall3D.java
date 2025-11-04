/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.math.Vector2f;
import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * 3D wall composed of a base and a top node.
 */
public class Wall3D {

    public enum WallType { BOX, CYLINDER }

    private static final PseudoClass WALL3D_BASE = PseudoClass.getPseudoClass("wall3d-base");
    private static final PseudoClass WALL3D_TOP = PseudoClass.getPseudoClass("wall3d-top");

    private static final int CYLINDER_DIVISIONS = 32; // default=64

    public static final double DEFAULT_BASE_HEIGHT = 4;
    public static final double DEFAULT_TOP_HEIGHT = 0.2;
    public static final double DEFAULT_WALL_THICKNESS = 2;

    public static boolean isBase(Node node) {
        return node.getPseudoClassStates().contains(WALL3D_BASE);
    }

    public static boolean isTop(Node node) {
        return node.getPseudoClassStates().contains(WALL3D_TOP);
    }

    public static Wall3D createBoxWall(Vector2f center, double sizeX, double sizeY) {
        var wall3D = new Wall3D(WallType.BOX);
        var base = wall3D.<Box>base();
        base.setWidth(sizeX);
        base.setHeight(sizeY);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());

        var top = wall3D.<Box>top();
        top.setWidth(sizeX);
        top.setHeight(sizeY);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());
        return wall3D;
    }

    public static Wall3D createCylinderWall(Vector2f center, double radius) {
        var wall3D = new Wall3D(WallType.CYLINDER);
        var base = wall3D.<Cylinder>base();
        base.setRadius(radius);
        base.setTranslateX(center.x());
        base.setTranslateY(center.y());

        var top = wall3D.<Cylinder>top();
        top.setRadius(radius);
        top.setTranslateX(center.x());
        top.setTranslateY(center.y());

        return wall3D;
    }

    private Shape3D base;
    private Shape3D top;

    public Wall3D(WallType type) {
        requireNonNull(type);
        switch (type) {
            case BOX -> {
                base = new Box(4, 4, DEFAULT_BASE_HEIGHT);
                top = new Box(4, 4, DEFAULT_TOP_HEIGHT);
            }
            case CYLINDER -> {
                base = new Cylinder(1, DEFAULT_BASE_HEIGHT, CYLINDER_DIVISIONS);
                base.setRotationAxis(Rotate.X_AXIS);
                base.setRotate(90);
                top = new Cylinder(1, DEFAULT_TOP_HEIGHT, CYLINDER_DIVISIONS);
                top.setRotationAxis(Rotate.X_AXIS);
                top.setRotate(90);
            }
        }
        base.pseudoClassStateChanged(WALL3D_BASE, true);
        base.setMouseTransparent(true);
        base.setCullFace(CullFace.BACK);

        top.pseudoClassStateChanged(WALL3D_TOP, true);
        top.setMouseTransparent(true);
        top.setCullFace(CullFace.BACK);
    }

    public void setBaseMaterial(Material material) {
        base.setMaterial(material);
    }

    public void setTopMaterial(Material material) {
        top.setMaterial(material);
    }

    @SuppressWarnings("unchecked")
    public <T extends Shape3D> T base() {
        return (T) base;
    }

    @SuppressWarnings("unchecked")
    public <T extends Shape3D> T top() {
        return (T) top;
    }

    public static void dispose(Node part) {
        if (isBase(part) || isTop(part)) {
            switch (part) {
                case Box box -> disposeBox(box);
                case Cylinder cylinder -> disposeCylinder(cylinder);
                default -> Logger.error("Only Box and Cylinder are allowed as parts of 3D wall");
            }
        }
    }

    private static void disposeBox(Box box) {
        box.depthProperty().unbind();
        box.translateXProperty().unbind();
        box.translateYProperty().unbind();
        box.translateZProperty().unbind();
        box.setMaterial(null);
    }

    private static void disposeCylinder(Cylinder cylinder) {
        cylinder.heightProperty().unbind();
        cylinder.translateXProperty().unbind();
        cylinder.translateYProperty().unbind();
        cylinder.translateZProperty().unbind();
        cylinder.setMaterial(null);
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
