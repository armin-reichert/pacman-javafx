/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadeMsPacMan_IntroScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private static final String TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final MarqueeRenderer marqueeRenderer;
    private final CopyrightRenderer copyrightRenderer;
    private final ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    private final Image copyrightImage;

    public ArcadeMsPacMan_IntroScene_Renderer(GameVariantConfig gameVariant, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);

        copyrightImage = gameVariant.assets().image("logo.midway");

        marqueeRenderer = scene.configureRenderer(new MarqueeRenderer(canvas));
        copyrightRenderer = scene.configureRenderer(new CopyrightRenderer(canvas));
        actorRenderer = scene.configureRenderer(gameVariant.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        clearCanvas();

        final ArcadeMsPacMan_IntroScene introScene = (ArcadeMsPacMan_IntroScene) scene;

        ctx.setFont(arcadeFont8());
        fillText(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);

        marqueeRenderer.drawMarquee(introScene.marquee);

        introScene.ghosts.forEach(actorRenderer::drawActor);
        actorRenderer.drawActor(introScene.msPacMan);

        switch (introScene.sceneState()) {
            case SceneState.GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[introScene.presentedGhostPersonality];
                Color ghostColor = GHOST_COLORS[introScene.presentedGhostPersonality];
                if (introScene.presentedGhostPersonality == RED_GHOST_SHADOW) {
                    fillText("WITH", ARCADE_WHITE, TITLE_X, TOP_Y + tilesPx(3));
                }
                double x = TITLE_X + (ghostName.length() < 4 ? tilesPx(4) : tilesPx(3));
                double y = TOP_Y + tilesPx(6);
                fillText(ghostName, ghostColor, x, y);
            }
            case SceneState.MS_PACMAN_MARCHING_IN, SceneState.READY_TO_PLAY -> {
                fillText("STARRING", ARCADE_WHITE, TITLE_X, TOP_Y + tilesPx(3));
                fillText("MS PAC-MAN", ARCADE_YELLOW, TITLE_X, TOP_Y + tilesPx(6));
            }
            default -> {}
        }
        copyrightRenderer.drawCopyright(copyrightImage, tilesPx(6), tilesPx(28));

        if (scene.actionContext().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }

}
