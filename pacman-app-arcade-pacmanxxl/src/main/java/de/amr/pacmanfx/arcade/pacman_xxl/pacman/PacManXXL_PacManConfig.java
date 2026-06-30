/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.ArcadePacManConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostFactory;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public final class PacManXXL_PacManConfig implements GameVariantConfig, ResourceManager {

    private static final ResourceManager ARCADE_PACMAN_RES = () -> ArcadePacManConfig.class;

    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final Factory3D factory3D = new DefaultFactory3D();

    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects soundEffects;

    private Game game;

    public PacManXXL_PacManConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_pacman");
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public Class<?> resourceRootClass() {
        return getClass();
    }

    @Override
    public void init(Game game) {
        this.game = game;

        gameSceneConfig = new PacManXXL_PacMan_GameSceneConfig(game);

        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();

        Logger.info("Register sounds and effects of UI configuration {}", getClass().getSimpleName());
        registerSounds(game.ui().sounds());
        soundEffects = new GameSoundEffects(game.ui().sounds());
        initSoundEffects();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());

        gameSceneConfig.dispose();

        Logger.info("Dispose assets of UI configuration {}", getClass().getSimpleName());
        assets().dispose();

        if (game != null) {
            Logger.info("Unregister sounds and effects of UI configuration {}", getClass().getSimpleName());
            unregisterSounds(game.ui().sounds());
            soundEffects.dispose();
        }
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
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
    public WorldSettings worldSettings() {
        return ArcadePacManConfig.WORLD_CONFIG;
    }

    @Override
    public PacManXXL_PacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_PacMan_GameLevelRenderer(canvas);
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D      ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadePacMan_IntroScene ignored -> new ArcadePacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadePacMan_StartScene ignored -> new ArcadePacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D      ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadePacMan_CutScene1  ignored -> new ArcadePacMan_CutScene1_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene2  ignored -> new ArcadePacMan_CutScene2_Renderer(gameScene2D, canvas);
            case ArcadePacMan_CutScene3  ignored -> new ArcadePacMan_CutScene3_Renderer(gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        final var hudRenderer = new ArcadePacMan_HeadsUpDisplay_Renderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        final var actorRenderer = new ArcadePacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
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

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return enhanceContrast(worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(400, 0, 256, 160);
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
        return new ArcadePacMan_GhostAnimations(animationSet, personality);
    }

    @Override
    public ArcadePacMan_PacAnimations createPacAnimations(SpriteAnimationContainer animationSet) {
        return new ArcadePacMan_PacAnimations(animationSet, spriteSheet());
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    // -----

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon", loadImage(XXL_PATH + "graphics/icons/pacman.png"));
        assets.register("color.game_over_message", ARCADE_RED);
    }

    private void registerSounds(SoundManager sounds) {
        sounds.setAudioClip    (PacManGameSoundID.BONUS_EATEN,           ARCADE_PACMAN_RES.url("sound/eat_fruit.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.COIN_INSERTED,         ARCADE_PACMAN_RES.url("sound/credit.wav"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_1, url(XXL_PATH + "sound/explosion1.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_2, url(XXL_PATH + "sound/explosion2.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.EXTRA_LIFE,            ARCADE_PACMAN_RES.url("sound/extend.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GAME_OVER,             ARCADE_PACMAN_RES.url("sound/common/game-over.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GAME_READY,            ARCADE_PACMAN_RES.url("sound/game_start.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GHOST_EATEN,           ARCADE_PACMAN_RES.url("sound/eat_ghost.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GHOST_RETURNS,         ARCADE_PACMAN_RES.url("sound/retreating.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_1,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_2,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_3,        ARCADE_PACMAN_RES.url("sound/intermission.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_PACMAN_RES.url("sound/common/sweep.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_PACMAN_RES.url("sound/common/level-complete.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_PACMAN_RES.url("sound/pacman_death.wav"));
        sounds.setAudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_PACMAN_RES.url("sound/munch.wav"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_PACMAN_RES.url("sound/ghost-turn-to-blue.mp3"));
    }

    private void unregisterSounds(SoundManager sounds) {
        sounds.unregisterSound(PacManGameSoundID.BONUS_EATEN);
        sounds.unregisterSound(PacManGameSoundID.COIN_INSERTED);
        sounds.unregisterSound(PacManGameSoundID.ENERGIZER_EXPLOSION_1);
        sounds.unregisterSound(PacManGameSoundID.ENERGIZER_EXPLOSION_2);
        sounds.unregisterSound(PacManGameSoundID.EXTRA_LIFE);
        sounds.unregisterSound(PacManGameSoundID.GAME_OVER);
        sounds.unregisterSound(PacManGameSoundID.GAME_READY);
        sounds.unregisterSound(PacManGameSoundID.GHOST_EATEN);
        sounds.unregisterSound(PacManGameSoundID.GHOST_RETURNS);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_1);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_2);
        sounds.unregisterSound(PacManGameSoundID.INTERMISSION_3);
        sounds.unregisterSound(PacManGameSoundID.LEVEL_CHANGED);
        sounds.unregisterSound(PacManGameSoundID.LEVEL_COMPLETE);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_DEATH);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_MUNCHING);
        sounds.unregisterSound(PacManGameSoundID.PAC_MAN_POWER);
    }

    private void initSoundEffects() {
        soundEffects.setMunchingSoundDelay((byte) 9);

        soundEffects.registerSirens(
            ARCADE_PACMAN_RES.url("sound/siren_1.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_2.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_3.mp3"),
            ARCADE_PACMAN_RES.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }

}