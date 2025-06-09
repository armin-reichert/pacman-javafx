package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.uilib.GameAction;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Set;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;
import static de.amr.pacmanfx.uilib.ActionBindingSupport.createBinding;
import static de.amr.pacmanfx.uilib.input.Keyboard.*;

public interface TengenMsPacMan_ActionBindings {

    Map<GameAction, Set<KeyCombination>> TENGEN_DEFAULT_ACTION_BINDINGS = Map.ofEntries(
        createBinding(ACTION_PLAYER_UP,                theJoypad().key(JoypadButton.UP),    control(KeyCode.UP)),
        createBinding(ACTION_PLAYER_DOWN,              theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        createBinding(ACTION_PLAYER_LEFT,              theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        createBinding(ACTION_PLAYER_RIGHT,             theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
        createBinding(ACTION_QUIT_DEMO_LEVEL,          theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_START_GAME,               theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_START_PLAYING,            theJoypad().key(JoypadButton.START)),
        createBinding(ACTION_TOGGLE_DISPLAY_MODE,      alt(KeyCode.C)),
        createBinding(ACTION_TOGGLE_JOYPAD_KEYS_SHOWN, nude(KeyCode.SPACE)),
        createBinding(ACTION_TOGGLE_PAC_BOOSTER,       theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B))
    );
}
