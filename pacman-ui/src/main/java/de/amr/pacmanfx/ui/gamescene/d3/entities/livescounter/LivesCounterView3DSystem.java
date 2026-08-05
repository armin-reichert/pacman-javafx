/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3.entities.livescounter;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.livescounter.LivesCounter;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.scene.Node;

public class LivesCounterView3DSystem {

    public static void startTracking(LivesCounter livesCounter, GameEntity gameEntity) {
        final LivesCounterView3DComp livesCounter3D = livesCounter.requireComponent(LivesCounterView3DComp.class);
        final Pac3DViewComp pac3D =  gameEntity.requireComponent(Pac3DViewComp.class);
        for (NodePositionTracker tracker : livesCounter3D.trackers()) {
            tracker.startTrackingTarget(pac3D.root());
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