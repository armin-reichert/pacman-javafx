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
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.*;

/**
 * Animated "TENGEN PRESENTS" text and ghost running through scene.
 */
public class BootScene extends GameScene2D {

    static final int TENGEN_PRESENTS_FINAL_Y = 13 * TS;
    static final int TENGEN_PRESENTS_X = 9 * TS;
    static final float GHOST_Y = 21.5f * TS;

    private Ghost ghost;
    private int tengenPresentsY;
    private boolean grayScreen;
    private long t;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        t = 0;
        context.enableJoypad();
        context.setScoreVisible(false);
        tengenPresentsY = (NES_TILES_Y + 1) * TS; // just out of visible area
        grayScreen = false;

        ghost = Ghost.blinky();
        ghost.setPosition((NES_TILES_X + 1) * TS, GHOST_Y);
        ghost.setMoveAndWishDir(Direction.LEFT);
        ghost.setSpeed(8); // TODO check speed
        ghost.setVisible(true);
        GameSpriteSheet spriteSheet = context.currentGameSceneConfig().spriteSheet();
        ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
        ghost.selectAnimation(Animations.ANIM_GHOST_NORMAL);
    }

    @Override
    public void update() {
        if (t < 90) {
            if (tengenPresentsY > TENGEN_PRESENTS_FINAL_Y) {
                // move up, stop at final y
                tengenPresentsY = Math.max(tengenPresentsY - TS, TENGEN_PRESENTS_FINAL_Y);
            }
        }
        else if (t < 150) {
            ghost.move();
        }
        else if (t < 180) {
            // move down again
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
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        var r = (MsPacManGameTengenRenderer) renderer;
        r.setScaling(scaling());
        if (grayScreen) {
            r.ctx().setFill(nesPaletteColor(0x10));
            r.ctx().fillRect(0, 0, r.canvas().getWidth(), r.canvas().getHeight());
        } else {
            r.drawTengenPresents(t, TENGEN_PRESENTS_X, tengenPresentsY);
            r.drawAnimatedEntity(ghost);
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