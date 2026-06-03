/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Set;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public final class TengenMsPacMan_ActionBindings {

    private TengenMsPacMan_ActionBindings() {
        throw new IllegalStateException();
    }

    private static KeyCodeCombination keyFor(JoypadButton button) {
        return Input.instance().joypad().keyForButton(button);
    }

    public static final Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(CommonActions.ACTION_STEER_UP,    keyFor(JoypadButton.UP), control(KeyCode.UP)),
        new ActionBinding(CommonActions.ACTION_STEER_DOWN,  keyFor(JoypadButton.DOWN), control(KeyCode.DOWN)),
        new ActionBinding(CommonActions.ACTION_STEER_LEFT,  keyFor(JoypadButton.LEFT), control(KeyCode.LEFT)),
        new ActionBinding(CommonActions.ACTION_STEER_RIGHT, keyFor(JoypadButton.RIGHT), control(KeyCode.RIGHT))
    );

    public static final Set<ActionBinding> TENGEN_SPECIFIC_BINDINGS = Set.of(
        new ActionBinding(ACTION_QUIT_DEMO_LEVEL, keyFor(JoypadButton.START)),
        new ActionBinding(ACTION_ENTER_START_SCREEN, keyFor(JoypadButton.START)),
        new ActionBinding(ACTION_START_PLAYING, keyFor(JoypadButton.START)),
        new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER, keyFor(JoypadButton.A), keyFor(JoypadButton.B)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
        new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
    );
}
