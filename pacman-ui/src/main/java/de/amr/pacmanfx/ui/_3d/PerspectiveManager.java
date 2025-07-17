/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.PerspectiveCamera;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class PerspectiveManager {

    private final Map<PerspectiveID, Perspective> perspectiveMap = new EnumMap<>(PerspectiveID.class);
    private final PerspectiveCamera camera;

    private final ObjectProperty<PerspectiveID> perspectiveIDProperty = new SimpleObjectProperty<>(PerspectiveID.TOTAL) {
        @Override
        protected void invalidated() {
            initPerspective();
        }
    };

    public PerspectiveManager(PerspectiveCamera camera) {
        this.camera = requireNonNull(camera);
        perspectiveMap.put(PerspectiveID.DRONE, new DronePerspective());
        perspectiveMap.put(PerspectiveID.TOTAL, new TotalPerspective());
        perspectiveMap.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective());
        perspectiveMap.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective());
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveIDProperty;
    }

    public void initPerspective() {
        PerspectiveID id = perspectiveIDProperty.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).init(camera);
        } else {
            Logger.error("Cannot init camera perspective with ID '{}'", id);
        }
    }

    public void updatePerspective(GameLevel gameLevel) {
        PerspectiveID id = perspectiveIDProperty.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).update(camera, gameLevel, gameLevel.pac());
        } else {
            Logger.error("Cannot update camera perspective with ID '{}'", id);
        }
    }
}