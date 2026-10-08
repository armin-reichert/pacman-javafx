/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter.system;

import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.Stand;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import javafx.scene.Group;
import javafx.scene.Node;

import static de.amr.basics.TileDimension.tilesPx;
import static java.util.Objects.requireNonNull;

public class LivesCounterView3DSystem {

    public LivesCounterView3DSystem() {}

    public LivesCounterView3D createView(Factory3D factory3D, WorldSettings settings) {
        requireNonNull(factory3D);
        requireNonNull(settings);

        final LivesCounterView3D view3D = new LivesCounterView3D();

        view3D.pillarMaterialProperty().bind(view3D.pillarColorProperty().map(Ufx::coloredPhongMaterial));
        view3D.plateMaterialProperty().bind((view3D.plateColorProperty().map(Ufx::coloredPhongMaterial)));

        final var standsGroup = new Group();
        view3D.root().getChildren().add(standsGroup);

        final var counterShapes = new Node[settings.livesCounter().numShapes()];
        for (int i = 0; i < counterShapes.length; ++i) {
            counterShapes[i] = factory3D.createLivesCounterShape3D(settings);
        }
        for (int i = 0; i < counterShapes.length; ++i) {
            final Node shape = counterShapes[i];

            final float x = i * tilesPx(2);
            final int lift = i % 2 == 0 ? 0 : 4;

            final var stand = new Stand(
                view3D.pillarMaterialProperty(),
                view3D.plateMaterialProperty(),
                view3D.plateRadiusProperty(),
                view3D.plateThicknessProperty()
            );
            stand.pillar().heightProperty().bind(view3D.pillarHeightProperty().add(lift));
            stand.setTranslateX(x);
            standsGroup.getChildren().add(stand);

            shape.setUserData(i);
            shape.setTranslateX(x);
            shape.setTranslateY(0);
            // let Pac shape sit on top of plate
            final double shapeRadius = 0.5 * shape.getBoundsInParent().getHeight(); // take scale transform into account!
            shape.translateZProperty().bind(
                stand.pillar().heightProperty()
                    .add(view3D.plateThicknessProperty())
                    .add(shapeRadius)
                    .negate());

            shape.visibleProperty().bind(
                view3D.livesCountProperty()
                    .map(count -> count.intValue() > (int) shape.getUserData()));

            view3D.root().getChildren().add(shape);
        }

        for (Node shape : counterShapes) {
            view3D.trackers().add(new NodePositionTracker(shape));
        }

        return view3D;
    }

    public void startTrackingPac(LivesCounter livesCounter, Pac pac) {
        final LivesCounterView3D livesCounter3D = livesCounter.assertComponent(LivesCounterView3D.class);
        final PacView3D pac3D = pac.assertComponent(PacView3D.class);
        for (NodePositionTracker tracker : livesCounter3D.trackers()) {
            tracker.startTrackingTarget(pac3D.root());
        }
    }

    public void stopTrackingPac(LivesCounter livesCounter) {
        final LivesCounterView3D view3D = livesCounter.assertComponent(LivesCounterView3D.class);
        for (NodePositionTracker tracker : view3D.trackers()) {
            tracker.stopTracking();
        }
    }

    public void update(LivesCounter livesCounter) {
        final LivesCounterView3D view3D = livesCounter.assertComponent(LivesCounterView3D.class);
        view3D.livesCountProperty().set(livesCounter.data().numLivesShown() - 1);
    }
}