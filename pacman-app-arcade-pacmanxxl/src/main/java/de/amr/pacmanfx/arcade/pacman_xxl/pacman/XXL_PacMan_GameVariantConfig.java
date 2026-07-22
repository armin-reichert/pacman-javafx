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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

public final class XXL_PacMan_GameVariantConfig implements GameVariantConfig {

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

    private static final ResourceManager XXL_RM = () -> XXL_PacMan_GameVariantConfig.class;
    private static final ResourceManager ARCADE_PACMAN_RM = () -> ArcadePacMan_GameVariantConfig.class;
    private static final String XXL_PATH = "/de/amr/pacmanfx/arcade/pacman_xxl/";
    private static final String XXL_PKG = "de.amr.pacmanfx.arcade.pacman_xxl.";

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        audioClip    (PacManGameSoundID.BONUS_EATEN,           ARCADE_PACMAN_RM.url("sound/eat_fruit.mp3")),
        audioClip    (PacManGameSoundID.COIN_INSERTED,         ARCADE_PACMAN_RM.url("sound/credit.wav")),
        audioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_1, XXL_RM.url(XXL_PATH + "sound/explosion1.mp3")),
        audioClip    (PacManGameSoundID.ENERGIZER_EXPLOSION_2, XXL_RM.url(XXL_PATH + "sound/explosion2.mp3")),
        audioClip    (PacManGameSoundID.EXTRA_LIFE,            ARCADE_PACMAN_RM.url("sound/extend.mp3")),
        audioClip    (PacManGameSoundID.GAME_OVER,             ARCADE_PACMAN_RM.url("sound/common/game-over.mp3")),
        mediaPlayer  (PacManGameSoundID.GAME_READY,            ARCADE_PACMAN_RM.url("sound/game_start.mp3")),
        audioClip    (PacManGameSoundID.GHOST_EATEN,           ARCADE_PACMAN_RM.url("sound/eat_ghost.mp3")),
        mediaPlayer  (PacManGameSoundID.GHOST_RETURNS,         ARCADE_PACMAN_RM.url("sound/retreating.mp3")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_1,        ARCADE_PACMAN_RM.url("sound/intermission.mp3")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_2,        ARCADE_PACMAN_RM.url("sound/intermission.mp3")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_3,        ARCADE_PACMAN_RM.url("sound/intermission.mp3")),
        audioClip    (PacManGameSoundID.LEVEL_CHANGED,         ARCADE_PACMAN_RM.url("sound/common/sweep.mp3")),
        mediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,        ARCADE_PACMAN_RM.url("sound/common/level-complete.mp3")),
        mediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,         ARCADE_PACMAN_RM.url("sound/pacman_death.wav")),
        audioClip    (PacManGameSoundID.PAC_MAN_MUNCHING,      ARCADE_PACMAN_RM.url("sound/munch.wav")),
        mediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,         ARCADE_PACMAN_RM.url("sound/ghost-turn-to-blue.mp3"))
    );

    private final ResourceBundle textBundle;
    private final AssetMap assets;
    private final Factory3D factory3D;

    private XXL_PacMan_GameSceneConfig gameSceneConfig;
    private XXL_PacMan_RenderConfig renderConfig;

    private SoundManager sounds;
    private GameSoundEffects soundEffects;

    public XXL_PacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle(XXL_PKG + "localized_texts_pacman");
        assets = new AssetMap();
        factory3D = new DefaultFactory3D();
    }

    @Override
    public void init(GameAppContext appContext) {
        requireNonNull(appContext);

        gameSceneConfig = new XXL_PacMan_GameSceneConfig();

        sounds = appContext.ui().sounds();
        loadSounds();

        assets.addAsset("app_icon", XXL_RM.loadImage(XXL_PATH + "graphics/icons/pacman.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);

        renderConfig = new XXL_PacMan_RenderConfig(assets);
        renderConfig.addAssets();

        assets.freeze();
    }

    @Override
    public void dispose() {
        Logger.info("Dispose game variant configuration {}:", getClass().getSimpleName());

        Logger.info("Dispose game scene configuration");
        gameSceneConfig.dispose();

        Logger.info("Dispose assets");
        assets.dispose();

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
    public Factory3D factory3D() {
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
        return () -> textBundle;
    }

    @Override
    public WorldSettings worldSettings() {
        return WorldSettings.DEFAULT_SETTINGS;
    }

    // private

    private void loadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.add(entry);
        }
        soundEffects = new GameSoundEffects(sounds);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            ARCADE_PACMAN_RM.url("sound/siren_1.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_2.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_3.mp3"),
            ARCADE_PACMAN_RM.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }

    private void unloadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.remove(entry);
        }
        soundEffects.dispose();
    }
}