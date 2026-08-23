/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
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
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

    /** Colors used by the six Ms. Pac-Man Arcade maps. */
    private static final GenericWorldMapColorScheme[] MAP_COLOR_SCHEMES = {
        new GenericWorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new GenericWorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new GenericWorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new GenericWorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new GenericWorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new GenericWorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
    };

    private final AssetMap assets;

    public ArcadeMsPacMan_RenderConfig(AssetMap assets) {
        this.assets = assets;
    }

    @Override
    public void addAssets() {
        for (int i = 0; i < MAP_COLOR_SCHEMES.length; ++i) {
            assets.addAsset("maze.bright.%d".formatted(i), createBrightMazeImage(i));
        }
    }

    // Creates the maze image used in the flash animation at the end of each level
    private Image createBrightMazeImage(int index) {
        final RectShort mazeSprite = spriteSheet().findSpriteSequence(SpriteID.EMPTY_MAPS)[index];
        final Image mazeImage = spriteSheet().image(mazeSprite);
        final GenericWorldMapColorScheme colorScheme = MAP_COLOR_SCHEMES[index];
        final Map<Color, Color> colorChanges = Map.of(
            Color.valueOf(colorScheme.wallStroke()), ARCADE_WHITE,
            Color.valueOf(colorScheme.door()), Color.TRANSPARENT
        );
        return Ufx.recolorImage(mazeImage, colorChanges);
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
    public GenericWorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        final int index = worldMap.getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);
        return GlobalAssets.enhanceContrast(worldSettings, MAP_COLOR_SCHEMES[index]);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);

        final GameScene2D_Renderer renderer = switch (gameScene) {
            case Arcade_BootScene2D ignored        -> new Arcade_BootScene2D_Renderer(gameScene, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(this, gameScene, canvas);
            case Arcade_PlayScene2D ignored        -> new Arcade_PlayScene2D_Renderer(gameScene, animSystem, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1 ignored  -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_CutScene2 ignored  -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene, animSystem, canvas);
            case ArcadeMsPacMan_CutScene3 ignored  -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene, animSystem, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene);
        };
        return r2D.configureRenderer(renderer);
    }

    @Override
    public ArcadeMsPacMan_GameLevelRenderer createGameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(animSystem);
        requireNonNull(canvas);
        return new ArcadeMsPacMan_GameLevelRenderer(animSystem, canvas, assets);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(gameScene);
        requireNonNull(animSystem);
        requireNonNull(canvas);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        final var renderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        renderer.setImageSmoothing(true);
        r2D.configureRenderer(renderer);

        return renderer;
    }

    @Override
    public ActorRenderer createActorRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        requireNonNull(canvas);

        final var renderer = new ArcadeMsPacMan_ActorRenderer(animSystem, canvas);
        renderer.setImageSmoothing(true);

        return renderer;
    }

    @Override
    public ArcadeMsPacMan_GhostSAM createGhostAnimations(SpriteAnimContainer container, GhostPersonality personality) {
        requireNonNull(personality);
        return new ArcadeMsPacMan_GhostSAM(container, personality);
    }

    @Override
    public ArcadeMsPacMan_PacSAM createPacAnimations(SpriteAnimContainer container) {
        return new ArcadeMsPacMan_PacSAM(container);
    }

    @Override
    public Ghost createAnimatedGhost(ActorSpriteAnimController animController, SpriteAnimContainer container, GhostPersonality personality) {
        final var factory = new ArcadeMsPacMan_ActorFactory();

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
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }
}
