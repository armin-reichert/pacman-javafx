package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Set;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Action.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;
import static de.amr.pacmanfx.ui.ActionBindingSupport.createBinding;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public interface TengenMsPacMan_ActionBindings {

    Map<GameAction, Set<KeyCombination>> TENGEN_ACTION_BINDINGS = Map.ofEntries(
        createBinding(ACTION_STEER_UP,                theJoypad().key(JoypadButton.UP),    control(KeyCode.UP)),
        createBinding(ACTION_STEER_DOWN,              theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        createBinding(ACTION_STEER_LEFT,              theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        createBinding(ACTION_STEER_RIGHT,             theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
        createBinding(ACTION_QUIT_DEMO_LEVEL,          theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_START_GAME,               theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_START_PLAYING,            theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_TOGGLE_DISPLAY_MODE,      alt(KeyCode.C)),
        createBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, nude(KeyCode.SPACE)),
        createBinding(ACTION_TOGGLE_PAC_BOOSTER,       theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B))
    );
}
