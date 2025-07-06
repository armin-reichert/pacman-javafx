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

    private final Map<Perspective.ID, Perspective> perspectiveMap = new EnumMap<>(Perspective.ID.class);
    private final SubScene subScene;

    private final ObjectProperty<Perspective.ID> perspectiveIDPy = new SimpleObjectProperty<>(Perspective.ID.TOTAL) {
        @Override
        protected void invalidated() {
            initPerspective();
        }
    };

    public PerspectiveManager(SubScene subScene) {
        this.subScene = requireNonNull(subScene);
        perspectiveMap.put(Perspective.ID.DRONE, new Perspective.Drone());
        perspectiveMap.put(Perspective.ID.TOTAL, new Perspective.Total());
        perspectiveMap.put(Perspective.ID.TRACK_PLAYER, new Perspective.TrackingPlayer());
        perspectiveMap.put(Perspective.ID.NEAR_PLAYER, new Perspective.StalkingPlayer());
    }

    public ObjectProperty<Perspective.ID> perspectiveIDProperty() {
        return perspectiveIDPy;
    }

    public void setPerspective(Perspective.ID perspectiveID) {
        requireNonNull(perspectiveID);
        perspectiveIDProperty().unbind();
        perspectiveIDProperty().set(perspectiveID);
    }

    public void initPerspective() {
        Perspective.ID id = perspectiveIDPy.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).init(subScene);
        } else {
            Logger.error("Cannot init camera perspective with ID '{}'", id);
        }
    }

    public void updatePerspective(GameLevel gameLevel) {
        Perspective.ID id = perspectiveIDPy.get();
        if (id != null && perspectiveMap.containsKey(id)) {
            perspectiveMap.get(id).update(subScene, gameLevel, gameLevel.pac());
        } else {
            Logger.error("Cannot update camera perspective with ID '{}'", id);
        }
    }
}
