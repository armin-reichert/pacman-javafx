/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.input.Keyboard.control;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    protected void setActionBindings(GameLevel level) {
        actionBindings.removeAllBindings(GameUI.KEYBOARD);
        actionBindings.registerAllBindingsFrom(GameUI.PLAY_3D_BINDINGS);
        if (level.isDemoLevel()) {
            actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
        } else {
            actionBindings.registerAllBindingsFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllBindingsFrom(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.triggerActionByKeyCombination(actionDroneClimb, control(KeyCode.MINUS));
        actionBindings.triggerActionByKeyCombination(actionDroneDescent, control(KeyCode.PLUS));
        actionBindings.activateBindings(GameUI.KEYBOARD);
    }
}