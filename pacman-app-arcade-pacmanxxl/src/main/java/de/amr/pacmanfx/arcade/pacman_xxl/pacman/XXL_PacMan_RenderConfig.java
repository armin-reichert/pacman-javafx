/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;


import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
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

public class XXL_PacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);

    private final AssetMap assets;

    public XXL_PacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
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
    public XXL_PacMan_GameLevelRenderer createGameLevelRenderer(SpriteAnimController animSystem, Canvas canvas) {
        return new XXL_PacMan_GameLevelRenderer(canvas);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene gameScene, SpriteAnimController animSystem, Canvas canvas) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        final GameScene2D_Renderer renderer = switch (gameScene) {
            case Arcade_BootScene2D ignored -> new Arcade_BootScene2D_Renderer(gameScene, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene, animSystem, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene, canvas);
            case Arcade_PlayScene2D ignored -> new Arcade_PlayScene2D_Renderer(gameScene, animSystem, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored -> new ArcadePacMan_CutScene1_Renderer(gameScene, animSystem, canvas);
            case ArcadePacMan_CutScene2  ignored -> new ArcadePacMan_CutScene2_Renderer(gameScene, animSystem, canvas);
            case ArcadePacMan_CutScene3  ignored -> new ArcadePacMan_CutScene3_Renderer(gameScene, animSystem, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        };
        return r2D.configureRenderer(renderer);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene gameScene, SpriteAnimController animSystem, Canvas canvas) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        r2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(SpriteAnimController animSystem, Canvas canvas) {
        final var actorRenderer = new ArcadePacMan_ActorRenderer(animSystem, canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createAnimatedGhost(GameContext game, SpriteAnimationContainer container, GhostPersonality personality) {
        final var factory = ArcadePacMan_ActorFactory.instance();
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY  -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        final SpriteAnimController animSystem = game.variant().systems().spriteAnimController();
        animSystem.setAnimations(ghost, createGhostAnimations(container, personality));
        animSystem.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public ArcadePacMan_GhostSAM createGhostAnimations(SpriteAnimationContainer container, GhostPersonality personality) {
        return new ArcadePacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadePacMan_PacSAM createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadePacMan_PacSAM(container, spriteSheet());
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
