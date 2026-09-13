/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes.startscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_StartScene_Renderer extends BaseRenderer implements SpriteRenderer {

    public ArcadeMsPacMan_StartScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        if (r instanceof StartSceneText text) {
            renderStartSceneText(text);
        }
    }

    private void renderStartSceneText(StartSceneText text) {
        final int tx = text.tileX();
        final int ty = text.tileY();
        final double STS = scaled(TS);
        final Font arcade6 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(6));
        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));
        ctx.setFill(ARCADE_ORANGE);
        ctx.setFont(arcade8);
        ctx.fillText("PUSH START BUTTON",      STS * tx, STS * ty);
        ctx.fillText("1 PLAYER ONLY",          STS * (tx + 2), STS * (ty + 2));
        ctx.fillText("ADDITIONAL    AT 10000", STS * (tx - 4), STS * (ty + 9));
        ctx.setFont(arcade6);
        ctx.fillText("PTS", STS * (tx + 19), STS * (ty + 9));
        drawSprite(spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL), (tx + 7) * TS, (ty + 7) * TS + 1, true);
    }
}