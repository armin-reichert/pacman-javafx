/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;


/**
 * Mixin interface providing default methods for cleaning up JavaFX 3D graphics resources.
 * <p>
 * Classes that own {@link Shape3D}, {@link MeshView}, {@link LightBase}, or {@link Group}
 * nodes (or subtrees thereof) should implement this interface and call the cleanup methods
 * from their {@link #dispose()} implementation.
 * <p>
 * This interface handles:
 * <ul>
 *   <li>Unbinding and nulling materials and meshes on shapes</li>
 *   <li>Unbinding and resetting common group transforms (translate, rotate, scale)</li>
 *   <li>Unbinding visibility, opacity, effect, and clip properties</li>
 *   <li>Recursively cleaning all Shape3D descendants (including nested groups)</li>
 *   <li>Unbinding and nulling light colors</li>
 * </ul>
 * <p>
 * It does <strong>not</strong> clean up:
 * <ul>
 *   <li>Animations, timelines, transitions</li>
 *   <li>Custom bindings on {@link Group#getTransforms()}</li>
 *   <li>Event listeners or subscriptions</li>
 *   <li>User data or custom properties</li>
 * </ul>
 * <p>
 * Typical usage in {@code dispose()}:
 * <pre>{@code
 *     cleanupShape3D(myCustomShape);
 *     cleanupLight(myPointLight);
 *     cleanupGroup(myRootGroup, true);  // recursive + clears children list
 * }</pre>
 */
public interface DisposableGraphicsObject extends Disposable {

    /**
     * Cleans a single {@link Shape3D} or {@link MeshView} by unbinding and nulling
     * its material and (if applicable) mesh.
     * <p>
     * Null input is safely ignored.
     *
     * @param shape3D the shape to clean, or {@code null}
     */
    default void cleanupShape3D(Shape3D shape3D) {
        if (shape3D == null) return;

        shape3D.materialProperty().unbind();
        shape3D.setMaterial(null);
        switch (shape3D) {
            case MeshView meshView -> {
                meshView.meshProperty().unbind();
                meshView.setMesh(null);
            }
            case Cylinder cylinder -> {
                cylinder.radiusProperty().unbind();
                cylinder.heightProperty().unbind();
            }
            case Box box -> {
                box.widthProperty().unbind();
                box.heightProperty().unbind();
                box.depthProperty().unbind();
            }
            default -> {
            }
        }
    }

    /**
     * Recursively cleans all {@link Shape3D} descendants of the given group (including
     * nested groups), resets common transforms and properties, and optionally clears
     * the group's children list.
     * <p>
     * Null input is safely ignored.
     *
     * @param group        the root group to clean, or {@code null}
     * @param clearChildren if {@code true}, calls {@code group.getChildren().clear()}
     *                      after recursion
     */
    default void cleanupGroup(Group group, boolean clearChildren) {
        if (group == null) return;

        group.getTransforms().clear();

        // Transforms — most common bindings
        group.translateXProperty().unbind();   group.setTranslateX(0);
        group.translateYProperty().unbind();   group.setTranslateY(0);
        group.translateZProperty().unbind();   group.setTranslateZ(0);

        group.rotateProperty().unbind();       group.setRotate(0);
        group.rotationAxisProperty().unbind(); group.setRotationAxis(Rotate.Z_AXIS);

        group.scaleXProperty().unbind();       group.setScaleX(1.0);
        group.scaleYProperty().unbind();       group.setScaleY(1.0);
        group.scaleZProperty().unbind();       group.setScaleZ(1.0);

        // Visibility & opacity
        group.visibleProperty().unbind();
        group.opacityProperty().unbind();      // group.setOpacity(1.0);  ← optional

        // Effects & clipping
        group.effectProperty().unbind();       group.setEffect(null);
        group.clipProperty().unbind();         group.setClip(null);

        for (Node child : group.getChildren()) {
            if (child instanceof Group childGroup) {
                cleanupGroup(childGroup, clearChildren);
            }
            else if (child instanceof Shape3D shape3D) {
                cleanupShape3D(shape3D);
            }
        }

        if (clearChildren) {
            group.getChildren().clear();
        }
    }

    /**
     * Cleans a light source by unbinding and nulling its color property.
     * <p>
     * Null input is safely ignored.
     *
     * @param light the light to clean, or {@code null}
     */
    default void cleanupLight(LightBase light) {
        if (light == null) return;

        light.colorProperty().unbind();
        light.setColor(null);
    }
}