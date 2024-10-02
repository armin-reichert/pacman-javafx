package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.shape.DrawMode;
import org.tinylog.Logger;

import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_PIP_ON;
import static de.amr.games.pacman.ui2d.util.KeyInput.alt;
import static de.amr.games.pacman.ui2d.util.KeyInput.key;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static de.amr.games.pacman.ui3d.PacManGames3dApp.*;

public enum GameAction3D implements GameAction {

    NEXT_PERSPECTIVE(alt(KeyCode.RIGHT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            Perspective.Name next = PY_3D_PERSPECTIVE.get().next();
            PY_3D_PERSPECTIVE.set(next);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(next.name())));
        }
    },

    PREV_PERSPECTIVE(alt(KeyCode.LEFT)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            Perspective.Name prev = PY_3D_PERSPECTIVE.get().prev();
            PY_3D_PERSPECTIVE.set(prev);
            context.showFlashMessage(context.locText("camera_perspective", context.locText(prev.name())));
        }
    },

    TOGGLE_DRAW_MODE(alt(KeyCode.W)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
        }
    },

    TOGGLE_PLAY_SCENE_2D_3D(alt(KeyCode.DIGIT3)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            context.currentGameScene().ifPresent(gameScene -> {
                toggle(PY_3D_ENABLED);
                if (context.currentGameSceneIs(GameSceneID.PLAY_SCENE) || context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D)) {
                    context.updateGameScene(true);
                    context.gameSceneProperty().get().onSceneVariantSwitch(gameScene);
                }
                context.gameController().update();
                if (!context.game().isPlaying()) {
                    context.showFlashMessage(context.locText(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
                }
            });
        }
    },

    TOGGLE_PIP_VISIBILITY(key(KeyCode.F2)) {
        @Override
        public void execute(GameContext context) {
            super.execute(context);
            toggle(PY_PIP_ON);
            if (!context.currentGameSceneIs(GameSceneID.PLAY_SCENE_3D)) {
                context.showFlashMessage(context.locText(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
            }
        }
    };

    GameAction3D(KeyCodeCombination... combinations) {
        trigger = KeyInput.register(combinations);
    }

    @Override
    public KeyInput trigger() {
        return trigger;
    }

    @Override
    public boolean called() {
        return Keyboard.pressed(trigger);
    }

    @Override
    public void execute(GameContext context) {
        Logger.info("Execute game action {}", name());
    }

    private final KeyInput trigger;
}