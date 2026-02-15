/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import de.amr.pacmanfx.uilib.animation.SwirlAnimation;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;

/**
 * 3D representation of the ghost house inside the maze.
 * <p>
 * This class wraps an {@link ArcadeHouse3D} instance and manages additional
 * 3D effects associated with the house, such as the swirl animations that
 * appear above the ghost revival tiles. It also reacts to house state changes
 * (e.g., door opening) and exposes convenience accessors for rendering and
 * animation control.
 * <p>
 * The {@code MazeHouse3D} instance is owned by {@code GameLevel3D}, which
 * inserts the house and swirl groups into the correct rendering layers.
 * <p>
 * Instances must be disposed via {@link #dispose()} to remove listeners and
 * release animation resources.
 */
public class MazeHouse3D implements Disposable {

    /** The 3D model of the ghost house. */
    private final ArcadeHouse3D arcadeHouse3D;

    /** Listener that triggers the door animation when the house opens. */
    private final ChangeListener<Boolean> houseOpenListener;

    /** Swirl animations above the ghost revival tiles. */
    private List<SwirlAnimation> swirlAnimations = new ArrayList<>(3);

    /**
     * Creates a new 3D ghost house with swirl animations positioned above the
     * ghost revival tiles.
     *
     * @param prefs            user preferences for 3D rendering parameters
     * @param colorScheme      color scheme of the world map
     * @param animationRegistry registry for creating and tracking animations
     * @param house            the logical ghost house from the game world
     */
    public MazeHouse3D(
            PreferencesManager prefs,
            WorldMapColorScheme colorScheme,
            AnimationRegistry animationRegistry,
            House house)
    {
        final Vector2i[] ghostRevivalTiles = {
                house.ghostRevivalTile(CYAN_GHOST_BASHFUL),
                house.ghostRevivalTile(PINK_GHOST_SPEEDY),
                house.ghostRevivalTile(ORANGE_GHOST_POKEY)
        };

        // Revival tile is the left tile of the pair. The 3D swirl center is one tile right and half a tile down.
        final Vector2f[] ghostRevivalPositionCenters = Stream.of(ghostRevivalTiles)
                .map(tile -> tile.scaled((float) TS).plus(TS, HTS))
                .toArray(Vector2f[]::new);

        arcadeHouse3D = new ArcadeHouse3D(
                animationRegistry,
                house,
                prefs.getFloat("3d.house.base_height"),
                prefs.getFloat("3d.house.wall_thickness"),
                prefs.getFloat("3d.house.opacity")
        );

        // Apply color scheme and rendering parameters
        arcadeHouse3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        arcadeHouse3D.wallBaseHeightProperty().set(prefs.getFloat("3d.house.base_height"));
        arcadeHouse3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        arcadeHouse3D.setDoorColor(Color.valueOf(colorScheme.door()));
        arcadeHouse3D.setDoorSensitivity(prefs.getFloat("3d.house.sensitivity"));

        // Door animation listener
        houseOpenListener = (_, _, open) -> {
            if (open) {
                arcadeHouse3D.doorsOpenCloseAnimation().playFromStart();
            }
        };
        arcadeHouse3D.openProperty().addListener(houseOpenListener);

        // Create swirl animations above the revival tiles
        for (int i = 0; i < ghostRevivalPositionCenters.length; ++i) {
            var animation = new SwirlAnimation(animationRegistry, "Swirl_%d".formatted(i));
            swirlAnimations.add(animation);
            animation.swirlGroup().setTranslateX(ghostRevivalPositionCenters[i].x());
            animation.swirlGroup().setTranslateY(ghostRevivalPositionCenters[i].y());
        }
    }

    /**
     * Returns the property controlling the base height of the house walls.
     *
     * @return the wall base height property
     */
    public DoubleProperty wallBaseHeightProperty() {
        return arcadeHouse3D.wallBaseHeightProperty();
    }

    /**
     * Returns the root node of the 3D house model.
     *
     * @return the root {@link Node} of the house
     */
    public Node root() {
        return arcadeHouse3D;
    }

    /**
     * @return the swirl animations above the ghost revival tiles
     */
    public List<SwirlAnimation> swirlAnimations() {
        return swirlAnimations;
    }

    /**
     * @return the group containing the animated house doors
     */
    public Group doors() {
        return arcadeHouse3D.doors();
    }

    /**
     * Starts all swirl animations from the beginning.
     */
    public void startSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(RegisteredAnimation::playFromStart);
            Logger.info("Swirl animations started");
        }
    }

    /**
     * Stops all swirl animations.
     */
    public void stopSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(RegisteredAnimation::stop);
        }
    }

    /**
     * Disposes all swirl animations and clears their scene graph nodes.
     * <p>
     * After calling this method, the swirl animation list becomes {@code null}.
     */
    public void deleteSwirlAnimations() {
        if (swirlAnimations != null) {
            for (var swirlAnimation : swirlAnimations) {
                swirlAnimation.stop();
                swirlAnimation.swirlGroup().getChildren().clear();
                swirlAnimation.dispose();
            }
            swirlAnimations.clear();
            swirlAnimations = null;
            Logger.info("Disposed swirl animations");
        }
    }

    /**
     * Updates the 3D house model based on the current game level state.
     *
     * @param level the current game level
     */
    public void update(GameLevel level) {
        arcadeHouse3D.update(level);
    }

    /**
     * Hides the house doors (used during certain transitions or effects).
     */
    public void hideDoors() {
        arcadeHouse3D.setDoorsVisible(false);
    }

    /**
     * Disposes this 3D house, removes listeners, and releases all animation resources.
     */
    @Override
    public void dispose() {
        arcadeHouse3D.openProperty().removeListener(houseOpenListener);
        Logger.info("Removed 'house open' listener");
        arcadeHouse3D.dispose();
        Logger.info("Disposed 3D house");
        deleteSwirlAnimations();
    }
}
