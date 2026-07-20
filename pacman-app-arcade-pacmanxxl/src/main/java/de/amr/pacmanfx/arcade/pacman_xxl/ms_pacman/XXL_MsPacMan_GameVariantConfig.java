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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.AudioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.MediaPlayer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

public final class XXL_MsPacMan_GameVariantConfig implements GameVariantConfig {

    private static final ResourceManager XXL_RM = () -> XXL_MsPacMan_GameVariantConfig.class;
    private static final ResourceManager ARCADE_RM = () -> ArcadeMsPacMan_GameVariantConfig.class;
    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private static final List<SoundEntry> SOUND_ENTRIES = Arrays.asList(
        MediaPlayer (PacManGameSoundID.BONUS_ACTIVE,          ARCADE_RM.url("sound/Fruit_Bounce.mp3")),
        AudioClip   (PacManGameSoundID.BONUS_EATEN,           ARCADE_RM.url("sound/Fruit.mp3")),
        AudioClip   (PacManGameSoundID.COIN_INSERTED,         ARCADE_RM.url("sound/credit.wav")),
        AudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_1, XXL_RM.url(XXL_PATH + "sound/explosion1.mp3")),
        AudioClip   (PacManGameSoundID.ENERGIZER_EXPLOSION_2, XXL_RM.url(XXL_PATH + "sound/explosion2.mp3")),
        AudioClip   (PacManGameSoundID.EXTRA_LIFE,            ARCADE_RM.url("sound/ExtraLife.mp3")),
        MediaPlayer (PacManGameSoundID.GAME_OVER,             ARCADE_RM.url("sound/game-over.mp3")),
        MediaPlayer (PacManGameSoundID.GAME_READY,            ARCADE_RM.url("sound/Start.mp3")),
        AudioClip   (PacManGameSoundID.GHOST_EATEN,           ARCADE_RM.url("sound/Ghost.mp3")),
        MediaPlayer (PacManGameSoundID.GHOST_RETURNS,         ARCADE_RM.url("sound/GhostEyes.mp3")),
        MediaPlayer (PacManGameSoundID.INTERMISSION_1,        ARCADE_RM.url("sound/Act_1_They_Meet.mp3")),
        MediaPlayer (PacManGameSoundID.INTERMISSION_2,        ARCADE_RM.url("sound/Act_2_The_Chase.mp3")),
        MediaPlayer (PacManGameSoundID.INTERMISSION_3,        ARCADE_RM.url("sound/Act_3_Junior.mp3")),
        AudioClip   (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_RM.url("sound/sweep.mp3")),
        MediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_RM.url("sound/level-complete.mp3")),
        MediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_RM.url("sound/Died.mp3")),
        AudioClip   (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_RM.url("sound/munch.wav")),
        MediaPlayer (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_RM.url("sound/ScaredGhost.mp3"))
    );

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private final TranslationManager translations;
    private final AssetMap assets;
    private final ArcadeMsPacMan_Factory3D factory3D;

    private XXL_MsPacMan_GameSceneConfig gameSceneConfig;
    private XXL_MsPacMan_RenderConfig renderConfig;

    private SoundManager sounds;
    private GameSoundEffects soundEffects;

    public XXL_MsPacMan_GameVariantConfig() {
        translations = () -> ResourceBundle.getBundle(XXL_PKG + "localized_texts_ms_pacman");
        assets = new AssetMap();
        factory3D = new ArcadeMsPacMan_Factory3D();
    }

    @Override
    public void init(GameAppContext appContext) {
        requireNonNull(appContext);

        gameSceneConfig = new XXL_MsPacMan_GameSceneConfig(appContext);

        sounds = appContext.ui().sounds();
        loadSounds();

        assets.addAsset("app_icon", XXL_RM.loadImage(XXL_PATH + "graphics/icons/mspacman.png"));
        assets.addAsset("logo.midway", ARCADE_RM.loadImage("graphics/midway_logo.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);

        renderConfig = new XXL_MsPacMan_RenderConfig(assets);
        renderConfig.addAssets();

        assets.freeze();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose game variant configuration {}:", getClass().getSimpleName());

        Logger.info("Dispose game scene configuration");
        gameSceneConfig.dispose();

        Logger.info("Dispose assets");
        assets().dispose();

        Logger.info("Unload sounds");
        if (sounds != null) {
            unloadSounds();
        }
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
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public GameVariantRenderConfig renderConfig() {
        return renderConfig;
    }

    @Override
    public TranslationManager translations() {
        return translations;
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_SETTINGS;
    }

    // private

    private void loadSounds() {
        for (SoundEntry entry : SOUND_ENTRIES) {
            sounds.add(entry);
        }
        soundEffects = new GameSoundEffects(sounds);
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.registerSirens(
            ARCADE_RM.url("sound/GhostNoise1.wav"),
            ARCADE_RM.url("sound/GhostNoise2.wav"),
            ARCADE_RM.url("sound/GhostNoise3.wav"),
            ARCADE_RM.url("sound/GhostNoise4.wav")
        );
        soundEffects.setSirenVolume(0.33f);
    }

    private void unloadSounds() {
        for (SoundEntry entry : SOUND_ENTRIES) {
            sounds.remove(entry);
        }
        soundEffects.dispose();
    }
}