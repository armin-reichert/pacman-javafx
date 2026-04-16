/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.input.Input;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public interface TengenMsPacMan_ActionBindings {

    Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(CommonGameActions.ACTION_STEER_UP,
            Input.instance().joypad.keyForButton(JoypadButton.UP), control(KeyCode.UP)),

        new ActionBinding(CommonGameActions.ACTION_STEER_DOWN,
            Input.instance().joypad.keyForButton(JoypadButton.DOWN), control(KeyCode.DOWN)),

        new ActionBinding(CommonGameActions.ACTION_STEER_LEFT,
            Input.instance().joypad.keyForButton(JoypadButton.LEFT), control(KeyCode.LEFT)),

        new ActionBinding(CommonGameActions.ACTION_STEER_RIGHT,
            Input.instance().joypad.keyForButton(JoypadButton.RIGHT), control(KeyCode.RIGHT))
    );

    Set<ActionBinding> TENGEN_SPECIFIC_BINDINGS = Set.of(
        new ActionBinding(ACTION_QUIT_DEMO_LEVEL, Input.instance().joypad.keyForButton(JoypadButton.START)),

        new ActionBinding(ACTION_ENTER_START_SCREEN, Input.instance().joypad.keyForButton(JoypadButton.START)),

        new ActionBinding(ACTION_START_PLAYING, Input.instance().joypad.keyForButton(JoypadButton.START)),

        new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,
            Input.instance().joypad.keyForButton(JoypadButton.A),
            Input.instance().joypad.keyForButton(JoypadButton.B)),

        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),

        new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
    );
}
