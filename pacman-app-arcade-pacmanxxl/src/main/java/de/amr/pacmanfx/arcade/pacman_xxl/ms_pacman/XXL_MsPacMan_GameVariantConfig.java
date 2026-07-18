/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_Factory3D;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

public final class XXL_MsPacMan_GameVariantConfig implements GameVariantConfig {

    private static final ResourceManager XXL_RM = () -> XXL_MsPacMan_GameVariantConfig.class;
    private static final ResourceManager ARCADE_RM = () -> ArcadeMsPacMan_GameVariantConfig.class;
    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

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
    public void init(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);

        gameSceneConfig = new XXL_MsPacMan_GameSceneConfig(appContext);

        assets.addAsset("app_icon", XXL_RM.loadImage(XXL_PATH + "graphics/icons/mspacman.png"));
        assets.addAsset("logo.midway", ARCADE_RM.loadImage("graphics/midway_logo.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);
        renderConfig = new XXL_MsPacMan_RenderConfig(assets);
        renderConfig.addAssets();
        assets.freeze();

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

    // -----

    private void registerSounds(SoundManager soundManager) {
        soundManager.setMediaPlayer (PacManGameSoundID.BONUS_ACTIVE,          ARCADE_RM.url("sound/Fruit_Bounce.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.BONUS_EATEN,           ARCADE_RM.url("sound/Fruit.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.COIN_INSERTED,         ARCADE_RM.url("sound/credit.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_1, XXL_RM.url(XXL_PATH + "sound/explosion1.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_2, XXL_RM.url(XXL_PATH + "sound/explosion2.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.EXTRA_LIFE,            ARCADE_RM.url("sound/ExtraLife.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_OVER,             ARCADE_RM.url("sound/game-over.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_READY,            ARCADE_RM.url("sound/Start.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.GHOST_EATEN,           ARCADE_RM.url("sound/Ghost.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GHOST_RETURNS,         ARCADE_RM.url("sound/GhostEyes.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_1,        ARCADE_RM.url("sound/Act_1_They_Meet.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_2,        ARCADE_RM.url("sound/Act_2_The_Chase.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_3,        ARCADE_RM.url("sound/Act_3_Junior.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_RM.url("sound/sweep.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_RM.url("sound/level-complete.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_RM.url("sound/Died.mp3"));
        soundManager.setAudioClip   (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_RM.url("sound/munch.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_RM.url("sound/ScaredGhost.mp3"));
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
            ARCADE_RM.url("sound/GhostNoise1.wav"),
            ARCADE_RM.url("sound/GhostNoise2.wav"),
            ARCADE_RM.url("sound/GhostNoise3.wav"),
            ARCADE_RM.url("sound/GhostNoise4.wav")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}