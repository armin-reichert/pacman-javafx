/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import java.util.Optional;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ROSE;

public class ArcadePacMan_IntroScene_Renderer extends BaseRenderer {

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
            case ArcadePacMan_IntroScene _ -> {}
            case Energizer energizer -> renderEnergizer(energizer);
            case Pellet pellet -> renderPellet(pellet);
            default -> super.render(r, tick);
        }
    }

    private void renderPellet(Pellet pellet) {
        if (!pellet.isVisible()) return;
        ctx.save();
        ctx.setImageSmoothing(true);
        ctx.setFill(ARCADE_ROSE);
        ctx.fillRect(scaled(pellet.pos().x()), scaled(pellet.pos().y()), scaled(2), scaled(2));
        ctx.restore();
    }

    private void renderEnergizer(Energizer energizer) {
        if (energizer.isVisible() && energizer.pulse().state() == Pulse.State.ON) {
            ctx.save();
            ctx.setImageSmoothing(true);
            ctx.setFill(ARCADE_ROSE);
            drawSpriteCentered(energizerSprite, energizer.pos().x(), energizer.pos().y());
            ctx.restore();
        }
    }
}