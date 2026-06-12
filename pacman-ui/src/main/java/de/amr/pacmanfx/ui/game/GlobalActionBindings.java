/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.ui.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.TestActions;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public final class GlobalActionBindings {

    /** Cheat key bindings (Alt + key). */
    public static final Set<ActionKeyBinding> CHEAT_ACTION_BINDINGS = Set.of(
        new ActionKeyBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionKeyBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionKeyBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionKeyBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X))
    );

    /** Steering key bindings (arrow keys, optionally with Ctrl). */
    public static final Set<ActionKeyBinding> STEERING_ACTION_BINDINGS = Set.of(
        new ActionKeyBinding(ACTION_STEER_UP,    bare(KeyCode.UP),    control(KeyCode.UP)),
        new ActionKeyBinding(ACTION_STEER_DOWN,  bare(KeyCode.DOWN),  control(KeyCode.DOWN)),
        new ActionKeyBinding(ACTION_STEER_LEFT,  bare(KeyCode.LEFT),  control(KeyCode.LEFT)),
        new ActionKeyBinding(ACTION_STEER_RIGHT, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
    );

    /** Key bindings for scene/level test utilities. */
    public static final Set<ActionKeyBinding> SCENE_TESTS_BINDINGS = Set.of(
        new ActionKeyBinding(TestActions.ACTION_CUT_SCENES_TEST,      alt(KeyCode.C)),
        new ActionKeyBinding(TestActions.ACTION_SHORT_LEVEL_TEST,     alt(KeyCode.T)),
        new ActionKeyBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST,    alt_shift(KeyCode.T))
    );

    /** Common global key bindings used across all views/scenes. */
    public static final Set<ActionKeyBinding> COMMON_BINDINGS = Set.of(
        new ActionKeyBinding(ACTION_START_GAME,                       bare(KeyCode.F3)),
        new ActionKeyBinding(ACTION_ENTER_FULLSCREEN,                 bare(KeyCode.F11)),
        new ActionKeyBinding(ACTION_OPEN_EDITOR,                      alt_shift(KeyCode.E)),
        new ActionKeyBinding(ACTION_SHOW_HELP,                        bare(KeyCode.H)),
        new ActionKeyBinding(ACTION_QUIT,                             bare(KeyCode.Q)),
        new ActionKeyBinding(ACTION_SIMULATION_SLOWER,                alt(KeyCode.MINUS)),
        new ActionKeyBinding(ACTION_SIMULATION_SLOWEST,               alt_shift(KeyCode.MINUS)),
        new ActionKeyBinding(ACTION_SIMULATION_FASTER,                alt(KeyCode.PLUS)),
        new ActionKeyBinding(ACTION_SIMULATION_FASTEST,               alt_shift(KeyCode.PLUS)),
        new ActionKeyBinding(ACTION_SIMULATION_RESET,                 alt(KeyCode.DIGIT0)),
        new ActionKeyBinding(ACTION_SIMULATION_ONE_STEP,              shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionKeyBinding(ACTION_SIMULATION_TEN_STEPS,             shift(KeyCode.SPACE)),
        new ActionKeyBinding(ACTION_TOGGLE_AUTOPILOT,                 alt(KeyCode.A)),
        new ActionKeyBinding(ACTION_TOGGLE_COLLISION_STRATEGY,        alt(KeyCode.S)),
        new ActionKeyBinding(ACTION_TOGGLE_DEBUG_INFO,                alt(KeyCode.D)),
        new ActionKeyBinding(ACTION_TOGGLE_KEYBOARD_MONITOR,          alt(KeyCode.K)),
        new ActionKeyBinding(ACTION_TOGGLE_MUTED,                     alt(KeyCode.M)),
        new ActionKeyBinding(ACTION_TOGGLE_PAUSED,                    bare(KeyCode.P), bare(KeyCode.F5)),
        new ActionKeyBinding(ACTION_TOGGLE_DASHBOARD,                 bare(KeyCode.F1), alt(KeyCode.B)),
        new ActionKeyBinding(ACTION_TOGGLE_IMMUNITY,                  alt(KeyCode.I)),
        new ActionKeyBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,      bare(KeyCode.F2)),
        new ActionKeyBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,          alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
    );

    private GlobalActionBindings() {}
}
