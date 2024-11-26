/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.NES_SIZE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.nesPaletteColor;

/**
 * Animated "TENGEN PRESENTS" text and ghost running through scene.
 */
public class BootScene extends GameScene2D {

    private static final int TENGEN_PRESENTS_FINAL_Y = 13 * TS;
    private static final int TENGEN_PRESENTS_X = 9 * TS;
    private static final float GHOST_Y = 21.5f * TS;

    private long t;
    private Ghost ghost;
    private boolean grayScreen;
    private float tengenPresentsY;
    private float tengenPresentsSpeed;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.setScoreVisible(false);
        t = -1;
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            grayScreen = false;
            tengenPresentsY = size().y() + TS;  // just out of visible area
            tengenPresentsSpeed = 0;
            ghost = Ghost.blinky();
            ghost.setSpeed(0);
            ghost.hide();
            GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
            ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(Animations.ANIM_GHOST_NORMAL);
        }
        if (t == 7) {
            grayScreen = true;
        }
        else if (t == 12) {
            grayScreen = false;
        }
        else if (t == 21) {
            tengenPresentsY = size().y();
            tengenPresentsSpeed = -HTS;
        }
        else if (t == 55) {
            tengenPresentsSpeed = 0;
            if (tengenPresentsY != TENGEN_PRESENTS_FINAL_Y) {
                Logger.error("Tengen presents text not at final position {} but at y={}", TENGEN_PRESENTS_FINAL_Y, tengenPresentsY);
            }
            tengenPresentsY = TENGEN_PRESENTS_FINAL_Y;
        }
        else if (t == 113) {
            ghost.setPosition(size().x() - TS, GHOST_Y);
            ghost.setMoveAndWishDir(Direction.LEFT);
            ghost.setSpeed(TS);
            ghost.setVisible(true);
        }
        else if (t == 181) {
            tengenPresentsSpeed = TS;
        }
        else if (t == 203) {
            grayScreen = true;
        }
        else if (t == 214) {
            grayScreen = false;
        }
        else if (t == 220) {
            context.gameController().changeState(GameState.INTRO);
        }
        ghost.move();
        tengenPresentsY += tengenPresentsSpeed;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        var r = (MsPacManGameTengenRenderer) gr;
        r.setScaling(scaling());
        if (grayScreen) {
            r.ctx().setFill(nesPaletteColor(0x10));
            r.ctx().fillRect(0, 0, r.canvas().getWidth(), r.canvas().getHeight());
        } else {
            r.drawAnimatedTengenPresentsText(t, TENGEN_PRESENTS_X, tengenPresentsY);
            r.drawAnimatedEntity(ghost);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(size().x(), size().y());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(Font.font(20));
        gr.ctx().fillText("Tick " + t, 20, 20);
    }
}