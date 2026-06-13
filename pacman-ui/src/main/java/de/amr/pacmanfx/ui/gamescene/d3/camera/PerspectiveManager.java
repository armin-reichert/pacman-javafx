/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3.camera;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.level.GameLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.PerspectiveCamera;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class PerspectiveManager implements Disposable {

    private final Map<PerspectiveID, Perspective<GameLevel>> perspectivesByID;
    private final ObjectProperty<PerspectiveID> activeID;

    public PerspectiveManager(PerspectiveCamera camera) {
        perspectivesByID = new EnumMap<>(PerspectiveID.class);
        activeID = new SimpleObjectProperty<>(PerspectiveID.NEAR_PLAYER);

        // Register all available perspectives
        perspectivesByID.put(PerspectiveID.DRONE,         new DronePerspective(camera));
        perspectivesByID.put(PerspectiveID.TOTAL,         new TotalPerspective(camera));
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER,  new TrackingPlayerPerspective(camera));
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER,   new StalkingPlayerPerspective(camera));

        // Automatically (de)activate control when perspective changes
        activeID.addListener((_, oldID, newID) -> {
            if (oldID != null) {
                Perspective<GameLevel> old = perspectivesByID.get(oldID);
                if (old != null) old.stopControlling();
            }
            if (newID != null) {
                Perspective<GameLevel> next = perspectivesByID.get(newID);
                if (next != null) {
                    next.startControlling();
                } else {
                    Logger.error("Perspective not found for ID: {}", newID);
                }
            } else {
                Logger.error("Cannot activate null perspective ID");
            }
        });
    }

    @Override
    public void dispose() {
        perspectivesByID.clear();
        activeID.set(null);
    }

    public ObjectProperty<PerspectiveID> activeIDProperty() {
        return activeID;
    }

    public Optional<Perspective<GameLevel>> optPerspective(PerspectiveID id) {
        requireNonNull(id);
        return Optional.ofNullable(perspectivesByID.get(id));
    }

    public Optional<Perspective<GameLevel>> currentPerspective() {
        final PerspectiveID id = activeID.get();
        return id == null ? Optional.empty() : optPerspective(id);
    }

    public void updatePerspective(GameLevel level) {
        currentPerspective().ifPresentOrElse(
            perspective -> perspective.update(level),
            () -> Logger.error("Cannot update: no active perspective")
        );
    }
}