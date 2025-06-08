package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.uilib.GameAction;
import de.amr.pacmanfx.uilib.input.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

import java.util.Map;
import java.util.Set;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;
import static de.amr.pacmanfx.uilib.ActionBindingsProvider.actionBinding;
import static de.amr.pacmanfx.uilib.input.Keyboard.alt;
import static de.amr.pacmanfx.uilib.input.Keyboard.control;

public interface TengenMsPacMan_ActionBindings {

    Map<GameAction, Set<KeyCombination>> TENGEN_DEFAULT_BINDING_MAP = Map.ofEntries(
        actionBinding(PLAYER_UP,           theJoypad().key(JoypadButton.UP),    control(KeyCode.UP)),
        actionBinding(PLAYER_DOWN,         theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        actionBinding(PLAYER_LEFT,         theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        actionBinding(PLAYER_RIGHT,        theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
        actionBinding(QUIT_DEMO_LEVEL,     theJoypad().key(JoypadButton.START)),
        actionBinding(TOGGLE_DISPLAY_MODE, alt(KeyCode.C)),
        actionBinding(TOGGLE_PAC_BOOSTER,  theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B))
    );
}
