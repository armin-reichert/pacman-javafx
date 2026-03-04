/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.util.Objects;

import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D;

/**
 * Context menu for the play scene in 2D/3D mode.
 * <p>
 * The menu provides:
 * <ul>
 *   <li>Switching between 2D and 3D play scenes</li>
 *   <li>Enabling/disabling the picture‑in‑picture mini view</li>
 *   <li>Selecting the active 3D perspective</li>
 *   <li>Toggling Pac‑Man autopilot and immunity</li>
 *   <li>Muting/unmuting sound</li>
 *   <li>Quitting the current game scene</li>
 * </ul>
 *
 * The menu keeps its perspective selection synchronized with the global
 * {@link GameUI#PROPERTY_3D_PERSPECTIVE_ID} property. When the property changes
 * externally, the corresponding radio button is automatically selected.
 *
 * <p>Instances must be disposed via {@link #dispose()} to remove listeners and
 * avoid memory leaks.</p>
 */
class PlaySceneContextMenu extends GameUI_ContextMenu implements Disposable {

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

    /**
     * Creates a new context menu for the play scene.
     *
     * @param ui the game UI providing access to game state and UI properties
     * @throws NullPointerException if {@code ui} is {@code null}
     */
    public PlaySceneContextMenu(GameUI ui) {
        super(ui);

        final Game game = ui.gameContext().currentGame();

        addLocalizedTitleItem("scene_display");
        addLocalizedActionItem(ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_2D_scene");
        addLocalizedCheckBox(GameUI.PROPERTY_MINI_VIEW_ON, "pip");

        addLocalizedTitleItem("select_perspective");
        for (PerspectiveID id : PerspectiveID.values()) {
            final RadioMenuItem item = addLocalizedRadioButton("perspective_id_" + id.name());
            item.setUserData(id);
            item.setToggleGroup(perspectivesGroup);

            if (id == GameUI.PROPERTY_3D_PERSPECTIVE_ID.get()) {
                item.setSelected(true);
            }

            item.setOnAction(_ -> GameUI.PROPERTY_3D_PERSPECTIVE_ID.set(id));
        }

        addLocalizedTitleItem("pacman");
        addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot");
        addLocalizedCheckBox(game.immuneProperty(), "immunity");

        addSeparator();
        addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        addLocalizedActionItem(ACTION_QUIT_GAME_SCENE, "quit");

        GameUI.PROPERTY_3D_PERSPECTIVE_ID.addListener(perspectiveListener);
    }

    /**
     * Removes listeners registered by this menu.
     * <p>
     * Must be called when the menu is no longer needed to prevent memory leaks.
     */
    @Override
    public void dispose() {
        GameUI.PROPERTY_3D_PERSPECTIVE_ID.removeListener(perspectiveListener);
    }
}
