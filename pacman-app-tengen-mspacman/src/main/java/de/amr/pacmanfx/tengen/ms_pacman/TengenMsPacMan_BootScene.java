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
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel.createRedGhost;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Renderer2D.blueShadedColor;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

/**
 * Animated "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    private static final float GHOST_Y = 21.5f * TS;

    private SpriteSheet spriteSheet;
    private long tick;
    private Ghost ghost;
    private boolean grayScreen;
    private TextActor presentsText;

    @Override
    public void doInit() {
        spriteSheet = theUI().configuration().spriteSheet();
        theGame().setScoreVisible(false);
        grayScreen = false;
        presentsText = new TextActor("TENGEN PRESENTS");
        ghost = createRedGhost();
        ghost.setSpeed(0);
        ghost.setAnimations(theUI().configuration().createGhostAnimations(ghost));
        ghost.selectAnimation(ANIM_GHOST_NORMAL);
        tick = 0;
    }

    @Override
    public void update() {
        ghost.move();
        presentsText.move();
        tick += 1;
        if (tick == 7) {
            grayScreen = true;
        }
        else if (tick == 12) {
            grayScreen = false;
        }
        else if (tick == 21) {
            presentsText.setPosition(9 * TS, sizeInPx().y()); // lower border of screen
            presentsText.setVelocity(0, -HTS);
            presentsText.show();
        }
        else if (tick == 55) {
            presentsText.setPosition(9 * TS, 13 * TS);
            presentsText.setVelocity(Vector2f.ZERO);
        }
        else if (tick == 113) {
            ghost.setPosition(sizeInPx().x() - TS, GHOST_Y);
            ghost.setMoveAndWishDir(Direction.LEFT);
            ghost.setSpeed(TS);
            ghost.show();
        }
        else if (tick == 181) {
            presentsText.setVelocity(0, TS);
        }
        else if (tick == 203) {
            grayScreen = true;
        }
        else if (tick == 214) {
            grayScreen = false;
        }
        else if (tick == 220) {
            theGameController().changeGameState(GameState.INTRO);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        var r = (TengenMsPacMan_Renderer2D) gr();
        r.drawVerticalSceneBorders();
        if (grayScreen) {
            r.fillCanvas(nesPaletteColor(0x10));
        } else {
            r.fillText(presentsText.text(), blueShadedColor(tick), arcadeFont8(), presentsText.x(), presentsText.y());
            r.drawActor(ghost, spriteSheet);
        }
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        ctx().setFill(Color.WHITE);
        ctx().setFont(Font.font(20));
        ctx().fillText("Tick " + tick, 20, 20);
    }
}