/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.entities3D.livescounter.system;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounter3DViewComp;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;

public class LivesCounter3DViewSystem {

    public static void startTracking(LivesCounter livesCounter, GameEntity gameEntity) {
        final LivesCounter3DViewComp livesCounter3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        final Pac3DViewComp pac3D =  gameEntity.reqComp(Pac3DViewComp.class);
        for (NodePositionTracker tracker : livesCounter3D.trackers()) {
            tracker.startTrackingTarget(pac3D.root());
        }
    }

    public static void stopTracking(LivesCounter livesCounter) {
        final LivesCounter3DViewComp view3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        for (NodePositionTracker tracker : view3D.trackers()) {
            tracker.stopTracking();
        }
    }

    public static void update(LivesCounter livesCounter) {
        final LivesCounter3DViewComp view3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        view3D.livesCountProperty().set(livesCounter.data().numLives() - 1);
    }
}