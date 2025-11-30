/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.SubSceneProvider;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;
import static de.amr.pacmanfx.ui.api.GameUI.*;

/**
 * Infobox with 3D related settings.
 */
public class InfoBox3DSettings extends InfoBox {

    private static final int MINI_VIEW_MIN_HEIGHT = 200;
    private static final int MINI_VIEW_MAX_HEIGHT = 600;

    private CheckBox cbUsePlayScene3D;
    private ChoiceBox<PerspectiveID> comboPerspectives;
    private CheckBox cbMiniViewVisible;
    private Slider sliderMiniViewSceneHeight;
    private Slider sliderMiniViewOpacity;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public InfoBox3DSettings(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        cbUsePlayScene3D = addCheckBox("3D Play Scene");
        comboPerspectives = addChoiceBox("Perspective", PerspectiveID.values());
        addColorPicker("Light Color", PROPERTY_3D_LIGHT_COLOR);
        addColorPicker("Floor Color", PROPERTY_3D_FLOOR_COLOR);
        addDynamicLabeledValue("Camera",         this::subSceneCameraInfo);
        addDynamicLabeledValue("Sub-scene Size", this::subSceneSizeInfo);
        addDynamicLabeledValue("Scene Size",     this::sceneSizeInfo);

        cbMiniViewVisible = addCheckBox("Mini View", PROPERTY_MINI_VIEW_ON);
        sliderMiniViewSceneHeight = addSlider(
            " - Height",
            MINI_VIEW_MIN_HEIGHT, MINI_VIEW_MAX_HEIGHT,
            PROPERTY_MINI_VIEW_HEIGHT.get(),
            false, false);
        sliderMiniViewOpacity = addSlider(
            " - Opacity",
            0, 100,
            PROPERTY_MINI_VIEW_OPACITY_PERCENT.get(),
            false, false);

        sliderWallHeight = addSlider(
            "Wall Height",
            0, 16,
            PROPERTY_3D_WALL_HEIGHT.get(),
            false, false);
        sliderWallOpacity = addSlider(
            "Wall Opacity",
            0, 1,
            PROPERTY_3D_WALL_OPACITY.get(),
            false, false);

        cbAxesVisible = addCheckBox("Show Axes", PROPERTY_3D_AXES_VISIBLE);
        cbWireframeMode = addCheckBox("Wireframe Mode");

        setTooltip(sliderMiniViewSceneHeight, sliderMiniViewSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderMiniViewOpacity, sliderMiniViewOpacity.valueProperty(), "%.0f %%");

        setEditor(sliderMiniViewSceneHeight, PROPERTY_MINI_VIEW_HEIGHT);
        setEditor(sliderMiniViewOpacity, PROPERTY_MINI_VIEW_OPACITY_PERCENT);
        setEditor(sliderWallHeight, PROPERTY_3D_WALL_HEIGHT);
        setEditor(sliderWallOpacity, PROPERTY_3D_WALL_OPACITY);
        setEditor(comboPerspectives, PROPERTY_3D_PERSPECTIVE_ID);

        cbUsePlayScene3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(ui));
        cbWireframeMode.setOnAction(e -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(ui));
    }

    @Override
    public void update() {
        super.update();
        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE_ID.get());
        sliderMiniViewSceneHeight.setValue(PROPERTY_MINI_VIEW_HEIGHT.get());
        sliderMiniViewOpacity.setValue(PROPERTY_MINI_VIEW_OPACITY_PERCENT.get());
        sliderWallHeight.setValue(PROPERTY_3D_WALL_HEIGHT.get());
        sliderWallOpacity.setValue(PROPERTY_3D_WALL_OPACITY.get());
        cbUsePlayScene3D.setSelected(PROPERTY_3D_ENABLED.get());
        cbMiniViewVisible.setSelected(PROPERTY_MINI_VIEW_ON.getValue());
        comboPerspectives.setValue(PROPERTY_3D_PERSPECTIVE_ID.get());
        cbAxesVisible.setSelected(PROPERTY_3D_AXES_VISIBLE.get());
        cbWireframeMode.setSelected(PROPERTY_3D_DRAW_MODE.get() == DrawMode.LINE);
    }

    private Optional<SubScene> optSubScene() {
        GameScene gameScene = ui.currentGameScene().orElse(null);
        return gameScene instanceof SubSceneProvider subSceneProvider
            ? Optional.of(subSceneProvider.subScene()) : Optional.empty();
    }

    private String subSceneSizeInfo() {
        return optSubScene().map(ss -> "%.0fx%.0f".formatted(ss.getWidth(), ss.getHeight())).orElse(NO_INFO);
    }

    private String subSceneCameraInfo() {
        return optSubScene()
            .map(ss -> String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                ss.getCamera().getRotate(),
                ss.getCamera().getTranslateX(),
                ss.getCamera().getTranslateY(),
                ss.getCamera().getTranslateZ()))
            .orElse(NO_INFO);
    }

    private String sceneSizeInfo() {
        if (ui.currentGameScene().isEmpty()) return NO_INFO;

        final Game game = ui.context().currentGame();
        final GameScene gameScene = ui.currentGameScene().get();
        if (gameScene instanceof GameScene2D gameScene2D) {
            final Vector2i size = gameScene2D.unscaledSize();
            final Vector2f scaledSize = size.scaled(gameScene2D.scaling());
            return "%dx%d (scaled: %.0fx%.0f)".formatted(size.x(), size.y(), scaledSize.x(), scaledSize.y());
        }
        if (game.optGameLevel().isPresent()) {
            final var worldMap = game.level().worldMap();
            return "%dx%d (map size px)".formatted(worldMap.numCols() * TS, worldMap.numRows() * TS);
        }
        return NO_INFO;
    }
}