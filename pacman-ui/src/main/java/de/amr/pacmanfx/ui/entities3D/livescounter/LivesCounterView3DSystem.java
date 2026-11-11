/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter;

import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.Factory3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.NodePositionTracker;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import javafx.scene.Group;
import javafx.scene.Node;

import static de.amr.basics.TileDimension.tilesPx;
import static java.util.Objects.requireNonNull;

public class LivesCounterView3DSystem {

    public LivesCounterView3DSystem() {}

    public LivesCounterView3D createView3D(Factory3D factory3D, WorldSettings settings) {
        requireNonNull(factory3D);
        requireNonNull(settings);

        final LivesCounterView3D view3D = new LivesCounterView3D();

        view3D.setPillarMaterial(Ufx.coloredPhongMaterial(view3D.pillarColor()));
        view3D.setPlateMaterial(Ufx.coloredPhongMaterial(view3D.plateColor()));

        final var standsGroup = new Group();
        view3D.root().getChildren().add(standsGroup);

        for (int i = 0; i < settings.livesCounter().numShapes(); ++i) {
            final Node shape = factory3D.createLivesCounterShape3D(settings);
            view3D.counterShapes().add(shape);

            final float x = i * tilesPx(2);
            final int lift = i % 2 == 0 ? 0 : 4;

            final var stand = new LivesCounterStand(
                view3D.pillarMaterial(),
                view3D.plateMaterial(),
                view3D.plateRadius(),
                view3D.plateThickness()
            );
            stand.pillar().setHeight(view3D.pillarHeight() + lift);
            stand.setTranslateX(x);
            standsGroup.getChildren().add(stand);

            shape.setUserData(i);
            shape.setTranslateX(x);
            shape.setTranslateY(0);
            // let Pac shape sit on top of plate
            final double shapeRadius = 0.5 * shape.getBoundsInParent().getHeight(); // take scale transform into account!
            shape.setTranslateZ(-(stand.pillar().getHeight() + view3D.plateThickness() + shapeRadius));

            view3D.root().getChildren().add(shape);
        }

        for (Node shape : view3D.counterShapes()) {
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
        for (int i = 0; i < view3D.counterShapes().size(); ++i) {
            view3D.counterShapes().get(i).setVisible(i < livesCounter.data().numLivesShown());
        }
    }
}