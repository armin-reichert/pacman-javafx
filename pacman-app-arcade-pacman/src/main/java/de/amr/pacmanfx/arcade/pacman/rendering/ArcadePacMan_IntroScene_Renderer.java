/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_IntroScene_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    private static final String MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";
    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    private static final Color[]  GHOST_COLORS     = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private static final int LEFT_TILE_X = 4;

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final ArcadePacMan_Actor_Renderer actorRenderer;
    private final RectShort energizerSprite;

    public ArcadePacMan_IntroScene_Renderer(ArcadePacMan_IntroScene scene, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene((ArcadePacMan_Actor_Renderer) uiConfig.createActorRenderer(canvas), scene);

        debugInfoRenderer = configureRendererForGameScene(new BaseDebugInfoRenderer(scene.ui(), canvas) {
            @Override
            public void draw(GameScene2D scene) {
                ArcadePacMan_IntroScene introScene = (ArcadePacMan_IntroScene) scene;
                super.draw(scene);
                ctx.fillText("Scene timer %d".formatted(introScene.state().timer().tickCount()), 0, scaled(5 * TS));
            }
        }, scene);

        energizerSprite = spriteSheet.sprite(SpriteID.ENERGIZER);
        setImageSmoothing(true);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(GameScene2D scene) {
        final ArcadePacMan_IntroScene introScene = (ArcadePacMan_IntroScene) scene;

        clearCanvas();
        switch (introScene.state()) {
            case STARTING -> drawTitle(introScene);
            case PRESENTING_GHOSTS -> {
                drawTitle(introScene);
                drawGhostGallery(introScene);
            }
            case SHOWING_PELLET_POINTS -> {
                drawTitle(introScene);
                drawGhostGallery(introScene);
                drawPoints(introScene);
            }
            case CHASING_PAC -> {
                drawTitle(introScene);
                drawGhostGallery(introScene);
                drawPoints(introScene);
                drawBlinkingEnergizer(introScene, TS(LEFT_TILE_X), TS(20));
                actorRenderer.drawActor(introScene.pacMan());
                drawGhostsShaking(introScene);
                fillText(MIDWAY_MFG_CO, ARCADE_PINK, arcadeFont8(), TS(4), TS(32));
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawTitle(introScene);
                drawGhostGallery(introScene);
                drawPoints(introScene);
                actorRenderer.drawActor(introScene.pacMan());
                drawGhosts(introScene);
                fillText(MIDWAY_MFG_CO, ARCADE_PINK, arcadeFont8(), TS(4), TS(32));
            }
        }
        if (introScene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }

    private void drawTitle(ArcadePacMan_IntroScene introScene) {
        ctx.setFont(arcadeFont8());
        if (introScene.titleVisible()) {
            fillText("CHARACTER / NICKNAME", ARCADE_WHITE, TS(LEFT_TILE_X + 3), TS(6));
        }
    }

    private void drawGhostGallery(ArcadePacMan_IntroScene introScene) {
        ctx.setFont(arcadeFont8());
        for (byte p = RED_GHOST_SHADOW; p <= ORANGE_GHOST_POKEY; ++p) {
            if (introScene.ghostImageVisible(p)) {
                RectShort sprite = spriteSheet.spriteSequence(GALLERY_GHOSTS)[p];
                drawSpriteCentered(TS(LEFT_TILE_X + 1), TS(7.5f + 3 * p), sprite);
            }
            if (introScene.ghostCharacterVisible(p)) {
                fillText("-" + GHOST_CHARACTERS[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 3), TS(8 + 3 * p));
            }
            if (introScene.ghostNicknameVisible(p)) {
                fillText(GHOST_NICKNAMES[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 14), TS(8 + 3 * p));
            }
        }
    }

    private void drawGhostsShaking(ArcadePacMan_IntroScene introScene) {
        final long tick = introScene.state().timer().tickCount();
        final int offsetX = tick % 5 < 2 ? 0 : -1; // I think this is what happens
        if (offsetX == 0) {
            drawGhosts(introScene);
        } else {
            final List<Ghost> ghosts = introScene.ghosts();
            actorRenderer.drawActor(ghosts.get(RED_GHOST_SHADOW));
            actorRenderer.drawActor(ghosts.get(ORANGE_GHOST_POKEY));
            ctx.save();
            ctx.translate(offsetX, 0);
            actorRenderer.drawActor(ghosts.get(PINK_GHOST_SPEEDY));
            actorRenderer.drawActor(ghosts.get(CYAN_GHOST_BASHFUL));
            ctx.restore();
        }
    }

    private void drawGhosts(ArcadePacMan_IntroScene introScene) {
        introScene.ghosts().forEach(actorRenderer::drawActor);
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
        if (introScene.blinking().state() == Pulse.State.ON) {
            drawSpriteCentered(x + 4, y + 4, energizerSprite);
        }
    }
}