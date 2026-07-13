/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.model.Common3DSettingsModel;
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

    private final PacManGamesCollection game;

    public PlaySceneContextMenu(PacManGamesCollection game) {
        this.game = requireNonNull(game);

        final TranslationManager translations = game.ui().translations();
        final Common3DSettingsModel settings3D = game.ui().viewModel().common3D;

        addLocalizedTitleItem(this, translations, "context_menu.scene_display");

        addLocalizedActionItem(this, translations,
            game.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(), "context_menu.use_2D_scene");

        addLocalizedCheckBox(this, translations,
            game.ui().viewModel().miniView.activeProperty, "context_menu.pip");

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
        addLocalizedCheckBox(this, translations, game.currentGameContext().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(this, translations, game.currentGameContext().cheats().pacImmuneProperty(), "context_menu.immunity");

        addSeparator(this);
        addLocalizedCheckBox(this, translations, game.ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(this, translations, game.commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        settings3D.cameraPerspectiveIdProperty.addListener(perspectiveListener);
    }

    /**
     * Removes listeners registered by this menu.
     * <p>
     * Must be called when the menu is no longer needed to prevent memory leaks.
     */
    @Override
    public void dispose() {
        game.ui().viewModel().common3D.cameraPerspectiveIdProperty.removeListener(perspectiveListener);
    }
}
