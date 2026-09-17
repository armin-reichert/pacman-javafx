/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.timer.Pulse;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import java.util.Optional;

import static de.amr.pacmanfx.arcade.pacman.gamescene.introscene.ArcadePacMan_IntroScene.SceneState.*;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ROSE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;

public class ArcadePacMan_IntroScene_Renderer extends BaseRenderer {

    private static final byte LEFT_TILE_X = 4;

    private final RectShort energizerSprite;

    public ArcadePacMan_IntroScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);

        energizerSprite = ArcadePacMan_SpriteSheet.instance().findSprite(SpriteID.ENERGIZER);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(ArcadePacMan_SpriteSheet.instance());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case ArcadePacMan_IntroScene scene -> renderScene(scene);
            case ArcadePacMan_IntroScene.BlinkingEnergizer energizer -> renderBlinkingEnergizer(energizer);
            default -> super.render(r, tick);
        }
    }

    private void renderScene(ArcadePacMan_IntroScene introScene) {
        ctx.setImageSmoothing(true);
        switch (introScene.flow.state()) {
            case SHOWING_POINTS, CHASING_PAC_MAN, CHASING_GHOSTS, WAIT_FOR_DEMO_LEVEL -> drawPoints();
            default -> {}
        }
        ctx.setImageSmoothing(false);
    }

    private void drawPoints() {
        final Font arcade6 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(6));
        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));
        ctx.setFill(ARCADE_ROSE);
        // normal pellet
        ctx.fillRect(scaled(tilesPx(LEFT_TILE_X + 6) + 4), scaled(tilesPx(24) + 4), scaled(2), scaled(2));
        fillText("10",  ARCADE_WHITE, arcade8, tilesPx(LEFT_TILE_X + 8), tilesPx(25));
        fillText("PTS", ARCADE_WHITE, arcade6, tilesPx(LEFT_TILE_X + 11), tilesPx(25));
        // energizer
        fillText("50",  ARCADE_WHITE, arcade8, tilesPx(LEFT_TILE_X + 8), tilesPx(27));
        fillText("PTS", ARCADE_WHITE, arcade6, tilesPx(LEFT_TILE_X + 11), tilesPx(27));
    }

    private void renderBlinkingEnergizer(ArcadePacMan_IntroScene.BlinkingEnergizer energizer) {
        if (energizer.isVisible() && energizer.pulse().state() == Pulse.State.ON) {
            ctx.save();
            ctx.setImageSmoothing(true);
            drawSpriteCentered(energizerSprite, energizer.pos().x(), energizer.pos().y());
            ctx.restore();
        }
    }
}