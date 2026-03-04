/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.PerspectiveCamera;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class PerspectiveManager implements Disposable {

    private final Map<PerspectiveID, Perspective<GameLevel>> perspectivesByID = new EnumMap<>(PerspectiveID.class);
    private final ObjectProperty<PerspectiveID> activeID = new SimpleObjectProperty<>(PerspectiveID.NEAR_PLAYER);

    // Package-visible for access by play scene
    final GameAction actionDroneClimb;
    final GameAction actionDroneDescent;
    final GameAction actionDroneReset;

    public PerspectiveManager(PerspectiveCamera camera) {
        perspectivesByID.put(PerspectiveID.DRONE, new DronePerspective(camera));
        perspectivesByID.put(PerspectiveID.TOTAL, new TotalPerspective(camera));
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER, new TrackingPlayerPerspective(camera));
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER, new StalkingPlayerPerspective(camera));

        actionDroneClimb   = createDroneAction("DroneClimb", DronePerspective::moveUp);
        actionDroneDescent = createDroneAction("DroneDescent", DronePerspective::moveDown);
        actionDroneReset   = createDroneAction("DroneReset", DronePerspective::moveDefaultHeight);

        activeID.addListener((_, oldID, newID) -> {
            if (oldID != null) {
                perspectivesByID.get(oldID).stopControlling();
            }
            if (newID != null) {
                perspectivesByID.get(newID).startControlling();
            }
            else {
                Logger.error("New perspective ID is NULL!");
            }
        });
    }

    @Override
    public void dispose() {
        perspectivesByID.clear();
    }

    public ObjectProperty<PerspectiveID> activeIDProperty() {
        return activeID;
    }

    public Optional<Perspective<GameLevel>> currentPerspective() {
        return activeID.get() == null
            ? Optional.empty()
            : Optional.of(perspectivesByID.get(activeID.get()));
    }

    public void updatePerspective(GameLevel level) {
        currentPerspective().ifPresentOrElse(
            perspective -> perspective.update(level),
            () -> Logger.error("No perspective is active"))
        ;
    }

    private GameAction createDroneAction(String name, Consumer<DronePerspective> perspectiveAction) {
        return new GameAction(name) {
            @Override
            public void execute(GameUI ui) {
                currentPerspective()
                    .filter(DronePerspective.class::isInstance)
                    .map(DronePerspective.class::cast)
                    .ifPresent(perspectiveAction);
            }
            @Override
            public boolean isEnabled(GameUI ui) {
                return activeID.get() == PerspectiveID.DRONE;
            }
        };
    }
}
