/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.util.KeyInput.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public enum GameAction {
    ADD_CREDIT          (key(KeyCode.DIGIT5), key(KeyCode.NUMPAD5), key(KeyCode.UP)),
    AUTOPILOT           (alt(KeyCode.A)),
    BOOT                (key(KeyCode.F3)),
    CHEAT_ADD_LIVES     (alt(KeyCode.L)) {
        public void execute(GameContext context) {
            context.game().addLives(3);
            context.showFlashMessage(context.locText("cheat_add_lives", context.game().lives()));
        }
    },
    CHEAT_EAT_ALL       (alt(KeyCode.E)) {
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                GameWorld world = context.game().world();
                world.map().food().tiles().filter(not(world::isEnergizerPosition)).forEach(world::eatFoodAt);
                context.game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
            }
        }
    },
    CHEAT_KILL_GHOSTS   (alt(KeyCode.X)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.game().victims().clear();
                context.game().ghosts(FRIGHTENED, HUNTING_PAC).forEach(context.game()::killGhost);
                context.gameController().changeState(GameState.GHOST_DYING);
            }
        }
    },
    CHEAT_NEXT_LEVEL    (alt(KeyCode.N)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            if (context.game().isPlaying() && context.gameState() == GameState.HUNTING) {
                context.gameController().changeState(GameState.LEVEL_COMPLETE);
            }
        }
    },
    CUTSCENES           (alt(KeyCode.C)),
    DEBUG_INFO          (alt(KeyCode.D)),
    ENTER_GAME_PAGE     (key(KeyCode.SPACE), key(KeyCode.ENTER)),
    FULLSCREEN          (key(KeyCode.F11)),
    HELP                (key(KeyCode.H)),
    IMMUNITY            (alt(KeyCode.I)),
    MUTE                (alt(KeyCode.M)),
    NEXT_FLYER_PAGE     (key(KeyCode.DOWN)),
    NEXT_PERSPECTIVE    (alt(KeyCode.RIGHT)),
    NEXT_VARIANT        (key(KeyCode.V), key(KeyCode.RIGHT)),
    PAUSE               (key(KeyCode.P)),
    OPEN_EDITOR         (shift_alt(KeyCode.E)),
    PREV_FLYER_PAGE     (key(KeyCode.UP)),
    PREV_PERSPECTIVE    (alt(KeyCode.LEFT)),
    PREV_VARIANT        (key(KeyCode.LEFT)),
    QUIT                (key(KeyCode.Q)),
    SIMULATION_FASTER   (alt(KeyCode.PLUS)),
    SIMULATION_NORMAL   (alt(KeyCode.DIGIT0)),
    SIMULATION_SLOWER   (alt(KeyCode.MINUS)),
    SIMULATION_1_STEP   (key(KeyCode.SPACE), shift(KeyCode.P)),
    SIMULATION_10_STEPS (shift(KeyCode.SPACE)),
    START_GAME          (key(KeyCode.DIGIT1), key(KeyCode.NUMPAD1), key(KeyCode.ENTER), key(KeyCode.SPACE)),
    START_TEST_MODE     (alt(KeyCode.T)),
    TOGGLE_DASHBOARD    (key(KeyCode.F1), alt(KeyCode.B)),
    TOGGLE_PIP_VIEW     (key(KeyCode.F2)),
    TWO_D_THREE_D       (alt(KeyCode.DIGIT3));

    GameAction(KeyCodeCombination... combinations) {
        trigger = KeyInput.register(combinations);
    }

    public KeyInput trigger() {
        return trigger;
    }

    /**
     * @return {@code true} if any key combination defined for this game key is pressed
     */
    public boolean called() {
        return Keyboard.pressed(trigger);
    }

    public void execute(GameContext context) {
        Logger.info("Action {} executed", name());
    }

    private final KeyInput trigger;
}