/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.entities;

import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.EntityConfig;
import de.amr.pacmanfx.ui.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
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
 * Displays for each remaining live a Pac-Man sitting on a pillar tracking the Pac-Man in the maze.
 */
public class LivesCounter3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    private final ObjectProperty<Color> pillarColor = new SimpleObjectProperty<>(Color.grayRgb(120));
    private final ObjectProperty<PhongMaterial> pillarMaterial = new SimpleObjectProperty<>(new PhongMaterial());
    private final DoubleProperty pillarHeight = new SimpleDoubleProperty(8);

    private final DoubleProperty plateThickness = new SimpleDoubleProperty(1);
    private final DoubleProperty plateRadius = new SimpleDoubleProperty(6);
    private final ObjectProperty<Color> plateColor = new SimpleObjectProperty<>(Color.grayRgb(180));
    private final ObjectProperty<PhongMaterial> plateMaterial = new SimpleObjectProperty<>(new PhongMaterial());

    private final IntegerProperty livesCount = new SimpleIntegerProperty(0);
    private final List<NodePositionTracker> trackers = new ArrayList<>();

    private class Stand extends Group implements DisposableGraphicsObject{
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

    public LivesCounter3D(UIConfig uiConfig) {
        requireNonNull(uiConfig);

        final EntityConfig entityConfig = uiConfig.entityConfig();

        pillarMaterial.bind(pillarColor.map(Ufx::coloredPhongMaterial));
        plateMaterial.bind((plateColor.map(Ufx::coloredPhongMaterial)));

        final var standsGroup = new Group();
        getChildren().add(standsGroup);

        final var counterShapes = new Node[entityConfig.livesCounter().capacity()];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = uiConfig.factory3D().createLivesCounterShape3D(uiConfig.entityConfig());
        }
        for (int i = 0; i < counterShapes.length; ++i) {
            final Node shape = counterShapes[i];

            final float x = i * TS(2);
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
            getChildren().add(shape);
        }

        for (Node shape : counterShapes) {
            trackers.add(new NodePositionTracker(shape));
        }
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

        cleanupGroup(this, true);
    }

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

    public IntegerProperty livesCountProperty() {
        return livesCount;
    }

    public ObjectProperty<Color> pillarColorProperty() {
        return pillarColor;
    }

    public ObjectProperty<Color> plateColorProperty() {
        return plateColor;
    }

    /**
     * Updates the lives counter visibility and count based on game state.
     */
    @Override
    public void update(GameLevel level) {
        final GameControl gameControl = level.game().control();
        final boolean oneMore = gameControl.state().nameMatches(GameControl.CommonGameState.STARTING_GAME_OR_LEVEL.name())
            && !level.pac().isVisible();
        final boolean visible = level.game().canStartNewGame();
        int lifeCount = level.game().lifeCount() - 1;
        // when the game starts and Pac-Man is not yet visible, show one more
        if (oneMore) lifeCount += 1;
        livesCountProperty().set(lifeCount);
        setVisible(visible);
    }
}