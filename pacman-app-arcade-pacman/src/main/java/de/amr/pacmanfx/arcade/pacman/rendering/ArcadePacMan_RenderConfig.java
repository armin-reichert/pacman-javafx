/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorSchemeImpl;
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
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_RenderConfig implements GameVariantRenderConfig {

    private static final WorldMapColorSchemeImpl WORLD_MAP_COLOR_SCHEME = new WorldMapColorSchemeImpl(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);

    private final AssetMap assets;

    public ArcadePacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
    }

    @Override
    public void addAssets() {
        assets.addAsset("maze.bright", createBrightEmptyMap());
    }

    private Image createBrightEmptyMap() {
        return Ufx.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES);
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public WorldMapColorSchemeImpl colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        return GlobalAssets.enhanceContrast(worldSettings, WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene gameScene, SpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(gameScene);
        requireNonNull(animSystem);
        requireNonNull(canvas);

        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);

        final GameScene2D_Renderer renderer = switch (gameScene) {
            case Arcade_BootScene2D ignored      -> new Arcade_BootScene2D_Renderer(gameScene, canvas,
                spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene, animSystem, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene, canvas);
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(gameScene, animSystem, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> new ArcadePacMan_CutScene1_Renderer(
                gameScene, animSystem, canvas);
            case ArcadePacMan_CutScene2 ignored  -> new ArcadePacMan_CutScene2_Renderer(gameScene, animSystem, canvas);
            case ArcadePacMan_CutScene3 ignored  -> new ArcadePacMan_CutScene3_Renderer(gameScene, animSystem, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene);
        };
        return r2D.configureRenderer(renderer);
    }

    @Override
    public ArcadePacMan_GameLevel_Renderer createGameLevelRenderer(SpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(canvas);
        return new ArcadePacMan_GameLevel_Renderer(canvas, assets.image("maze.bright"));
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene gameScene, SpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(gameScene);
        requireNonNull(animSystem);
        requireNonNull(canvas);

        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        final var renderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        renderer.setImageSmoothing(true);
        r2D.configureRenderer(renderer);

        return renderer;
    }

    @Override
    public ActorRenderer createActorRenderer(SpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(canvas);
        final var actorRenderer = new ArcadePacMan_ActorRenderer(animSystem, canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality) {
        final var factory = ArcadePacMan_ActorFactory.instance();
        final Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> factory.createRedGhost();
            case PINK_GHOST_SPEEDY  -> factory.createPinkGhost();
            case CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
        };

        animController.setAnimations(ghost, createGhostAnimations(container, personality));
        animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);

        return ghost;
    }

    @Override
    public ArcadePacMan_GhostSAM createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality) {
        requireNonNull(personality);
        return new ArcadePacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadePacMan_PacSAM createPacAnimations(SpriteAnimContainer container) {
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
