/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.livescounter.system;

import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounter3DViewComp;
import de.amr.pacmanfx.ui.gamescene.d3.animation.NodePositionTracker;
import de.amr.pacmanfx.uilib.entities3d.pac.comp.Pac3DViewComp;

public class LivesCounter3DViewSystem {

    public LivesCounter3DViewSystem() {
    }

    public void startTrackingPac(LivesCounter livesCounter, Pac pac) {
        final LivesCounter3DViewComp livesCounter3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        final Pac3DViewComp pac3D = pac.reqComp(Pac3DViewComp.class);
        for (NodePositionTracker tracker : livesCounter3D.trackers()) {
            tracker.startTrackingTarget(pac3D.root());
        }
    }

    public void stopTrackingPac(LivesCounter livesCounter) {
        final LivesCounter3DViewComp view3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        for (NodePositionTracker tracker : view3D.trackers()) {
            tracker.stopTracking();
        }
    }

    public void update(LivesCounter livesCounter) {
        final LivesCounter3DViewComp view3D = livesCounter.reqComp(LivesCounter3DViewComp.class);
        view3D.livesCountProperty().set(livesCounter.data().numLivesShown() - 1);
    }
}