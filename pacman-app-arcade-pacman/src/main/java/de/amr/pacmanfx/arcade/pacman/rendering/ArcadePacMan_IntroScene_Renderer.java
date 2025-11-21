/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_IntroScene.*;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

public class ArcadePacMan_IntroScene_Renderer extends GameScene2DRenderer {

    private final ArcadePacMan_ActorRenderer actorRenderer;

    public ArcadePacMan_IntroScene_Renderer(GameScene2D scene, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(scene, canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = configureRendererForGameScene((ArcadePacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        setImageSmoothing(true);
    }

    public void draw() {
        ArcadePacMan_IntroScene introScene = (ArcadePacMan_IntroScene) scene();

        drawGallery(introScene);
        switch (introScene.sceneController.state()) {
            case STARTING, PRESENTING_GHOSTS -> {}
            case SHOWING_PELLET_POINTS -> drawPoints(introScene);
            case CHASING_PAC -> {
                drawPoints(introScene);
                drawBlinkingEnergizer(introScene, TS(LEFT_TILE_X), TS(20));
                drawGuys(introScene, true);
                fillText(MIDWAY_MFG_CO, ARCADE_PINK, arcadeFont8(), TS(4), TS(32));
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints(introScene);
                drawGuys(introScene, false);
                fillText(MIDWAY_MFG_CO, ARCADE_PINK, arcadeFont8(), TS(4), TS(32));
            }
        }
    }

    private void drawGallery(ArcadePacMan_IntroScene introScene) {
        ArcadePacMan_SpriteSheet ss = (ArcadePacMan_SpriteSheet) spriteSheet();
        ctx.setFont(arcadeFont8());
        if (introScene.titleVisible) {
            fillText("CHARACTER / NICKNAME", ARCADE_WHITE, TS(LEFT_TILE_X + 3), TS(6));
        }
        for (byte p = RED_GHOST_SHADOW; p <= ORANGE_GHOST_POKEY; ++p) {
            if (introScene.ghostImageVisible[p]) {
                RectShort sprite = ss.spriteSequence(GALLERY_GHOSTS)[p];
                drawSpriteCentered(TS(LEFT_TILE_X + 1), TS(7.5f + 3 * p), sprite);
            }
            if (introScene.ghostCharacterVisible[p]) {
                fillText("-" + GHOST_CHARACTERS[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 3), TS(8 + 3 * p));
            }
            if (introScene.ghostNicknameVisible[p]) {
                fillText(GHOST_NICKNAMES[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 14), TS(8 + 3 * p));
            }
        }
    }

    // TODO make shaking effect look exactly as in original game, find out what's exactly is going on here
    private void drawGuys(ArcadePacMan_IntroScene introScene, boolean shaking) {
        long tick = introScene.sceneController.state().timer().tickCount();
        int shakingAmount = shaking ? (tick % 5 < 2 ? 0 : -1) : 0;
        if (shakingAmount == 0) {
            introScene.ghosts.forEach(actorRenderer::drawActor);
        } else {
            actorRenderer.drawActor(introScene.ghosts.get(RED_GHOST_SHADOW));
            actorRenderer.drawActor(introScene.ghosts.get(ORANGE_GHOST_POKEY));
            ctx.save();
            ctx.translate(shakingAmount, 0);
            actorRenderer.drawActor(introScene.ghosts.get(PINK_GHOST_SPEEDY));
            actorRenderer.drawActor(introScene.ghosts.get(CYAN_GHOST_BASHFUL));
            ctx.restore();
        }
        actorRenderer.drawActor(introScene.pacMan);
    }

    private void drawPoints(ArcadePacMan_IntroScene introScene) {
        ctx.setFill(ARCADE_ROSE);
        // normal pellet
        ctx.fillRect(scaled(TS(LEFT_TILE_X + 6) + 4), scaled(TS(24) + 4), scaled(2), scaled(2));
        fillText("10",  ARCADE_WHITE, arcadeFont8(), TS(LEFT_TILE_X + 8), TS(25));
        fillText("PTS", ARCADE_WHITE, arcadeFont6(), TS(LEFT_TILE_X + 11), TS(25));
        // energizer
        drawBlinkingEnergizer(introScene, TS(LEFT_TILE_X + 6), TS(26));
        fillText("50",  ARCADE_WHITE, arcadeFont8(), TS(LEFT_TILE_X + 8), TS(27));
        fillText("PTS", ARCADE_WHITE, arcadeFont6(), TS(LEFT_TILE_X + 11), TS(27));
    }

    private void drawBlinkingEnergizer(ArcadePacMan_IntroScene introScene, double x, double y) {
        if (introScene.blinking.state() == Pulse.State.ON) {
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(ARCADE_ROSE);
            // draw pixelated "circle"
            ctx.fillRect(x + 2, y, 4, 8);
            ctx.fillRect(x, y + 2, 8, 4);
            ctx.fillRect(x + 1, y + 1, 6, 6);
            ctx.restore();
        }
    }

}
