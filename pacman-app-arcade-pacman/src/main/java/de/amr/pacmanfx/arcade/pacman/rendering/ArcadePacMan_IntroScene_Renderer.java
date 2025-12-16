/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_IntroScene;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_IntroScene_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    private static final String MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";
    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    private static final Color[]  GHOST_COLORS     = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private static final byte LEFT_TILE_X = 4;
    private static final short ENERGIZER_X = TS * LEFT_TILE_X;
    private static final short ENERGIZER_Y = TS * 20;

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final ArcadePacMan_Actor_Renderer actorRenderer;
    private final RectShort energizerSprite;

    public ArcadePacMan_IntroScene_Renderer(GameScene2D scene, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = adaptRenderer((ArcadePacMan_Actor_Renderer) uiConfig.createActorRenderer(canvas), scene);

        debugRenderer = adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas) {
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
        final var intro = (ArcadePacMan_IntroScene) scene;
        clearCanvas();
        drawGhostGallery(intro);
        switch (intro.state()) {
            case SHOWING_POINTS -> drawPoints(intro);
            case CHASING_PAC -> {
                drawBlinkingEnergizer(intro, ENERGIZER_X, ENERGIZER_Y);
                drawRumblingGuys(intro);
                drawPoints(intro);
                drawCopyright();
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawRumblingGuys(intro);
                drawPoints(intro);
                drawCopyright();
            }
        }
        if (intro.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }

    private void drawGhostGallery(ArcadePacMan_IntroScene introScene) {
        ctx.setFont(arcadeFont8());
        if (introScene.titleVisible()) {
            fillText("CHARACTER / NICKNAME", ARCADE_WHITE, TS(LEFT_TILE_X + 3), TS(6));
        }
        final int y = TS * 8;
        for (byte p = RED_GHOST_SHADOW; p <= ORANGE_GHOST_POKEY; ++p) {
            int offsetY = 3 * p * TS;
            if (introScene.ghostImageVisible(p)) {
                RectShort sprite = spriteSheet.spriteSequence(GALLERY_GHOSTS)[p];
                drawSpriteCentered(TS * 5, y + offsetY - HTS, sprite);
            }
            if (introScene.ghostCharacterVisible(p)) {
                fillText("-" + GHOST_CHARACTERS[p], GHOST_COLORS[p], TS * 7, y + offsetY);
            }
            if (introScene.ghostNicknameVisible(p)) {
                fillText(GHOST_NICKNAMES[p], GHOST_COLORS[p], TS * 18, y + offsetY);
            }
        }
    }

    private void drawRumblingGuys(ArcadePacMan_IntroScene introScene) {
        introScene.ghosts().forEach(actorRenderer::drawActor);
        actorRenderer.drawActor(introScene.pacMan());
    }

    private void drawCopyright() {
        fillText(MIDWAY_MFG_CO, ARCADE_PINK, arcadeFont8(), TS(4), TS(32));
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