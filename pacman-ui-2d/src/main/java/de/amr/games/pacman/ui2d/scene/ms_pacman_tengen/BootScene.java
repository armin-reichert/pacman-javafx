package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.GameRenderer.shadeOfBlue;

public class BootScene extends GameScene2D {

    private Ghost ghost;
    private int tengenPresentsY;
    private boolean grayScreen;
    private long t;

    @Override
    public void init() {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        t = 0;
        context.setScoreVisible(false);
        tengenPresentsY = 36 * TS;
        grayScreen = false;
        ghost = Ghost.blinky();
        ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
        ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
    }

    @Override
    public void update() {
        if (0 <= t && t < 90) {
            if (t % 4 == 0 && tengenPresentsY > 17 * TS) {
                tengenPresentsY -= 2*TS;
            }
        }
        else if (t == 90) {
            ghost.setPosition(t(31), t(25));
            ghost.setMoveAndWishDir(Direction.LEFT);
            ghost.setSpeed(8); // TODO check speed
            ghost.setVisible(true);
        }
        else if (90 < t && t < 150) {
            ghost.move();
        }
        else if (150 <= t && t < 180) {
            if (t % 2 == 0) { tengenPresentsY += TS; }
        }
        else if (t == 180) {
            grayScreen = true;
        }
        else if (t == 185) {
            context.gameController().changeState(GameState.INTRO);
        }
        t += 1;
    }

    @Override
    public void end() {
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        if (grayScreen) {
            renderer.ctx().setFill(Color.grayRgb(116));
            renderer.ctx().fillRect(0, 0, renderer.canvas().getWidth(), renderer.canvas().getHeight());
        } else {
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t, 16), font, 6 * TS, tengenPresentsY);
            renderer.drawAnimatedEntity(ghost);
        }
    }
}
