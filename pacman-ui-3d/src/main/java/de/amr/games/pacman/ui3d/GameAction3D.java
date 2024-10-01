package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui2d.util.KeyInput.alt;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_PERSPECTIVE;

public enum GameAction3D {

    NEXT_PERSPECTIVE(alt(KeyCode.RIGHT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            var next = Perspective.next(PacManGames3dApp.PY_3D_PERSPECTIVE.get());
            PacManGames3dApp.PY_3D_PERSPECTIVE.set(next);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(next.name())));
        }
    },

    PREV_PERSPECTIVE(alt(KeyCode.LEFT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            var prev = Perspective.previous(PY_3D_PERSPECTIVE.get());
            PY_3D_PERSPECTIVE.set(prev);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(prev.name())));
        }
    };

    GameAction3D(KeyCodeCombination... combinations) {
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
        Logger.info("Execute game action {}", name());
    }

    private final KeyInput trigger;
}