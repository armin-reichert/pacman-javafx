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
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createGhost;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";
    public static final Color GRAY = nesColor(0x10);

    private static final float GHOST_Y = TS(21.5);

    private int tick;
    private boolean grayScreen;
    private Actor movingText;
    private Ghost ghost;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_BootScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        actorRenderer = (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas);
        bindRendererProperties(actorRenderer);

        movingText = new Actor();
        movingText.setPosition(TS(9), sizeInPx().y()); // lower border of screen
        movingText.setVelocity(Vector2f.ZERO);

        ghost = createGhost(RED_GHOST_SHADOW);
        ghost.setSpeed(0);
        ghost.setAnimations(uiConfig.createGhostAnimations(ghost));
        ghost.selectAnimation(ANIM_GHOST_NORMAL);
    }

    private void grayOn()  { grayScreen = true; }
    private void grayOff() { grayScreen = false; }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        tick = (int) context().gameState().timer().tickCount();

        ghost.move();
        movingText.move();

        switch (tick) {
            case   1 -> grayOff();
            case   7 -> grayOn();
            case  12 -> grayOff();
            case  21 -> {
                movingText.setVelocity(0, -HTS);
                movingText.show();
            }
            case  55 -> {
                movingText.setPosition(TS(9), TS(13));
                movingText.setVelocity(Vector2f.ZERO);
            }
            case 113 -> {
                ghost.setPosition(sizeInPx().x() - TS, GHOST_Y);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setSpeed(TS);
                ghost.show();
            }
            case 181 -> movingText.setVelocity(0, TS);
            case 203 -> {
                grayOn();
                movingText.hide();
                ghost.hide();
            }
            case 214 -> grayOff();
            case 220 -> context().gameController().changeGameState(GamePlayState.INTRO);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE_PX;
    }

    @Override
    public void drawHUD() {
        // No HUD
    }

    @Override
    public void drawSceneContent() {
        if (grayScreen) {
            actorRenderer.fillCanvas(GRAY);
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, blueShadedColor(tick), actorRenderer.arcadeFontTS(), movingText.x(), movingText.y());
            actorRenderer.drawActor(ghost);
        }
    }
}