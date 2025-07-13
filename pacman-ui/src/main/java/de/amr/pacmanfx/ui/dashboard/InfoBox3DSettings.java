/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.Perspective;
import de.amr.pacmanfx.uilib.CameraControlledView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.shape.DrawMode;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_DRAW_MODE;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

/**
 * Infobox with 3D related settings.
 */
public class InfoBox3DSettings extends InfoBox {

    private static final int PIP_MIN_HEIGHT = 200;
    private static final int PIP_MAX_HEIGHT = 600;

    private CheckBox cbUsePlayScene3D;
    private ColorPicker pickerLightColor;
    private ColorPicker pickerFloorColor;
    private ChoiceBox<Perspective.ID> comboPerspectives;
    private CheckBox cbPiPOn;
    private Slider sliderPiPSceneHeight;
    private Slider sliderPiPOpacity;
    private CheckBox cbEnergizerExplodes;
    private Slider sliderWallHeight;
    private Slider sliderWallOpacity;
    private CheckBox cbPacLighted;
    private CheckBox cbAxesVisible;
    private CheckBox cbWireframeMode;

    public InfoBox3DSettings(GameUI ui) {
        super(ui);
    }

    @Override
    public void init(GameUI ui) {
        cbUsePlayScene3D     = addCheckBox("3D Play Scene");
        pickerLightColor     = addColorPicker("Light Color", ui.property3DLightColor());
        pickerFloorColor     = addColorPicker("Floor Color", ui.property3DFloorColor());
        comboPerspectives    = addChoiceBox("Perspective", Perspective.ID.values());
        addLabeledValue("Camera",        this::sceneCameraInfo);
        addLabeledValue("Viewport Size", this::sceneViewportSizeInfo);
        addLabeledValue("Scene Size",    this::sceneSizeInfo);
        cbPiPOn              = addCheckBox("Picture-In-Picture", ui.propertyMiniViewOn());
        sliderPiPSceneHeight = addSlider("- Height", PIP_MIN_HEIGHT, PIP_MAX_HEIGHT, ui.propertyPipHeight().get(), false, false);
        sliderPiPOpacity     = addSlider("- Opacity", 0, 100, ui.propertyPipOpacityPercent().get(), false, false);
        sliderWallHeight     = addSlider("Obstacle Height", 0, 16, ui.property3DWallHeight().get(), false, false);
        sliderWallOpacity    = addSlider("Wall Opacity", 0, 1, ui.property3DWallOpacity().get(), false, false);
        cbEnergizerExplodes  = addCheckBox("Energizer Explosion", ui.property3DEnergizerExplodes());
        cbPacLighted         = addCheckBox("Pac-Man Lighted", ui.property3DPacLightEnabled());
        cbAxesVisible        = addCheckBox("Show Axes", ui.property3DAxesVisible());
        cbWireframeMode      = addCheckBox("Wireframe Mode");

        setTooltip(sliderPiPSceneHeight, sliderPiPSceneHeight.valueProperty(), "%.0f px");
        setTooltip(sliderPiPOpacity, sliderPiPOpacity.valueProperty(), "%.0f %%");

        setEditor(sliderPiPSceneHeight, ui.propertyPipHeight());
        setEditor(sliderPiPOpacity, ui.propertyPipOpacityPercent());
        setEditor(sliderWallHeight, ui.property3DWallHeight());
        setEditor(sliderWallOpacity, ui.property3DWallOpacity());
        setEditor(comboPerspectives, ui.property3DPerspective());

        //TODO check these
        cbUsePlayScene3D.setOnAction(e -> ACTION_TOGGLE_PLAY_SCENE_2D_3D.executeIfEnabled(theUI()));
        cbWireframeMode.setOnAction(e -> ACTION_TOGGLE_DRAW_MODE.executeIfEnabled(theUI()));
    }

    private void updateControlsFromProperties(GameUI ui) {
        comboPerspectives.setValue(ui.property3DPerspective().get());
        sliderPiPSceneHeight.setValue(ui.propertyPipHeight().get());
        sliderPiPOpacity.setValue(ui.propertyPipOpacityPercent().get());
        sliderWallHeight.setValue(ui.property3DWallHeight().get());
        sliderWallOpacity.setValue(ui.property3DWallOpacity().get());
        cbUsePlayScene3D.setSelected(ui.property3DEnabled().get());
        cbPiPOn.setSelected(ui.propertyMiniViewOn().getValue());
        comboPerspectives.setValue(ui.property3DPerspective().get());
        cbEnergizerExplodes.setSelected(ui.property3DEnergizerExplodes().get());
        cbPacLighted.setSelected(ui.property3DPacLightEnabled().get());
        cbAxesVisible.setSelected(ui.property3DAxesVisible().get());
        cbWireframeMode.setSelected(ui.property3DDrawMode().get() == DrawMode.LINE);
    }

    @Override
    public void update() {
        super.update();
        //TODO this should not be necessary on every update, when to initialize controls?
        updateControlsFromProperties(theUI());
    }

    private String sceneViewportSizeInfo() {
        if (theUI().currentGameScene().isPresent()
            && theUI().currentGameScene().get() instanceof CameraControlledView sgs) {
            return "%.0fx%.0f".formatted(
                sgs.viewPortWidthProperty().get(),
                sgs.viewPortHeightProperty().get()
            );
        }
        return InfoText.NO_INFO;
    }

    private String sceneSizeInfo() {
        if (theUI().currentGameScene().isPresent()) {
            GameScene gameScene = theUI().currentGameScene().get();
            if (gameScene instanceof GameScene2D gameScene2D) {
                Vector2f size = gameScene2D.sizeInPx();
                double scaling = gameScene2D.scaling();
                return "%.0fx%.0f (scaled: %.0fx%.0f)".formatted(
                        size.x(), size.y(), size.x() * scaling, size.y() * scaling);
            } else {
                if (ui.theGameContext().optGameLevel().isPresent()) {
                    int width = ui.theGameContext().theGameLevel().worldMap().numCols() * TS;
                    int height = ui.theGameContext().theGameLevel().worldMap().numRows() * TS;
                    return "%dx%d (unscaled)".formatted(width, height);

                }
            }
        }
        return InfoText.NO_INFO;
    }

    private String sceneCameraInfo() {
        if (theUI().currentGameScene().isPresent()
            && theUI().currentGameScene().get() instanceof CameraControlledView scrollableGameScene) {
            var cam = scrollableGameScene.camera();
            return String.format("rot=%.0f x=%.0f y=%.0f z=%.0f",
                cam.getRotate(), cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ());
        }
        return InfoText.NO_INFO;
    }
}