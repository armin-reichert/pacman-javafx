package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.shadeOfBlue;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_TILES_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_TILES_Y;

public class BootScene extends GameScene2D {

    static final int TENGEN_PRESENTS_FINAL_Y = 12 * TS;
    static final float GHOST_Y = 21 * TS - 4;

    private Ghost ghost;
    private int tengenPresentsY;
    private boolean grayScreen;
    private long t;

    @Override
    public void init() {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        t = 0;
        context.setScoreVisible(false);
        tengenPresentsY = (NES_TILES_Y + 1) * TS; // just out of screen
        grayScreen = false;
        ghost = Ghost.blinky();
        ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
        ghost.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
    }

    @Override
    public void update() {
        if (0 <= t && t < 90) {
            if (tengenPresentsY > TENGEN_PRESENTS_FINAL_Y) {
                tengenPresentsY -= HTS;
            }
        }
        else if (t == 90) {
            ghost.setPosition(t(NES_TILES_X + 1), GHOST_Y);
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
    protected void drawSceneContent(GameRenderer renderer, Vector2f sceneSize) {
        if (grayScreen) {
            renderer.ctx().setFill(Color.grayRgb(116));
            renderer.ctx().fillRect(0, 0, renderer.canvas().getWidth(), renderer.canvas().getHeight());
        } else {
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText("TENGEN PRESENTS", shadeOfBlue(t, 16), font, 8 * TS, tengenPresentsY);
            renderer.drawAnimatedEntity(ghost);
        }
    }
}