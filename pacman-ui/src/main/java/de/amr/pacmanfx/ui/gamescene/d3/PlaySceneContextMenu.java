/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.config.UISettings3D;
import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.game.Game;
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

    private final Game game;

    public PlaySceneContextMenu(Game game) {
        this.game = requireNonNull(game);

        final GameModel gameModel = game.currentGameContext().model();
        final TranslationManager translator = game.ui().translations();
        final UISettings3D settings3D = game.ui().settings().d3();

        addLocalizedTitleItem(this, translator, "scene_display");
        addLocalizedActionItem(this, translator, game.actions().uiSettingsActions().actionTogglePlayScene2D3D(), "use_2D_scene");
        addLocalizedCheckBox(this, translator, game.ui().settings().miniView().activeProperty(), "pip");

        addLocalizedTitleItem(this, translator, "select_perspective");
        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem item = addLocalizedRadioButton(this, translator, "perspective_id_" + id.name());
            item.setUserData(id);
            item.setToggleGroup(perspectivesGroup);

            if (id == settings3D.cameraPerspectiveIdProperty().get()) {
                item.setSelected(true);
            }

            item.setOnAction(_ -> settings3D.cameraPerspectiveIdProperty().set(id));
        }

        addLocalizedTitleItem(this, translator, "pacman");
        addLocalizedCheckBox(this, translator, gameModel.cheats().pacUsingAutopilotProperty(), "autopilot");
        addLocalizedCheckBox(this, translator, gameModel.cheats().pacImmuneProperty(), "immunity");

        addSeparator(this);
        addLocalizedCheckBox(this, translator, game.ui().settings().mutedProperty(), "muted");
        addLocalizedActionItem(this, translator, game.actions().gameFlowActions().actionQuit(), "quit");

        settings3D.cameraPerspectiveIdProperty().addListener(perspectiveListener);
    }

    /**
     * Removes listeners registered by this menu.
     * <p>
     * Must be called when the menu is no longer needed to prevent memory leaks.
     */
    @Override
    public void dispose() {
        game.ui().settings().d3().cameraPerspectiveIdProperty().removeListener(perspectiveListener);
    }
}
