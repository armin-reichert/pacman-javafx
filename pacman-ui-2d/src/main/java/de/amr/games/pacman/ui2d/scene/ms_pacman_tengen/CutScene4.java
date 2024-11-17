package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.*;

public class CutScene4 extends GameScene2D {

    private int t;
    private MediaPlayer music;

    @Override
    protected void doInit() {
        t = 0;
        context.setScoreVisible(false);
        music = context.sound().makeSound("intermission.4",1.0, false);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        if (t == 0) {
            music.play();
        }
        else if (t == 11*60) {
            context.gameController().changeState(GameState.BOOT);
        }
        ++t;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE;
    }

    @Override
    public void bindGameActions() {}

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        MsPacManTengenGameRenderer r = (MsPacManTengenGameRenderer) renderer;
        renderer.drawText("CUT SCENE 4", Color.WHITE, Font.font(30), 14*TS, 2*TS);
        if (context.game().level().isPresent()) {
            // avoid exception in cut scene test mode
            r.drawLevelCounter(context, size());
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().setFont(Font.font(20));
        renderer.ctx().fillText("Tick " + t, 20, 20);
    }

}
