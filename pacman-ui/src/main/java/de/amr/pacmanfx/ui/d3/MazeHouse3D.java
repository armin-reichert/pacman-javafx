/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.ArcadeHouse;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.d3.config.HouseConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.ArcadeHouse3D;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

public class MazeHouse3D implements Disposable {

    /** The 3D model of the ghost house. */
    private final ArcadeHouse3D arcadeHouse3D;

    /** Listener that triggers the door animation when the house opens. */
    private final ChangeListener<Boolean> houseOpenListener;

    /**
     * Creates a new 3D ghost house with swirl animations positioned above the
     * ghost revival tiles.
     *
     * @param colorScheme      color scheme of the world map
     * @param animationRegistry registry for creating and tracking animations
     * @param house            the logical ghost house from the game world
     */
    public MazeHouse3D(
        WorldMapColorScheme colorScheme,
        HouseConfig3D config3D,
        AnimationRegistry animationRegistry,
        ArcadeHouse house)
    {
        arcadeHouse3D = new ArcadeHouse3D(
            animationRegistry,
            house,
            config3D.baseHeight(),
            config3D.wallThickness(),
            config3D.opacity()
        );

        // Apply color scheme and rendering parameters
        arcadeHouse3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        arcadeHouse3D.wallBaseHeightProperty().set(config3D.baseHeight());
        arcadeHouse3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        arcadeHouse3D.setDoorColor(Color.valueOf(colorScheme.door()));
        arcadeHouse3D.setDoorSensitivity(config3D.sensitivity());

        // Door animation listener
        houseOpenListener = (_, _, open) -> {
            if (open) {
                arcadeHouse3D.doorsOpenCloseAnimation().playFromStart();
            }
        };
        arcadeHouse3D.openProperty().addListener(houseOpenListener);
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
     * @return the group containing the animated house doors
     */
    public Group doors() {
        return arcadeHouse3D.doors();
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
    }
}
