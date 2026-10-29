/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Disposable;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsVM;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;

import java.util.Objects;

import static de.amr.pacmanfx.ui.views.MenuBuildingSupport.*;

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
        final Game3DSettingsVM settings3D = playScene3D.engine().ui().viewModel().common3DSettings();
        final GameContext game = playScene3D.engine().optCurrentGame().orElseThrow();
        final GameCheats cheats = game.session().cheats();
        final TranslationManager translator = playScene3D.engine().translationManager();

        perspectiveIDProperty = settings3D.cameraPerspectiveIDProperty();

        addLocalizedTitleItem(getItems(), translator, "context_menu.scene_display");
        addLocalizedActionItem(playScene3D.engine(), getItems(), translator,
            CommonGameActions.instance().uiSettingsActions().actionTogglePlayScene2D3D(), "context_menu.use_2D_scene");
        addLocalizedCheckBox(getItems(), translator, playScene3D.engine().ui().viewModel().miniViewSettings().activeProperty, "context_menu.pip");
        addLocalizedTitleItem(getItems(), translator, "context_menu.select_perspective");

        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem radio = addLocalizedRadioButton(getItems(), translator, "perspective_id_" + id.name());
            radio.setOnAction(_ -> perspectiveIDProperty.set(id));
            radio.setUserData(id);
            radio.setToggleGroup(perspectivesGroup);
            if (id == perspectiveIDProperty.get()) {
                radio.setSelected(true);
            }
        }

        addLocalizedTitleItem(getItems(), translator, "context_menu.pacman");
        addLocalizedCheckBox(getItems(), translator, cheats.pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(getItems(), translator, cheats.pacImmuneProperty(), "context_menu.immunity");
        addSeparator(getItems());
        addLocalizedCheckBox(getItems(), translator, playScene3D.engine().ui().viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(playScene3D.engine(), getItems(), translator, CommonGameActions.instance().gameFlowActions().actionQuit(), "context_menu.quit");

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
