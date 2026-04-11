/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadeMsPacMan_IntroScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private static final String TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final MarqueeRenderer marqueeRenderer;
    private final ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_IntroScene_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        marqueeRenderer = scene.adaptRenderer(new MarqueeRenderer(canvas));
        actorRenderer = scene.adaptRenderer(uiConfig.createActorRenderer(canvas));
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

        // Midway Copyright
        final Image logo = scene.ui().currentConfig().assets().image("logo.midway");
        final double x = TS(6);
        final double y = TS(28);
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981", scaled(x + TS(8)), scaled(y + TS(4)));

        if (GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            debugRenderer.draw(scene);
        }
    }

}
