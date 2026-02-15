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

public class MazeHouse3D implements Disposable {

    private final ArcadeHouse3D arcadeHouse3D;
    private final ChangeListener<Boolean> houseOpenListener;
    private List<SwirlAnimation> swirlAnimations = new ArrayList<>(3);

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

        // Note: revival tile is the left of the pair of tiles in the house where the ghost is placed. The center
        //       of the 3D shape is one tile to the right and a half tile to the bottom from the tile origin.
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

        arcadeHouse3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        arcadeHouse3D.wallBaseHeightProperty().set(prefs.getFloat("3d.house.base_height"));
        arcadeHouse3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        arcadeHouse3D.setDoorColor(Color.valueOf(colorScheme.door()));
        arcadeHouse3D.setDoorSensitivity(prefs.getFloat("3d.house.sensitivity"));

        houseOpenListener = (_, _, open) -> {
            if (open) {
                arcadeHouse3D.doorsOpenCloseAnimation().playFromStart();
            }
        };
        arcadeHouse3D.openProperty().addListener(houseOpenListener);


        // These groups are added to their parent in GameLevel3D to ensure correct ordering!
        for (int i = 0; i < ghostRevivalPositionCenters.length; ++i) {
            var animation = new SwirlAnimation(animationRegistry, "Swirl_%d".formatted(i));
            swirlAnimations.add(animation);
            animation.swirlGroup().setTranslateX(ghostRevivalPositionCenters[i].x());
            animation.swirlGroup().setTranslateY(ghostRevivalPositionCenters[i].y());
        }

    }

    public DoubleProperty wallBaseHeightProperty() {
        return arcadeHouse3D.wallBaseHeightProperty();
    }

    public Node root() {
        return arcadeHouse3D;
    }

    public List<SwirlAnimation> swirlAnimations() {
        return swirlAnimations;
    }

    public Group doors() {
        return arcadeHouse3D.doors();
    }

    public void startSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(RegisteredAnimation::playFromStart);
            Logger.info("Swirl animations started");
        }
    }

    public void stopSwirlAnimations() {
        if (swirlAnimations != null) {
            swirlAnimations.forEach(RegisteredAnimation::stop);
        }
    }

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

    public void update(GameLevel level) {
        arcadeHouse3D.update(level);
    }

    public void hideDoors() {
        arcadeHouse3D.setDoorsVisible(false);
    }

    @Override
    public void dispose() {
        arcadeHouse3D.openProperty().removeListener(houseOpenListener);
        Logger.info("Removed 'house open' listener");
        arcadeHouse3D.dispose();
        Logger.info("Disposed 3D house");
        deleteSwirlAnimations();
    }
}
