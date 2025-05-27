/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel.createRedGhost;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theUIConfig;

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
    public void doInit() {
        theGame().scoreManager().setScoreVisible(false);
        t = -1;
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            grayScreen = false;
            tengenPresentsY = sizeInPx().y() + TS;  // just out of visible area
            tengenPresentsSpeed = 0;
            ghost = createRedGhost();
            ghost.setSpeed(0);
            ghost.hide();
            ghost.setAnimations(new TengenMsPacMan_GhostAnimationMap(theUIConfig().current().spriteSheet(), ghost.personality()));
            ghost.selectAnimation(ANIM_GHOST_NORMAL);
        }
        if (t == 7) {
            grayScreen = true;
        }
        else if (t == 12) {
            grayScreen = false;
        }
        else if (t == 21) {
            tengenPresentsY = sizeInPx().y();
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
            ghost.setPosition(sizeInPx().x() - TS, GHOST_Y);
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
            theGameController().changeState(GameState.INTRO);
        }
        ghost.move();
        tengenPresentsY += tengenPresentsSpeed;
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        var r = (TengenMsPacMan_Renderer2D) gr();
        r.fillCanvas(backgroundColor());
        r.drawScores(theGame().scoreManager(), scoreColor(), defaultSceneFont());
        r.drawSceneBorderLines();
        if (grayScreen) {
            r.fillCanvas(nesPaletteColor(0x10));
        } else {
            r.fillText("TENGEN PRESENTS", r.shadeOfBlue(t), defaultSceneFont(), TENGEN_PRESENTS_X, tengenPresentsY);
            r.drawActor(ghost);
        }
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        gr().ctx().setFill(Color.WHITE);
        gr().ctx().setFont(Font.font(20));
        gr().ctx().fillText("Tick " + t, 20, 20);
    }
}