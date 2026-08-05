/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3.entities.livescounter;

import de.amr.pacmanfx.core.model.entities.livescounter.LivesCounter;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import javafx.scene.Node;

public class LivesCounterView3DSystem {

    public static void startTracking(LivesCounter livesCounter, Node target) {
        final LivesCounterView3DComp view3D = livesCounter.requireComponent(LivesCounterView3DComp.class);
        for (NodePositionTracker tracker : view3D.trackers()) {
            tracker.startTrackingTarget(target);
        }
    }

    public static void stopTracking(LivesCounter livesCounter) {
        final LivesCounterView3DComp view3D = livesCounter.requireComponent(LivesCounterView3DComp.class);
        for (NodePositionTracker tracker : view3D.trackers()) {
            tracker.stopTracking();
        }
    }

    public static void update(LivesCounter livesCounter) {
        final LivesCounterView3DComp view3D = livesCounter.requireComponent(LivesCounterView3DComp.class);
        view3D.livesCountProperty().set(livesCounter.data().numLives() - 1);
    }
}