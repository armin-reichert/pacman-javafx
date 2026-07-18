/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_Factory3D;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

public final class XXL_MsPacMan_GameVariantConfig implements GameVariantConfig, ResourceManager {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private static final ResourceManager ARCADE_RES = () -> ArcadeMsPacMan_GameVariantConfig.class;

    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final ArcadeMsPacMan_Factory3D factory3D = new ArcadeMsPacMan_Factory3D();

    private GameSceneConfig gameSceneConfig;
    private XXL_MsPacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    private GameAppContext appContext;

    public XXL_MsPacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_ms_pacman");
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
    public void init(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);

        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();

        gameSceneConfig = new XXL_MsPacMan_GameSceneConfig(appContext);
        renderConfig = new XXL_MsPacMan_RenderConfig(assets);

        Logger.info("Register sounds and effects of UI configuration {}", getClass().getSimpleName());
        registerSounds(appContext.ui().sounds());
        soundEffects = new GameSoundEffects(appContext.ui().sounds());
        initSoundEffects();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());

        gameSceneConfig.dispose();

        Logger.info("Dispose assets of UI configuration {}", getClass().getSimpleName());
        assets().dispose();

        if (appContext != null) {
            Logger.info("Unregister sounds and effects of UI configuration {}", getClass().getSimpleName());
            unregisterSounds(appContext.ui().sounds());
            soundEffects.dispose();
        }
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public GameVariantRenderConfig renderConfig() {
        return renderConfig;
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadeMsPacMan_Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_CONFIG;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public Image killedGhostPointsImage(int killedGhostIndex) {
        final RectShort[] numberSprites = renderConfig.spriteSheet().findSprites(SpriteID.GHOST_NUMBERS);
        return renderConfig.spriteSheet().image(numberSprites[killedGhostIndex]);
    }

    @Override
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] sprites = renderConfig.spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS);
        return renderConfig.spriteSheet().image(sprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final RectShort[] sprites = renderConfig.spriteSheet().findSprites(SpriteID.BONUS_VALUES);
        return renderConfig.spriteSheet().image(sprites[symbolCode]);
    }

    // -----

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
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.registerSirens(
            ARCADE_RES.url("sound/GhostNoise1.wav"),
            ARCADE_RES.url("sound/GhostNoise2.wav"),
            ARCADE_RES.url("sound/GhostNoise3.wav"),
            ARCADE_RES.url("sound/GhostNoise4.wav")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}