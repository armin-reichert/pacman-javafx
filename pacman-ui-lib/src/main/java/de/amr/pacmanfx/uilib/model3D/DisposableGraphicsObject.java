/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.Node;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

/**
 * Mixin interface providing default methods for safely cleaning up JavaFX 3D graphics resources.
 * <p>
 * Classes that own or manage {@link Shape3D}, {@link MeshView}, {@link LightBase},
 * {@link Group}, or other {@link Node}-based 3D subtrees should implement this interface.
 * Call the cleanup methods from your {@link #dispose()} implementation to prevent memory
 * leaks, binding accumulation, and stale references in JavaFX 3D scenes.
 * </p>
 *
 * <p>This interface handles the most common cleanup tasks:</p>
 * <ul>
 *   <li>Unbinding and resetting common transforms (translate, rotate, scale)</li>
 *   <li>Unbinding visibility, opacity, effect, and clip properties</li>
 *   <li>Unbinding and nulling materials and meshes on shapes</li>
 *   <li>Recursively cleaning all {@link Shape3D} descendants (including nested groups)</li>
 *   <li>Unbinding and nulling light colors</li>
 * </ul>
 *
 * <p>It intentionally does <strong>not</strong> clean up:</p>
 * <ul>
 *   <li>Animations, timelines, transitions, or key frames</li>
 *   <li>Custom bindings on {@link Group#getTransforms()}</li>
 *   <li>Event listeners, subscriptions, or property change listeners</li>
 *   <li>User data, custom properties, or external references</li>
 * </ul>
 *
 * <p>Typical usage inside {@code dispose()}:</p>
 * <pre>{@code
 *     cleanupShape3D(myCustomShape);
 *     cleanupLight(myPointLight);
 *     cleanupGroup(myRootGroup, true);  // recursive + clears children list
 * }</pre>
 *
 * @see Disposable
 */
public interface DisposableGraphicsObject extends Disposable {

    /**
     * Cleans common properties and bindings of any {@link Node}.
     * <p>
     * Resets and unbinds transforms (translate, rotate, scale), visibility,
     * opacity, effect, and clip properties. Null input is safely ignored.
     * </p>
     *
     * @param node the node to clean, or {@code null}
     */
    default void cleanupNode(Node node) {
        if (node == null) return;

        node.getTransforms().clear();

        // Transforms — most common bindings
        node.translateXProperty().unbind();   node.setTranslateX(0);
        node.translateYProperty().unbind();   node.setTranslateY(0);
        node.translateZProperty().unbind();   node.setTranslateZ(0);

        node.rotateProperty().unbind();       node.setRotate(0);
        node.rotationAxisProperty().unbind(); node.setRotationAxis(Rotate.Z_AXIS);

        node.scaleXProperty().unbind();       node.setScaleX(1.0);
        node.scaleYProperty().unbind();       node.setScaleY(1.0);
        node.scaleZProperty().unbind();       node.setScaleZ(1.0);

        // Visibility & opacity
        node.visibleProperty().unbind();
        node.opacityProperty().unbind();      // group.setOpacity(1.0);  ← optional

        // Effects & clipping
        node.effectProperty().unbind();       node.setEffect(null);
        node.clipProperty().unbind();         node.setClip(null);
    }

    /**
     * Cleans a single {@link Shape3D} or {@link MeshView} by unbinding and nulling
     * its material and (if applicable) mesh. Also cleans common node properties.
     * <p>
     * Null input is safely ignored.
     * </p>
     *
     * @param shape3D the shape to clean, or {@code null}
     */
    default void cleanupShape3D(Shape3D shape3D) {
        if (shape3D == null) return;

        cleanupNode(shape3D);

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
            case Sphere sphere -> sphere.radiusProperty().unbind();
            default -> { /* no specific cleanup needed */ }
        }
    }

    /**
     * Recursively cleans all {@link Shape3D} descendants of the given group (including
     * nested groups), resets common node properties, and optionally clears the group's
     * children list after recursion.
     * <p>
     * Null input is safely ignored.
     * </p>
     *
     * @param group        the root group to clean, or {@code null}
     * @param clearChildren if {@code true}, calls {@code group.getChildren().clear()}
     *                      after recursion (recommended when disposing the entire subtree)
     */
    default void cleanupGroup(Group group, boolean clearChildren) {
        if (group == null) return;

        cleanupNode(group);

        for (Node child : group.getChildren()) {
            if (child instanceof Group childGroup) {
                cleanupGroup(childGroup, clearChildren);
            } else if (child instanceof Shape3D shape3D) {
                cleanupShape3D(shape3D);
            } else {
                cleanupNode(child);
            }
        }

        if (clearChildren) {
            group.getChildren().clear();
        }
    }

    /**
     * Cleans a light source by unbinding and nulling its color property.
     * Also resets common node properties.
     * <p>
     * Null input is safely ignored.
     * </p>
     *
     * @param light the light to clean, or {@code null}
     */
    default void cleanupLight(LightBase light) {
        if (light == null) return;

        cleanupNode(light);

        light.colorProperty().unbind();
        light.setColor(null);
    }
}