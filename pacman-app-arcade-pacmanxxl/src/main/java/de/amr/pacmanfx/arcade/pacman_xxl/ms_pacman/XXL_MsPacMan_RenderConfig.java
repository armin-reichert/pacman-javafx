/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.Rendering2DSupport;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class XXL_MsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

    private final AssetMap assets;

    public XXL_MsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public WorldMapColorSchemeImpl colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        return GlobalAssets.enhanceContrast(worldSettings, worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        final GameScene2D_Renderer renderer = switch (gameScene) {
            case Arcade_BootScene2D ignored -> new Arcade_BootScene2D_Renderer(gameScene, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(this, gameScene, canvas);
            case Arcade_PlayScene2D ignored -> new Arcade_PlayScene2D_Renderer(gameScene, animSystem, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1 ignored -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_CutScene2 ignored -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_CutScene3 ignored -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene, animSystem, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        };
        return r2D.configureRenderer(renderer);
    }

    @Override
    public XXL_MsPacMan_GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        return new XXL_MsPacMan_GameLevelRenderer(animSystem, canvas);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        final var hudRenderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        hudRenderer.setImageSmoothing(true);
        r2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        final var actorRenderer = new ArcadeMsPacMan_ActorRenderer(animSystem, canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality) {
        final var factory = new ArcadeMsPacMan_ActorFactory();

        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        animController.setAnimations(ghost, createGhostAnimations(container, personality));
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public LazySAM createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality) {
        return new ArcadeMsPacMan_GhostSAM(container, personality);
    }

    @Override
    public LazySAM createPacAnimations(SpriteAnimContainer container) {
        return new ArcadeMsPacMan_PacSAM(container);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbolCode]);
    }
}
