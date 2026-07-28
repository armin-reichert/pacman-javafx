/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_RenderConfig implements GameVariantRenderConfig {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(380, 0, 204, 208);

    /** Colors used by the six Ms. Pac-Man Arcade maps. */
    private static final WorldMapColorScheme[] MAP_COLOR_SCHEMES = {
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("47b7ff", "dedeff", "fcb5ff", "ffff00"),
        new WorldMapColorScheme("de9751", "dedeff", "fcb5ff", "ff0000"),
        new WorldMapColorScheme("2121ff", "ffb751", "fcb5ff", "dedeff"),
        new WorldMapColorScheme("ffb7ff", "ffff00", "fcb5ff", "00ffff"),
        new WorldMapColorScheme("ffb7ae", "ff0000", "fcb5ff", "dedeff")
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
        final RectShort mazeSprite = spriteSheet().findSprites(SpriteID.EMPTY_MAPS)[index];
        final Image mazeImage = spriteSheet().image(mazeSprite);
        final WorldMapColorScheme colorScheme = MAP_COLOR_SCHEMES[index];
        final Map<Color, Color> colorChanges = Map.of(
            Color.valueOf(colorScheme.wallStroke()), ARCADE_WHITE,
            Color.valueOf(colorScheme.door()), Color.TRANSPARENT
        );
        return UfxImages.recolorImage(mazeImage, colorChanges);
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
    public WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        final int index = worldMap.getConfigValue(WorldMapConfigKey.COLOR_MAP_INDEX);
        return GlobalAssets.enhanceContrast(worldSettings, MAP_COLOR_SCHEMES[index]);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, SpriteAnimSystem animSystem, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored        -> new Arcade_BootScene2D_Renderer(gameScene2D, animSystem, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene2D, animSystem, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(this, gameScene2D, canvas);
            case Arcade_PlayScene2D ignored        -> new Arcade_PlayScene2D_Renderer(gameScene2D, animSystem, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1 ignored  -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene2D, animSystem, canvas);
            case ArcadeMsPacMan_CutScene2 ignored  -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene2D, animSystem, canvas);
            case ArcadeMsPacMan_CutScene3 ignored  -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene2D, animSystem, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public ArcadeMsPacMan_GameLevelRenderer createGameLevelRenderer(SpriteAnimSystem animSystem, Canvas canvas) {
        requireNonNull(animSystem);
        requireNonNull(canvas);
        return new ArcadeMsPacMan_GameLevelRenderer(animSystem, canvas, assets);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, SpriteAnimSystem animSystem, Canvas canvas) {
        requireNonNull(gameScene2D);
        requireNonNull(animSystem);
        requireNonNull(canvas);

        final var renderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(animSystem, canvas);
        renderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(renderer);

        return renderer;
    }

    @Override
    public ActorRenderer createActorRenderer(SpriteAnimSystem animSystem, Canvas canvas) {
        requireNonNull(canvas);

        final var renderer = new ArcadeMsPacMan_ActorRenderer(animSystem, canvas);
        renderer.setImageSmoothing(true);

        return renderer;
    }

    @Override
    public ArcadeMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer container, byte personality) {
        requireValidGhostPersonality(personality);
        return new ArcadeMsPacMan_GhostAnimations(container, personality);
    }

    @Override
    public ArcadeMsPacMan_PacAnimations createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadeMsPacMan_PacAnimations(container);
    }

    @Override
    public Ghost createAnimatedGhost(GameContext gameContext, SpriteAnimationContainer container, byte personality) {
        final var factory = new ArcadeMsPacMan_ActorFactory();
        final SpriteAnimSystem animSystem = gameContext.systems().spriteAnim;

        final Ghost ghost = switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> factory.createRedGhost();
            case GameModel.PINK_GHOST_SPEEDY  -> factory.createPinkGhost();
            case GameModel.CYAN_GHOST_BASHFUL -> factory.createCyanGhost();
            case GameModel.ORANGE_GHOST_POKEY -> factory.createOrangeGhost();
            default -> throw new IllegalStateException("Illegal personality: " + personality);
        };

        animSystem.setAnimations(ghost, createGhostAnimations(container, personality));
        animSystem.select(ghost, CommonAnimationID.GHOST_NORMAL);

        return ghost;
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

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedGhostIndex]);
    }
}
