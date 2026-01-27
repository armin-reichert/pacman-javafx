/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_IntroScene;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_IntroScene.SceneState;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_IntroScene.MARQUEE_X;
import static de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_IntroScene.MARQUEE_Y;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_IntroScene_Renderer extends GameScene2D_Renderer
    implements SpriteRenderer, TengenMsPacMan_SceneRenderingCommons {

    private final ActorRenderer actorRenderer;

    public TengenMsPacMan_IntroScene_Renderer(GameUI_Config uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        requireNonNull(uiConfig);
        requireNonNull(scene);
        actorRenderer = scene.adaptRenderer(uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_IntroScene introScene = (TengenMsPacMan_IntroScene) scene;
        final long tick = introScene.sceneController.state().timer().tickCount();

        ctx.setFont(arcadeFont8());
        ctx.setImageSmoothing(false);
        switch (introScene.sceneController.state()) {
            case SceneState.WAITING_FOR_START -> {
                if (!introScene.dark) {
                    boolean showPressStart = tick % 60 < 30;
                    fillText("TENGEN PRESENTS", shadeOfBlue(tick), introScene.presentsText.x(), introScene.presentsText.y());
                    drawSprite(spriteSheet().sprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 6 * TS, MARQUEE_Y, true);
                    if (showPressStart) fillText("PRESS START", nesColor(0x20), 11 * TS, MARQUEE_Y + 9 * TS);
                    fillText("MS PAC-MAN TM NAMCO LTD", nesColor(0x25), 6 * TS, MARQUEE_Y + 15 * TS);
                    fillText("©1990 TENGEN INC",        nesColor(0x25), 8 * TS, MARQUEE_Y + 16 * TS);
                    fillText("ALL RIGHTS RESERVED",     nesColor(0x25), 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SceneState.SHOWING_MARQUEE -> {
                introScene.marquee.draw(ctx());
                fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case SceneState.GHOSTS_MARCHING_IN -> {
                introScene.marquee.draw(ctx());
                fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                if (introScene.ghostIndex == 0) {
                    fillText("WITH", nesColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                Ghost currentGhost = introScene.ghosts.get(introScene.ghostIndex);
                Color ghostColor = introScene.ghostColors[currentGhost.personality()];
                fillText(currentGhost.name().toUpperCase(), ghostColor, MARQUEE_X + 44, MARQUEE_Y + 41);
                introScene.ghosts.forEach(actorRenderer::drawActor);
            }
            case SceneState.MS_PACMAN_MARCHING_IN -> {
                introScene.marquee.draw(ctx());
                fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                fillText("STARRING", nesColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 22);
                fillText("MS PAC-MAN", nesColor(0x28), MARQUEE_X + 28, MARQUEE_Y + 38);
                introScene.ghosts.forEach(actorRenderer::drawActor);
                actorRenderer.drawActor(introScene.msPacMan);
            }
            default -> {}
        }

        if (PROPERTY_JOYPAD_BINDINGS_DISPLAYED.get()) {
            drawJoypadKeyBinding(JOYPAD.currentKeyBinding());
        }

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}