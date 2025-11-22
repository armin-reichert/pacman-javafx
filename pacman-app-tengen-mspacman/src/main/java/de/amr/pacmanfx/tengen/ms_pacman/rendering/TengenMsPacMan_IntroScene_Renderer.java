package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_IntroScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Properties.PROPERTY_JOYPAD_BINDINGS_DISPLAYED;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.shadeOfBlue;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_IntroScene.MARQUEE_X;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_IntroScene.MARQUEE_Y;

public class TengenMsPacMan_IntroScene_Renderer extends TengenMsPacMan_CommonSceneRenderer {

    private final ActorRenderer actorRenderer;

    public TengenMsPacMan_IntroScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = GameScene2DRenderer.configureRendererForGameScene(
            uiConfig.createActorRenderer(canvas), scene);
    }
    
    public void draw() {
        clearCanvas();

        final TengenMsPacMan_IntroScene introScene = scene();
        final long tick = introScene.sceneController.state().timer().tickCount();

        ctx.setFont(arcadeFont8());
        ctx.setImageSmoothing(false);
        switch (introScene.sceneController.state()) {
            case WAITING_FOR_START -> {
                if (!introScene.dark) {
                    boolean showPressStart = tick % 60 < 30;
                    fillText("TENGEN PRESENTS", shadeOfBlue(tick), introScene.presentsText.x(), introScene.presentsText.y());
                    drawSprite(introScene.spriteSheet.sprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 6 * TS, MARQUEE_Y, true);
                    if (showPressStart) fillText("PRESS START", nesColor(0x20), 11 * TS, MARQUEE_Y + 9 * TS);
                    fillText("MS PAC-MAN TM NAMCO LTD", nesColor(0x25), 6 * TS, MARQUEE_Y + 15 * TS);
                    fillText("©1990 TENGEN INC",        nesColor(0x25), 8 * TS, MARQUEE_Y + 16 * TS);
                    fillText("ALL RIGHTS RESERVED",     nesColor(0x25), 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }
            case SHOWING_MARQUEE -> {
                introScene.marquee.draw(ctx());
                fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
            }
            case GHOSTS_MARCHING_IN -> {
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
            case MS_PACMAN_MARCHING_IN -> {
                introScene.marquee.draw(ctx());
                fillText("\"MS PAC-MAN\"", nesColor(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                fillText("STARRING", nesColor(0x20), MARQUEE_X + 12, MARQUEE_Y + 22);
                fillText("MS PAC-MAN", nesColor(0x28), MARQUEE_X + 28, MARQUEE_Y + 38);
                introScene.ghosts.forEach(actorRenderer::drawActor);
                actorRenderer.drawActor(introScene.msPacMan);
            }
        }

        if (PROPERTY_JOYPAD_BINDINGS_DISPLAYED.get()) {
            drawJoypadKeyBinding(scene().ui().joypad().currentKeyBinding());
        }

        if (introScene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}