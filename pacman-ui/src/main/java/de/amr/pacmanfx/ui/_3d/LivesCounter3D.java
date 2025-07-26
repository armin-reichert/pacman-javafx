/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays a Pac-Man shape for each live remaining.
 */
public class LivesCounter3D extends Group implements Disposable {

    private final ObjectProperty<Color>         pillarColorProperty = new SimpleObjectProperty<>(Color.grayRgb(120));
    private final ObjectProperty<PhongMaterial> pillarMaterialProperty = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty                pillarHeightProperty = new SimpleDoubleProperty(8);

    private final ObjectProperty<Color>         plateColorProperty = new SimpleObjectProperty<>(Color.grayRgb(180));
    private final ObjectProperty<PhongMaterial> plateMaterialProperty = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty                plateThicknessProperty = new SimpleDoubleProperty(1);
    private final DoubleProperty                plateRadiusProperty = new SimpleDoubleProperty(6);

    private final IntegerProperty               livesCountProperty = new SimpleIntegerProperty(0);

    private final List<NodeTracker> trackers;
    private final PointLight light = new PointLight();

    private static class NodeTracker {
        private final AnimationTimer timer;
        private final Node observer;
        private Node target;

        public NodeTracker(Node observer) {
            this.observer = requireNonNull(observer);
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateAngle();
                }
            };
        }

        public void startTracking(Node target) {
            this.target = requireNonNull(target);
            timer.start();
        }

        public void stopTracking() {
            timer.stop();
        }

        private void updateAngle() {
            Point2D targetPos = positionXY(target);
            Point2D shapePos = positionXY(observer);
            Point2D arrow = (targetPos.subtract(shapePos)).normalize();
            double phi = Math.toDegrees(Math.atan2(arrow.getX(), arrow.getY()));
            double rotate = -90 - phi;
            observer.setRotationAxis(Rotate.Z_AXIS);
            observer.setRotate(rotate);
        }

        private Point2D positionXY(Node node) {
            return node.localToScene(Point2D.ZERO);
        }
    }

    public void startTracking(Node target) {
        for (NodeTracker tracker : trackers) {
            tracker.startTracking(target);
        }
    }

    public void stopTracking() {
        for (NodeTracker tracker : trackers) {
            tracker.stopTracking();
        }
    }

    public LivesCounter3D(AnimationRegistry animationRegistry, Node[] pacShapeArray) {
        requireNonNull(animationRegistry);
        pillarMaterialProperty.bind(pillarColorProperty.map(Ufx::coloredPhongMaterial));
        plateMaterialProperty.bind((plateColorProperty.map(Ufx::coloredPhongMaterial)));

        light.setMaxRange  (TS * (pacShapeArray.length + 1));
        light.setTranslateX(TS * (pacShapeArray.length - 1));
        light.setTranslateY(TS * (-1));
        light.translateZProperty().bind(pillarHeightProperty.add(20).multiply(-1));

        var standsGroup = new Group();
        for (int i = 0; i < pacShapeArray.length; ++i) {
            final Node pacShape = pacShapeArray[i];
            final int x = i * 2 * TS;
            final double shapeRadius = 0.5 * pacShape.getBoundsInParent().getHeight(); // take scale transform into account!

            pacShape.setUserData(i);
            pacShape.setTranslateX(x);
            pacShape.setTranslateY(0);
            pacShape.visibleProperty().bind(livesCountProperty.map(count -> count.intValue() > (int) pacShape.getUserData()));

            var pillar = new Cylinder(1, 0.1);
            pillar.heightProperty().bind(pillarHeightProperty.add(i % 2 == 0 ? 0 : 4));
            pillar.materialProperty().bind(pillarMaterialProperty);
            pillar.setTranslateX(x);
            pillar.translateZProperty().bind(pillar.heightProperty().multiply(-0.5));
            pillar.setRotationAxis(Rotate.X_AXIS);
            pillar.setRotate(90);

            var podium = new Cylinder();
            podium.radiusProperty().bind(plateRadiusProperty);
            podium.heightProperty().bind(plateThicknessProperty);
            podium.materialProperty().bind(plateMaterialProperty);
            podium.setTranslateX(x);
            podium.translateZProperty().bind(pillar.heightProperty().add(plateThicknessProperty).negate());
            podium.setRotationAxis(Rotate.X_AXIS);
            podium.setRotate(90);

            Group stand = new Group(pillar, podium);
            standsGroup.getChildren().add(stand);

            // let Pac shape sit on top of plate
            pacShape.translateZProperty().bind(pillar.heightProperty().add(plateThicknessProperty).add(shapeRadius).negate());

            getChildren().add(pacShape);
        }
        getChildren().addAll(standsGroup, light);

        trackers = new ArrayList<>();
        for (Node shape : pacShapeArray) {
            NodeTracker rotator = new NodeTracker(shape);
            trackers.add(rotator);
        }
    }

    public IntegerProperty livesCountProperty() { return livesCountProperty; }
    public ObjectProperty<Color> pillarColorProperty() { return pillarColorProperty; }
    public ObjectProperty<Color> plateColorProperty() { return plateColorProperty; }
    public PointLight light() {
        return light;
    }

    @Override
    public void dispose() {
        stopTracking();
        livesCountProperty.unbind();
        pillarHeightProperty.unbind();
        pillarMaterialProperty.unbind();
        pillarColorProperty.unbind();
        plateColorProperty.unbind();
        plateThicknessProperty.unbind();
        plateRadiusProperty.unbind();
        plateMaterialProperty.unbind();
        light.translateZProperty().unbind();
    }

}