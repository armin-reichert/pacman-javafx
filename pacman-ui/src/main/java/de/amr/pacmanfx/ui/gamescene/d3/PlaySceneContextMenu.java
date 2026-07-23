/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.vm.Game3DSettingsVM;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.util.Objects;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;
import static java.util.Objects.requireNonNull;

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
    private final ChangeListener<PerspectiveID> perspectiveListener = (_, _, perspectiveID) -> {
        for (Toggle toggle : perspectivesGroup.getToggles()) {
            if (Objects.equals(toggle.getUserData(), perspectiveID)) {
                perspectivesGroup.selectToggle(toggle);
                break;
            }
        }
    };

    private final GameAppContext appContext;

    public PlaySceneContextMenu(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);

        final GameUI ui = appContext.ui();
        final TranslationManager translations  = ui.translations();
        final Game3DSettingsVM settings3D = ui.viewModel().common3D;

        addLocalizedTitleItem(this, translations, "context_menu.scene_display");

        addLocalizedActionItem(this, translations,
            appContext.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(), "context_menu.use_2D_scene");

        addLocalizedCheckBox(this, translations,
            ui.viewModel().miniView.activeProperty, "context_menu.pip");

        addLocalizedTitleItem(this, translations, "context_menu.select_perspective");
        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem item = addLocalizedRadioButton(this, translations, "perspective_id_" + id.name());
            item.setUserData(id);
            item.setToggleGroup(perspectivesGroup);

            if (id == settings3D.cameraPerspectiveIdProperty.get()) {
                item.setSelected(true);
            }

            item.setOnAction(_ -> settings3D.cameraPerspectiveIdProperty.set(id));
        }

        addLocalizedTitleItem(this, translations, "context_menu.pacman");
        addLocalizedCheckBox(this, translations, appContext.currentGameContext().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(this, translations, appContext.currentGameContext().cheats().pacImmuneProperty(), "context_menu.immunity");

        addSeparator(this);
        addLocalizedCheckBox(this, translations, ui.viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(this, translations, appContext.commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        settings3D.cameraPerspectiveIdProperty.addListener(perspectiveListener);
    }

    /**
     * Removes listeners registered by this menu.
     * <p>
     * Must be called when the menu is no longer needed to prevent memory leaks.
     */
    @Override
    public void dispose() {
        appContext.ui().viewModel().common3D.cameraPerspectiveIdProperty.removeListener(perspectiveListener);
    }
}
