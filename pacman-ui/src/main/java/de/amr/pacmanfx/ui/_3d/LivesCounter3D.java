/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Displays a Pac-Man shape sitting on a pillar for each live remaining.
 */
public class LivesCounter3D extends Group implements Disposable {

    private final ObjectProperty<Color> pillarColor = new SimpleObjectProperty<>(Color.grayRgb(120));
    private final ObjectProperty<PhongMaterial> pillarMaterial = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty pillarHeight = new SimpleDoubleProperty(8);

    private final DoubleProperty plateThickness = new SimpleDoubleProperty(1);
    private final DoubleProperty plateRadius = new SimpleDoubleProperty(6);
    private final ObjectProperty<Color> plateColor = new SimpleObjectProperty<>(Color.grayRgb(180));
    private final ObjectProperty<PhongMaterial> plateMaterial = new SimpleObjectProperty<>(new PhongMaterial());

    private final IntegerProperty livesCount = new SimpleIntegerProperty(0);

    private final List<NodePositionTracker> trackers;

    public void startTracking(Node target) {
        for (NodePositionTracker tracker : trackers) {
            tracker.startTrackingTarget(target);
        }
    }

    public void stopTracking() {
        for (NodePositionTracker tracker : trackers) {
            tracker.stopTracking();
        }
    }

    private class Stand extends Group {
        Cylinder pillar;
        Cylinder podium;

        public Stand() {
            pillar = new Cylinder(1, 0.1);
            pillar.materialProperty().bind(pillarMaterial);
            pillar.translateZProperty().bind(pillar.heightProperty().multiply(-0.5));
            pillar.setRotationAxis(Rotate.X_AXIS);
            pillar.setRotate(90);

            podium = new Cylinder();
            podium.radiusProperty().bind(plateRadius);
            podium.heightProperty().bind(plateThickness);
            podium.materialProperty().bind(plateMaterial);
            podium.translateZProperty().bind(pillar.heightProperty().add(plateThickness).negate());
            podium.setRotationAxis(Rotate.X_AXIS);
            podium.setRotate(90);

            getChildren().setAll(pillar, podium);
        }
    }

    public LivesCounter3D(AnimationRegistry animationRegistry, Node[] pacShapeArray) {
        requireNonNull(animationRegistry);
        pillarMaterial.bind(pillarColor.map(Ufx::defaultPhongMaterial));
        plateMaterial.bind((plateColor.map(Ufx::defaultPhongMaterial)));

        var standsGroup = new Group();
        getChildren().addAll(standsGroup);
        for (int i = 0; i < pacShapeArray.length; ++i) {
            final int x = i * 2 * TS;

            var stand = new Stand();
            stand.pillar.heightProperty().bind(pillarHeight.add(i % 2 == 0 ? 0 : 4));
            stand.setTranslateX(x);
            standsGroup.getChildren().add(stand);

            final Node pacShape = pacShapeArray[i];
            final double shapeRadius = 0.5 * pacShape.getBoundsInParent().getHeight(); // take scale transform into account!
            pacShape.setUserData(i);
            pacShape.setTranslateX(x);
            pacShape.setTranslateY(0);
            pacShape.visibleProperty().bind(livesCount.map(count -> count.intValue() > (int) pacShape.getUserData()));
            // let Pac shape sit on top of plate
            pacShape.translateZProperty().bind(stand.pillar.heightProperty().add(plateThickness).add(shapeRadius).negate());

            getChildren().add(pacShape);
        }

        trackers = new ArrayList<>();
        for (Node pacShape : pacShapeArray) {
            trackers.add(new NodePositionTracker(pacShape));
        }
    }

    public IntegerProperty livesCountProperty() {
        return livesCount;
    }

    public ObjectProperty<Color> pillarColorProperty() {
        return pillarColor;
    }

    public ObjectProperty<Color> plateColorProperty() {
        return plateColor;
    }

    @Override
    public void dispose() {
        stopTracking();
        livesCount.unbind();
        pillarHeight.unbind();
        pillarMaterial.unbind();
        pillarColor.unbind();
        plateColor.unbind();
        plateThickness.unbind();
        plateRadius.unbind();
        plateMaterial.unbind();
    }
}