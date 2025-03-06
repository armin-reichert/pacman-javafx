/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.ActorAnimations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_GhostAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfig3D.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfig3D.nesPaletteColor;

/**
 * Animated "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

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
            GameSpriteSheet spriteSheet = context.gameConfiguration().spriteSheet();
            ghost.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, ghost.id()));
            ghost.selectAnimation(ActorAnimations.ANIM_GHOST_NORMAL);
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
        var r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        r.setScaling(scaling());
        if (grayScreen) {
            r.fillCanvas(nesPaletteColor(0x10));
        } else {
            r.drawText("TENGEN PRESENTS", r.shadeOfBlue(t), r.scaledArcadeFont(TS), TENGEN_PRESENTS_X, tengenPresentsY);
            r.drawAnimatedActor(ghost);
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