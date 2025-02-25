package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.input.Keyboard;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;

public class BootMenu extends GameScene2D {

    public BootMenu() {
    }

    @Override
    protected void drawSceneContent() {

    }

    @Override
    public void bindGameActions() {
        bind(context -> {Logger.info("SPACE action");}, Keyboard.naked(KeyCode.SPACE));
        bind(context -> {Logger.info("UP action");}, Keyboard.naked(KeyCode.UP));
        bind(context -> {Logger.info("DOWN action");}, Keyboard.naked(KeyCode.DOWN));
        bind(context -> {
            Logger.info("ENTER action");
            context.gameController().terminateCurrentState();
            }, Keyboard.naked(KeyCode.ENTER));
    }

    @Override
    public void update() {
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

}
