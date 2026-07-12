/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.core.Validations;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.JsonConfigLoader;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

/**
 * The Arcade Pac‑Man game variant.
 */
public class ArcadePacManGameVariant implements GameVariantConfig, ResourceManager {

    public static final WorldSettings WORLD_CONFIG = JsonConfigLoader.load(
        GameUI.class.getResource("/de/amr/pacmanfx/ui/world.json"), WorldSettings.class);

    public static final WorldMapColorScheme WORLD_MAP_COLOR_SCHEME = new WorldMapColorScheme(
        ARCADE_BLACK.toString(), ARCADE_BLUE.toString(), ARCADE_PINK.toString(), ARCADE_ROSE.toString()
    );

    private static final Map<Color, Color> BRIGHT_MAZE_COLOR_CHANGES = Map.of(
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.wallStroke()), ARCADE_WHITE,   // wall color change
        Color.valueOf(WORLD_MAP_COLOR_SCHEME.door()), Color.TRANSPARENT // door color change
    );

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new ArcadePacMan_Factory3D();

    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects soundEffects;

    public ArcadePacManGameVariant() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts");
    }

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacManGameVariant.class;
    }

    // GameVariant interface

    @Override
    public void init(PacManGamesCollection game) {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(game.ui().sounds());
        gameSceneConfig = new ArcadePacMan_GameSceneConfig(game);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        assets().dispose();
        gameSceneConfig.dispose();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public WorldSettings worldSettings() {
        return WORLD_CONFIG;
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        requireNonNull(worldMap);
        return GlobalAssets.enhanceContrast(worldSettings(), WORLD_MAP_COLOR_SCHEME);
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
    public Ghost createAnimatedGhost(SpriteAnimationContainer animationSet, byte personality) {
        final Ghost ghost = switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> GhostFactory.createRedGhostShadow("Blinky");
            case GameModel.PINK_GHOST_SPEEDY -> GhostFactory.createPinkGhostAmbusher("Pinky");
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Clyde");
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
        ghost.setAnimations(createGhostAnimations(animationSet, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public ArcadePacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer animationSet, byte personality) {
        Validations.requireValidGhostPersonality(personality);
        return new ArcadePacMan_GhostAnimations(animationSet, personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations(SpriteAnimationContainer animationSet) {
        return new ArcadePacMan_PacAnimations(animationSet, spriteSheet());
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbolCode]);
    }

    // private

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon", loadImage("graphics/icons/pacman.png"));
        assets.register("color.game_over_message", ARCADE_RED);
        assets.register("maze.bright", UfxImages.recolorImage(spriteSheet().image(SpriteID.MAP_EMPTY), BRIGHT_MAZE_COLOR_CHANGES));
    }

    private void initSound(SoundManager soundManager) {
        soundManager.setAudioClip    (PacManGameSoundID.BONUS_EATEN,      url("sound/eat_fruit.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.setAudioClip    (PacManGameSoundID.EXTRA_LIFE,       url("sound/extend.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.GAME_OVER,        url("sound/common/game-over.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.GAME_READY,       url("sound/game_start.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.GHOST_EATEN,      url("sound/eat_ghost.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.GHOST_RETURNS,    url("sound/retreating.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_1,   url("sound/intermission.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_2,   url("sound/intermission.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.INTERMISSION_3,   url("sound/intermission.mp3"));
        soundManager.setAudioClip    (PacManGameSoundID.LEVEL_CHANGED,    url("sound/common/sweep.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,   url("sound/common/level-complete.mp3"));
        soundManager.setMediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,    url("sound/pacman_death.wav"));
        soundManager.setAudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.setMediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,    url("sound/ghost-turn-to-blue.mp3"));

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            url("sound/siren_1.mp3"),
            url("sound/siren_2.mp3"),
            url("sound/siren_3.mp3"),
            url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}