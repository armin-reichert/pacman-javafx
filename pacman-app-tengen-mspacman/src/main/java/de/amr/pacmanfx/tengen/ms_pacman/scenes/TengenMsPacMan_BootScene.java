/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createGhost;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer.blueShadedColor;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private static final float GHOST_Y = 21.5f * TS;

    private long tick;
    private boolean grayScreen;
    private Actor movingText;
    private Ghost ghost;

    public TengenMsPacMan_BootScene(GameUI ui) {
        super(ui);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void doInit() {
        tick = 0;
        grayScreen = false;

        movingText = new Actor();
        ghost = createGhost(RED_GHOST_SHADOW);
        ghost.setSpeed(0);
        ghost.setAnimations(ui.currentConfig().createGhostAnimations(ghost));
        ghost.animations().ifPresent(am -> am.select(ANIM_GHOST_NORMAL));

        gameContext().game().hudData().all(false);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
        ghost.move();
        movingText.move();
        tick += 1;
        if (tick == 7) {
            grayScreen = true;
        }
        else if (tick == 12) {
            grayScreen = false;
        }
        else if (tick == 21) {
            movingText.setPosition(9 * TS, sizeInPx().y()); // lower border of screen
            movingText.setVelocity(0, -HTS);
            movingText.show();
        }
        else if (tick == 55) {
            movingText.setPosition(9 * TS, 13 * TS);
            movingText.setVelocity(Vector2f.ZERO);
        }
        else if (tick == 113) {
            ghost.setPosition(sizeInPx().x() - TS, GHOST_Y);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(TS);
            ghost.show();
        }
        else if (tick == 181) {
            movingText.setVelocity(0, TS);
        }
        else if (tick == 203) {
            grayScreen = true;
        }
        else if (tick == 214) {
            grayScreen = false;
        }
        else if (tick == 220) {
            gameContext().gameController().changeGameState(GamePlayState.INTRO);
        }
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void drawSceneContent() {
        renderer().drawVerticalSceneBorders();
        if (grayScreen) {
            GameRenderer.fillCanvas(canvas, nesPaletteColor(0x10));
        } else {
            renderer().fillTextAtScaledPosition(TENGEN_PRESENTS, blueShadedColor(tick), scaledArcadeFont8(),
                movingText.x(), movingText.y());
            renderer().drawActor(ghost);
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