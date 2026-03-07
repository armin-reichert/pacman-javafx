/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
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

/**
 * Manages all available camera perspectives in the 3D play scene.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Creating and holding instances of all {@link Perspective} implementations</li>
 *   <li>Tracking the currently active perspective via an {@link ObjectProperty}</li>
 *   <li>Automatically starting/stopping control when the active perspective changes</li>
 *   <li>Providing update calls to the active perspective each frame</li>
 *   <li>Exposing drone-specific control actions (climb, descend, reset) that are only enabled
 *       when the drone perspective is active</li>
 * </ul>
 * <p>
 * The manager is used by {@link PlayScene3D} to delegate all camera-related logic,
 * keeping the scene class cleaner and more focused.
 * <p>
 * Drone actions are package-visible so they can be directly bound to keyboard/scroll
 * inputs from the play scene.
 *
 * @see Perspective
 * @see DronePerspective
 * @see PlayScene3D
 */
public class PerspectiveManager implements Disposable {

    private final Map<PerspectiveID, Perspective<GameLevel>> perspectivesByID;
    private final ObjectProperty<PerspectiveID> activeID;

    /** Action to make the drone climb (increase height) */
    final GameAction actionDroneClimb;
    /** Action to make the drone descend (decrease height) */
    final GameAction actionDroneDescent;
    /** Action to reset the drone to its default height */
    final GameAction actionDroneReset;

    /**
     * Creates a new perspective manager and initializes all supported camera perspectives.
     *
     * @param camera the perspective camera that all views will control
     */
    public PerspectiveManager(PerspectiveCamera camera) {
        perspectivesByID = new EnumMap<>(PerspectiveID.class);
        activeID = new SimpleObjectProperty<>(PerspectiveID.NEAR_PLAYER);

        // Register all available perspectives
        perspectivesByID.put(PerspectiveID.DRONE,         new DronePerspective(camera));
        perspectivesByID.put(PerspectiveID.TOTAL,         new TotalPerspective(camera));
        perspectivesByID.put(PerspectiveID.TRACK_PLAYER,  new TrackingPlayerPerspective(camera));
        perspectivesByID.put(PerspectiveID.NEAR_PLAYER,   new StalkingPlayerPerspective(camera));

        // Initialize drone control actions
        actionDroneClimb   = createDroneAction("DRONE_CLIMB",   DronePerspective::moveUp);
        actionDroneDescent = createDroneAction("DRONE_DESCENT", DronePerspective::moveDown);
        actionDroneReset   = createDroneAction("DRONE_RESET",   DronePerspective::moveDefaultHeight);

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

    /**
     * Releases all held references. Called when the 3D scene is disposed.
     */
    @Override
    public void dispose() {
        perspectivesByID.clear();
        activeID.set(null);
    }

    /**
     * Returns the property that holds the currently active perspective identifier.
     * <p>
     * This property is typically bound to {@code GameUI.PROPERTY_3D_PERSPECTIVE_ID}.
     *
     * @return the active perspective ID property
     */
    public ObjectProperty<PerspectiveID> activeIDProperty() {
        return activeID;
    }

    /**
     * Returns the currently active perspective, if any.
     *
     * @return an Optional containing the active perspective, or empty if none is active
     */
    public Optional<Perspective<GameLevel>> currentPerspective() {
        PerspectiveID id = activeID.get();
        return (id != null) ? Optional.ofNullable(perspectivesByID.get(id)) : Optional.empty();
    }

    /**
     * Updates the currently active perspective with the latest game level state.
     * <p>
     * Called once per frame from {@link PlayScene3D#update(Game)}.
     *
     * @param level the current game level
     */
    public void updatePerspective(GameLevel level) {
        currentPerspective().ifPresentOrElse(
            perspective -> perspective.update(level),
            () -> Logger.error("Cannot update: no active perspective")
        );
    }

    /**
     * Factory method for creating drone-specific control actions.
     * <p>
     * The returned action is only enabled when the drone perspective is active.
     *
     * @param name             the action name (for debugging/identification)
     * @param perspectiveAction the function to invoke on the drone perspective
     * @return a new {@link GameAction} instance
     */
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