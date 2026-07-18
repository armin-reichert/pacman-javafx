/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;


import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.MapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.*;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_GhostAnimations;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_PacAnimations;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private final AssetMap assets;

    public TengenMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        final MapColorScheme scheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final WorldMapColorScheme colorScheme = new WorldMapColorScheme(
            scheme.wallFill(), scheme.wallStroke(), scheme.door(), scheme.pellet());
        return GlobalAssets.enhanceContrast(worldSettings, colorScheme);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case TengenMsPacMan_BootScene ignored -> new TengenMsPacMan_BootScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_IntroScene ignored -> new TengenMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_OptionsScene ignored -> new TengenMsPacMan_OptionsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_PlayScene2D ignored -> new TengenMsPacMan_PlayScene2D_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CreditsScene ignored -> new TengenMsPacMan_CreditsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene1    ignored -> new TengenMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene2    ignored -> new TengenMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene3    ignored -> new TengenMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene4    ignored -> new TengenMsPacMan_CutScene4_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new TengenMsPacMan_GameLevelRenderer(assets, canvas);
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        return gameScene2D.configureRenderer(new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas));
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(canvas);
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(container, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer container, byte personality) {
        return new TengenMsPacMan_GhostAnimations(container, personality);
    }

    @Override
    public TengenMsPacMan_PacAnimations createPacAnimations(SpriteAnimationContainer container) {
        return new TengenMsPacMan_PacAnimations(container);
    }
}
