/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer3D;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.opaqueColor;
import static de.amr.pacmanfx.uilib.Ufx.pauseSec;
import static java.util.Objects.requireNonNull;

public class Maze3D extends Group {

    private final DoubleProperty obstacleBaseHeightPy = new SimpleDoubleProperty(Settings3D.OBSTACLE_3D_BASE_HEIGHT);
    private final DoubleProperty wallOpacityPy = new SimpleDoubleProperty(1);
    private final DoubleProperty houseBaseHeightPy = new SimpleDoubleProperty(Settings3D.HOUSE_3D_BASE_HEIGHT);
    private final BooleanProperty houseLightOnPy = new SimpleBooleanProperty(false);

    private final PhongMaterial wallTopMaterial;
    private final Color wallBaseColor;
    private final Color wallTopColor;

    private final ArcadeHouse3D house3D;

    private final ManagedAnimation wallColorFlashingAnimation;
    private final ManagedAnimation wallsDisappearingAnimation;

    public Maze3D(GameLevel level, WorldMapColorScheme colorScheme, AnimationManager animationManager) {
        requireNonNull(animationManager);

        Logger.info("Build 3D maze for map with URL '{}'", level.worldMap().url());

        wallBaseColor = colorScheme.stroke();
        // Add some contrast with floor if wall fill color is black:
        wallTopColor = colorScheme.fill().equals(Color.BLACK) ? Color.grayRgb(42) : colorScheme.fill();

        PhongMaterial wallBaseMaterial = new PhongMaterial();
        wallBaseMaterial.diffuseColorProperty().bind(Bindings.createObjectBinding(
            () -> opaqueColor(wallBaseColor, wallOpacityPy.get()), wallOpacityPy
        ));
        wallBaseMaterial.specularColorProperty().bind(wallBaseMaterial.diffuseColorProperty().map(Color::brighter));

        wallTopMaterial = new PhongMaterial();
        wallTopMaterial.setDiffuseColor(wallTopColor);
        wallTopMaterial.setSpecularColor(wallTopColor.brighter());

        PhongMaterial cornerBaseMaterial = new PhongMaterial();
        cornerBaseMaterial.setDiffuseColor(wallBaseColor); // for now use same color
        cornerBaseMaterial.specularColorProperty().bind(cornerBaseMaterial.diffuseColorProperty().map(Color::brighter));

        PhongMaterial cornerTopMaterial = new PhongMaterial();
        cornerTopMaterial.setDiffuseColor(wallTopColor);
        cornerTopMaterial.specularColorProperty().bind(cornerTopMaterial.diffuseColorProperty().map(Color::brighter));

        TerrainMapRenderer3D r3D = new TerrainMapRenderer3D();
        r3D.setWallBaseHeightProperty(obstacleBaseHeightPy);
        r3D.setWallTopHeight(Settings3D.OBSTACLE_3D_TOP_HEIGHT);
        r3D.setWallTopMaterial(wallTopMaterial);
        r3D.setCornerBaseMaterial(cornerBaseMaterial);
        r3D.setCornerTopMaterial(wallTopMaterial); // for now such that power animation also affects corner top

        //TODO check this:
        obstacleBaseHeightPy.set(PY_3D_WALL_HEIGHT.get());

        for (Obstacle obstacle : level.worldMap().obstacles()) {
            Vector2i tile = tileAt(obstacle.startPoint().toVector2f());
            if (level.house().isPresent() && !level.house().get().isTileInHouseArea(tile)) {
                r3D.setWallThickness(Settings3D.OBSTACLE_3D_THICKNESS);
                r3D.setWallBaseMaterial(wallBaseMaterial);
                r3D.setWallTopMaterial(wallTopMaterial);
                r3D.renderObstacle3D(this, obstacle, isWorldBorder(level.worldMap(), obstacle));
            }
        }

        house3D = new ArcadeHouse3D(
                animationManager,
                level,
                r3D,
                colorScheme.fill(), colorScheme.stroke(), colorScheme.door(),
                Settings3D.HOUSE_3D_OPACITY,
                houseBaseHeightPy,
                Settings3D.HOUSE_3D_WALL_TOP_HEIGHT,
                Settings3D.HOUSE_3D_WALL_THICKNESS,
                houseLightOnPy);

        getChildren().add(house3D.root()); //TODO check this
        house3D.door3D().drawModeProperty().bind(PY_3D_DRAW_MODE);

        PY_3D_WALL_HEIGHT.addListener((py, ov, nv) -> obstacleBaseHeightPy.set(nv.doubleValue()));
        wallOpacityPy.bind(PY_3D_WALL_OPACITY);

        wallColorFlashingAnimation = new ManagedAnimation(animationManager, "MazeWallColorFlashing") {
            @Override
            protected Animation createAnimation() {
                return new MaterialColorAnimation(Duration.seconds(0.25), wallTopMaterial, wallTopColor, wallBaseColor);
            }
        };

        wallsDisappearingAnimation = new ManagedAnimation(animationManager, "Maze_WallsDisappearing") {
            @Override
            protected Animation createAnimation() {
                var totalDuration = Duration.seconds(1);
                var houseDisappears = new Timeline(
                    new KeyFrame(totalDuration.multiply(0.33), new KeyValue(houseBaseHeightPy, 0, Interpolator.EASE_IN)));
                var obstaclesDisappear = new Timeline(
                    new KeyFrame(totalDuration.multiply(0.33), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_IN)));
                var animation = new SequentialTransition(houseDisappears, obstaclesDisappear);
                animation.setOnFinished(e -> setVisible(false));
                return animation;
            }
        };
    }

    // Maze flashing animation

    public Animation createMazeFlashAnimation(int numFlashes) {
        if (numFlashes == 0) {
            return pauseSec(1.0);
        }
        var animation = new Timeline(
            new KeyFrame(Duration.millis(125), new KeyValue(obstacleBaseHeightPy, 0, Interpolator.EASE_BOTH)));
        animation.setAutoReverse(true);
        animation.setCycleCount(2 * numFlashes);
        return animation;
    }


    public ManagedAnimation wallColorFlashingAnimation() {
        return wallColorFlashingAnimation;
    }

    public ManagedAnimation wallsDisappearingAnimation() {
        return wallsDisappearingAnimation;
    }

    public void setHouseLightOn(boolean on) {
        houseLightOnPy.set(on);
    }

    public Door3D door3D() {
        return house3D.door3D();
    }

    private boolean isWorldBorder(WorldMap worldMap, Obstacle obstacle) {
        Vector2i start = obstacle.startPoint();
        if (obstacle.isClosed()) {
            return start.x() == TS || start.y() == GameLevel.EMPTY_ROWS_OVER_MAZE * TS + HTS;
        } else {
            return start.x() == 0 || start.x() == worldMap.numCols() * TS;
        }
    }
}