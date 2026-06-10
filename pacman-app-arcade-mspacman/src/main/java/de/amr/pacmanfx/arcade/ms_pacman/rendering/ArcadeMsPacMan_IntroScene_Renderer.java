/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.ui.Globals_GameUI;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.core.Globals_Core.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadeMsPacMan_IntroScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private static final String TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final MarqueeRenderer marqueeRenderer;
    private final CopyrightRenderer copyrightRenderer;
    private final ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_IntroScene_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        marqueeRenderer = scene.configureRenderer(new MarqueeRenderer(canvas));
        copyrightRenderer = scene.configureRenderer(new CopyrightRenderer(canvas, uiConfig.assets().image("logo.midway")));
        actorRenderer = scene.configureRenderer(uiConfig.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_IntroScene introScene = (ArcadeMsPacMan_IntroScene) scene;

        ctx.setFont(arcadeFont8());
        fillText(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);

        marqueeRenderer.drawMarquee(introScene.marquee);

        introScene.ghosts.forEach(actorRenderer::drawActor);
        actorRenderer.drawActor(introScene.msPacMan);

        switch (introScene.sceneController.state()) {
            case SceneState.GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[introScene.presentedGhostPersonality];
                Color ghostColor = GHOST_COLORS[introScene.presentedGhostPersonality];
                if (introScene.presentedGhostPersonality == RED_GHOST_SHADOW) {
                    fillText("WITH", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                }
                double x = TITLE_X + (ghostName.length() < 4 ? TS(4) : TS(3));
                double y = TOP_Y + TS(6);
                fillText(ghostName, ghostColor, x, y);
            }
            case SceneState.MS_PACMAN_MARCHING_IN, SceneState.READY_TO_PLAY -> {
                fillText("STARRING", ARCADE_WHITE, TITLE_X, TOP_Y + TS(3));
                fillText("MS PAC-MAN", ARCADE_YELLOW, TITLE_X, TOP_Y + TS(6));
            }
            default -> {}
        }
        copyrightRenderer.drawCopyright(TS(6), TS(28));

        if (Globals_GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            debugRenderer.draw(scene);
        }
    }

}
