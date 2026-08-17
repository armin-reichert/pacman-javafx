/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter.comp;


import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class LivesCounter3DViewComp implements EntityComponent, DisposableGraphicsObject {

    private final ObjectProperty<Color> pillarColor = new SimpleObjectProperty<>(Color.grayRgb(200));
    private final ObjectProperty<PhongMaterial> pillarMaterial = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty pillarHeight = new SimpleDoubleProperty(8);

    private final Group root = new Group();

    private final DoubleProperty plateThickness = new SimpleDoubleProperty(1);
    private final DoubleProperty plateRadius = new SimpleDoubleProperty(6);
    private final ObjectProperty<Color> plateColor = new SimpleObjectProperty<>(Color.grayRgb(100));
    private final ObjectProperty<PhongMaterial> plateMaterial = new SimpleObjectProperty<>(new PhongMaterial());

    private final IntegerProperty livesCount = new SimpleIntegerProperty(0);
    private final List<NodePositionTracker> trackers = new ArrayList<>();

    private class Stand extends Group implements DisposableGraphicsObject {
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

        @Override
        public void dispose() {
            cleanupGroup(this, true);
        }
    }

    public LivesCounter3DViewComp(Factory3D factory3D, WorldSettings worldConfig) {
        requireNonNull(factory3D);
        requireNonNull(worldConfig);

        pillarMaterial.bind(pillarColor.map(Ufx::coloredPhongMaterial));
        plateMaterial.bind((plateColor.map(Ufx::coloredPhongMaterial)));

        final var standsGroup = new Group();
        root.getChildren().add(standsGroup);

        final var counterShapes = new Node[worldConfig.livesCounter().numShapes()];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = factory3D.createLivesCounterShape3D(worldConfig);
        }
        for (int i = 0; i < counterShapes.length; ++i) {
            final Node shape = counterShapes[i];

            final float x = i * tilesPx(2);
            final int lift = i % 2 == 0 ? 0 : 4;

            final var stand = new Stand();
            stand.pillar.heightProperty().bind(pillarHeight.add(lift));
            stand.setTranslateX(x);
            standsGroup.getChildren().add(stand);

            shape.setUserData(i);
            shape.setTranslateX(x);
            shape.setTranslateY(0);
            // let Pac shape sit on top of plate
            final double shapeRadius = 0.5 * shape.getBoundsInParent().getHeight(); // take scale transform into account!
            shape.translateZProperty().bind(stand.pillar.heightProperty().add(plateThickness).add(shapeRadius).negate());

            shape.visibleProperty().bind(livesCount.map(count -> count.intValue() > (int) shape.getUserData()));

            root.getChildren().add(shape);
        }

        for (Node shape : counterShapes) {
            trackers.add(new NodePositionTracker(shape));
        }
    }

    public Group root() {
        return root;
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

    public List<NodePositionTracker> trackers() {
        return trackers;
    }

    @Override
    public void dispose() {
//        stopTracking();
        livesCount.unbind();
        pillarHeight.unbind();
        pillarMaterial.unbind();
        pillarColor.unbind();
        plateColor.unbind();
        plateThickness.unbind();
        plateRadius.unbind();
        plateMaterial.unbind();

        cleanupGroup(root, true);
    }

    @Override
    public void reset() {
    }
}
