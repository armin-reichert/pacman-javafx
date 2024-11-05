package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_RESOLUTION_Y;

public class CutScene4 extends GameScene2D {

    private MediaPlayer music;

    @Override
    protected void doInit() {
        context.setScoreVisible(false);
        music = context.sound().makeSound("intermission.4",1.0, false);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(1)) {
            music.play();
        }
        else if (context.gameState().timer().atSecond(11)) {
            context.gameController().changeState(GameState.BOOT);
        }
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    public void bindGameActions() {}

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        renderer.drawText("CUT SCENE 4", Color.WHITE, Font.font(30), 14*TS, 2*TS);
    }
}
