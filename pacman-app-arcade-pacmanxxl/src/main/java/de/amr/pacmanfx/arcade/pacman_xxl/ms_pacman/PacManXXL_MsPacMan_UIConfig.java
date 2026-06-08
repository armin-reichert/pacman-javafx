/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.math.RectShort;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_Factory3D;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.gamescene.GameSceneConfig;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationContainer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public final class PacManXXL_MsPacMan_UIConfig implements UIConfig, ResourceManager {

    private static final ResourceManager ARCADE_RES = () -> ArcadeMsPacMan_UIConfig.class;

    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final ArcadeMsPacMan_Factory3D factory3D = new ArcadeMsPacMan_Factory3D();

    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects sfx;

    private Game context;

    public PacManXXL_MsPacMan_UIConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_ms_pacman");
    }

    @Override
    public ResourceBundle textBundle() {
        return textBundle;
    }

    @Override
    public Class<?> resourceRootClass() {
        return getClass();
    }

    @Override
    public void init(Game context) {
        this.context = context;

        gameSceneConfig = new PacManXXL_MsPacMan_GameSceneConfig(context);

        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();

        Logger.info("Register sounds and effects of UI configuration {}", getClass().getSimpleName());
        registerSounds(context.ui().sounds());
        sfx = new GameSoundEffects(context.ui().sounds());
        initSoundEffects();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());

        gameSceneConfig.dispose();

        Logger.info("Dispose assets of UI configuration {}", getClass().getSimpleName());
        disposeAssets();

        if (context != null) {
            Logger.info("Unregister sounds and effects of UI configuration {}", getClass().getSimpleName());
            unregisterSounds(context.ui().sounds());
            sfx.dispose();
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
    public ArcadeMsPacMan_Factory3D factory3D() {
        return factory3D;
    }

    private void loadAssets() {
        assets.clear();

        assets.register("app_icon", loadImage(XXL_PATH + "graphics/icons/mspacman.png"));
        assets.register("logo.midway", ARCADE_RES.loadImage("graphics/midway_logo.png"));
        assets.register("color.game_over_message", ARCADE_RED);
    }

    private void registerSounds(SoundManager soundManager) {
        soundManager.setMediaPlayer (PacManGameSoundID.BONUS_ACTIVE,          ARCADE_RES.url("sound/Fruit_Bounce.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.BONUS_EATEN,           ARCADE_RES.url("sound/Fruit.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.COIN_INSERTED,         ARCADE_RES.url("sound/credit.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_1, url(XXL_PATH + "sound/explosion1.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_2, url(XXL_PATH + "sound/explosion2.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.EXTRA_LIFE,            ARCADE_RES.url("sound/ExtraLife.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_OVER,             ARCADE_RES.url("sound/game-over.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_READY,            ARCADE_RES.url("sound/Start.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.GHOST_EATEN,           ARCADE_RES.url("sound/Ghost.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GHOST_RETURNS,         ARCADE_RES.url("sound/GhostEyes.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_1,        ARCADE_RES.url("sound/Act_1_They_Meet.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_2,        ARCADE_RES.url("sound/Act_2_The_Chase.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_3,        ARCADE_RES.url("sound/Act_3_Junior.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_RES.url("sound/sweep.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_RES.url("sound/level-complete.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_RES.url("sound/Died.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_RES.url("sound/munch.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_RES.url("sound/ScaredGhost.mp3"));
    }

    private void unregisterSounds(SoundManager sounds) {
        sounds.unregisterSound(PacManGameSoundID.BONUS_ACTIVE);
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
        sfx.setMunchingSoundDelay((byte) 0);
        sfx.registerSirens(
            ARCADE_RES.url("sound/GhostNoise1.wav"),
            ARCADE_RES.url("sound/GhostNoise2.wav"),
            ARCADE_RES.url("sound/GhostNoise3.wav"),
            ARCADE_RES.url("sound/GhostNoise4.wav")
        );
        sfx.setSirenVolume(0.33f);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(380, 0, 204, 208);
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(sfx);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return enhanceContrast(worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D        ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(this, gameScene2D, canvas);
            case Arcade_PlayScene2D        ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1  ignored -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene2  ignored -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene3  ignored -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public PacManXXL_MsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameLevelRenderer(canvas);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas) {
        final var hudRenderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.configureRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        final var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createGhostWithAnimations(SpriteAnimationSet animationSet, byte personality) {
        final Ghost ghost = ArcadeMsPacMan_GameModel.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(animationSet, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public SpriteAnimationContainer<SpriteID> createGhostAnimations(SpriteAnimationSet animationSet, byte personality) {
        return new ArcadeMsPacMan_GhostAnimations(animationSet, personality);
    }

    @Override
    public SpriteAnimationContainer<SpriteID> createPacAnimations(SpriteAnimationSet animationSet) {
        return new ArcadeMsPacMan_PacAnimations(animationSet);
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
}