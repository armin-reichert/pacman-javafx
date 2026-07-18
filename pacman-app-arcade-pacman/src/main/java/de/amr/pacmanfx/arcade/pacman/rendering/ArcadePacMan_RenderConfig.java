/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
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

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_RenderConfig implements GameVariantRenderConfig {

    private static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
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

    public Image createBrightEmptyMap() {
        return UfxImages.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES);
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
    public WorldMapColorScheme colorScheme(WorldMap worldMap, WorldSettings worldSettings) {
        requireNonNull(worldMap);
        return GlobalAssets.enhanceContrast(worldSettings, WORLD_MAP_COLOR_SCHEME);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D ignored      -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), BOOT_SCENE_SPRITES);
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D ignored      -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1 ignored  -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene2 ignored  -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene3 ignored  -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas);
            default -> throw new IllegalStateException("Illegal game scene: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public ArcadePacMan_GameLevel_Renderer createGameLevelRenderer(Canvas canvas) {
        requireNonNull(canvas);
        return new ArcadePacMan_GameLevel_Renderer(canvas, assets.image("maze.bright"));
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        requireNonNull(canvas);
        requireNonNull(gameScene2D);
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        requireNonNull(canvas);
        final var actorRenderer = new ArcadePacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimationContainer container, byte personality) {
        final Ghost ghost = switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> GhostFactory.createRedGhostShadow("Blinky");
            case GameModel.PINK_GHOST_SPEEDY -> GhostFactory.createPinkGhostAmbusher("Pinky");
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Clyde");
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
        ghost.setAnimations(createGhostAnimations(container, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer container, byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return new ArcadePacMan_GhostAnimations(container, personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations(SpriteAnimationContainer container) {
        return new ArcadePacMan_PacAnimations(container, spriteSheet());
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
