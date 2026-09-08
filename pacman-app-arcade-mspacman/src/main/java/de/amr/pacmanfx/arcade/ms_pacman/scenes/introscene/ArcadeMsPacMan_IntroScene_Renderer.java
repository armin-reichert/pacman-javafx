/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes.introscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.arcade.ms_pacman.scenes.introscene.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadeMsPacMan_IntroScene_Renderer extends BaseRenderer {

    private static final String TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    public ArcadeMsPacMan_IntroScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof ArcadeMsPacMan_IntroScene introScene)) {
            return;
        }

        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
        ctx.setFont(arcade8);
        fillText(TITLE, ARCADE_ORANGE, TITLE_X, TITLE_Y);

        switch (introScene.sceneState()) {
            case SceneState.GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[introScene.ghostPresented.ordinal()];
                Color ghostColor = GHOST_COLORS[introScene.ghostPresented.ordinal()];
                if (introScene.ghostPresented == GhostPersonality.RED_GHOST_SHADOW) {
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
    }
}
