/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Disposable;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.action.core.PacManGameEngineContext;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsVM;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.util.Objects;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * Context menu for the play scene in 2D/3D mode.
 */
public class PlaySceneContextMenu extends ContextMenu implements Disposable {

    /**
     * Toggle group containing all perspective radio buttons.
     * Ensures that only one perspective can be selected at a time.
     */
    private final ToggleGroup perspectivesGroup = new ToggleGroup();

    /**
     * Listener that updates the selected radio button whenever the global
     * 3D perspective property changes.
     */
    private final ChangeListener<PerspectiveID> perspectiveChangeHandler = (_, _, perspectiveID) -> {
        for (Toggle toggle : perspectivesGroup.getToggles()) {
            if (Objects.equals(toggle.getUserData(), perspectiveID)) {
                perspectivesGroup.selectToggle(toggle);
                break;
            }
        }
    };

    private final ObjectProperty<PerspectiveID> perspectiveIDProperty;

    public PlaySceneContextMenu(PlayScene3D playScene3D) {
        final PacManGameEngineContext app = playScene3D.app();
        final Game3DSettingsVM settings3D = app.ui().viewModel().common3DSettings();
        final GameCheats cheats = playScene3D.game().session().cheats();
        final TranslationManager translator = app.translationManager();

        perspectiveIDProperty = settings3D.cameraPerspectiveIDProperty();

        addLocalizedTitleItem(this, translator, "context_menu.scene_display");
        addLocalizedActionItem(app, this, translator, app.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(), "context_menu.use_2D_scene");
        addLocalizedCheckBox(this, translator, app.ui().viewModel().miniViewSettings().activeProperty, "context_menu.pip");
        addLocalizedTitleItem(this, translator, "context_menu.select_perspective");

        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem radio = addLocalizedRadioButton(this, translator, "perspective_id_" + id.name());
            radio.setOnAction(_ -> perspectiveIDProperty.set(id));
            radio.setUserData(id);
            radio.setToggleGroup(perspectivesGroup);
            if (id == perspectiveIDProperty.get()) {
                radio.setSelected(true);
            }
        }

        addLocalizedTitleItem(this, translator, "context_menu.pacman");
        addLocalizedCheckBox(this, translator, cheats.pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(this, translator, cheats.pacImmuneProperty(), "context_menu.immunity");
        addSeparator(this);
        addLocalizedCheckBox(this, translator, app.ui().viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(app, this, translator, app.commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        perspectiveIDProperty.addListener(perspectiveChangeHandler);
    }

    /**
     * Removes listeners registered by this menu.
     * <p>
     * Must be called when the menu is no longer needed to prevent memory leaks.
     */
    @Override
    public void dispose() {
        perspectiveIDProperty.removeListener(perspectiveChangeHandler);
    }
}
