/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.DefaultFactory3D;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
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

public final class XXL_PacMan_GameVariantConfig implements GameVariantConfig {

    private static final ResourceManager XXL_RM = () -> XXL_PacMan_GameVariantConfig.class;
    private static final ResourceManager ARCADE_PACMAN_RM = () -> ArcadePacMan_GameVariantConfig.class;
    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    /**
     * Used by cartridge.
     *
     * @return the game flow controller for this variant
     */
    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Pac-Man XXL Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private final ResourceBundle textBundle;
    private final AssetMap assets;
    private final Factory3D factory3D;

    private GameSceneConfig gameSceneConfig;
    private XXL_PacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    private GameAppContext appContext;

    public XXL_PacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_pacman");
        assets = new AssetMap();
        factory3D = new DefaultFactory3D();
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public void init(GameAppContext appContext) {
        this.appContext = appContext;

        gameSceneConfig = new XXL_PacMan_GameSceneConfig(appContext);

        assets.addAsset("app_icon", XXL_RM.loadImage(XXL_PATH + "graphics/icons/pacman.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);
        renderConfig = new XXL_PacMan_RenderConfig(assets);
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
    public Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_SETTINGS;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    // -----

    private void registerSounds(SoundManager sounds) {
        sounds.setAudioClip    (PacManGameSoundID.BONUS_EATEN,           ARCADE_PACMAN_RM.url("sound/eat_fruit.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.COIN_INSERTED,         ARCADE_PACMAN_RM.url("sound/credit.wav"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_1, XXL_RM.url(XXL_PATH + "sound/explosion1.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_2, XXL_RM.url(XXL_PATH + "sound/explosion2.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.EXTRA_LIFE,            ARCADE_PACMAN_RM.url("sound/extend.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GAME_OVER,             ARCADE_PACMAN_RM.url("sound/common/game-over.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GAME_READY,            ARCADE_PACMAN_RM.url("sound/game_start.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.GHOST_EATEN,           ARCADE_PACMAN_RM.url("sound/eat_ghost.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.GHOST_RETURNS,         ARCADE_PACMAN_RM.url("sound/retreating.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_1,        ARCADE_PACMAN_RM.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_2,        ARCADE_PACMAN_RM.url("sound/intermission.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.INTERMISSION_3,        ARCADE_PACMAN_RM.url("sound/intermission.mp3"));
        sounds.setAudioClip    (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_PACMAN_RM.url("sound/common/sweep.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_PACMAN_RM.url("sound/common/level-complete.mp3"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_PACMAN_RM.url("sound/pacman_death.wav"));
        sounds.setAudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_PACMAN_RM.url("sound/munch.wav"));
        sounds.setMediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_PACMAN_RM.url("sound/ghost-turn-to-blue.mp3"));
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
            ARCADE_PACMAN_RM.url("sound/siren_1.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_2.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_3.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }
}