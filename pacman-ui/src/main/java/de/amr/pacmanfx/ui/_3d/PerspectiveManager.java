/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.GameLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class PerspectiveManager {

    private final Map<PerspectiveID, Perspective> perspectiveMap = new EnumMap<>(PerspectiveID.class);
    private final SubScene subScene;

    private final ObjectProperty<PerspectiveID> perspectiveIDPy = new SimpleObjectProperty<>(PerspectiveID.TOTAL) {
        @Override
        protected void invalidated() {
            initPerspective();
        }
    };

    public PerspectiveManager(SubScene subScene) {
        this.subScene = requireNonNull(subScene);
        perspectiveMap.put(PerspectiveID.DRONE, new Perspective.Drone());
        perspectiveMap.put(PerspectiveID.TOTAL, new Perspective.Total());
        perspectiveMap.put(PerspectiveID.TRACK_PLAYER, new Perspective.TrackingPlayer());
        perspectiveMap.put(PerspectiveID.NEAR_PLAYER, new Perspective.StalkingPlayer());
    }

    public ObjectProperty<PerspectiveID> perspectiveIDProperty() {
        return perspectiveIDPy;
    }

    public void setPerspective(PerspectiveID perspectiveID) {
        requireNonNull(perspectiveID);
        perspectiveIDProperty().unbind();
        perspectiveIDProperty().set(perspectiveID);
    }

    public void initPerspective() {
        PerspectiveID id = perspectiveIDPy.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).init(subScene);
        } else {
            Logger.error("Cannot init camera perspective with ID '{}'", id);
        }
    }

    public void updatePerspective(GameLevel gameLevel) {
        PerspectiveID id = perspectiveIDPy.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).update(subScene, gameLevel, gameLevel.pac());
        } else {
            Logger.error("Cannot update camera perspective with ID '{}'", id);
        }
    }
}
