/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_RenderConfig;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
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

public class ArcadeMsPacMan_GameVariantConfig implements GameVariantConfig {

    private static final ResourceManager RM = () -> ArcadeMsPacMan_GameVariantConfig.class;

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        mediaPlayer(PacManGameSoundID.BONUS_ACTIVE, RM.url("sound/Fruit_Bounce.mp3")),
        audioClip(PacManGameSoundID.BONUS_EATEN, RM.url("sound/Fruit.mp3")),
        audioClip(PacManGameSoundID.COIN_INSERTED, RM.url("sound/credit.wav")),
        audioClip(PacManGameSoundID.EXTRA_LIFE, RM.url("sound/ExtraLife.mp3")),
        mediaPlayer(PacManGameSoundID.GAME_OVER, RM.url("sound/game-over.mp3")),
        mediaPlayer(PacManGameSoundID.GAME_READY, RM.url("sound/Start.mp3")),
        audioClip(PacManGameSoundID.GHOST_EATEN, RM.url("sound/Ghost.mp3")),
        mediaPlayer(PacManGameSoundID.GHOST_RETURNS, RM.url("sound/GhostEyes.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_1, RM.url("sound/Act_1_They_Meet.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_2, RM.url("sound/Act_2_The_Chase.mp3")),
        mediaPlayer(PacManGameSoundID.INTERMISSION_3, RM.url("sound/Act_3_Junior.mp3")),
        audioClip(PacManGameSoundID.LEVEL_CHANGED, RM.url("sound/sweep.mp3")),
        mediaPlayer(PacManGameSoundID.LEVEL_COMPLETE, RM.url("sound/level-complete.mp3")),
        mediaPlayer(PacManGameSoundID.PAC_MAN_DEATH, RM.url("sound/Died.mp3")),
        audioClip(PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav")),
        mediaPlayer(PacManGameSoundID.PAC_MAN_POWER, RM.url("sound/ScaredGhost.mp3"))
    );

    /**
     * Used in cartridge.
     *
     * @return the game flow controller for this game variant
     */
    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private final TranslationManager translations;
    private final AssetMap assets;
    private final Factory3D factory3D;

    private GameSceneConfig gameSceneConfig;
    private ArcadeMsPacMan_RenderConfig renderConfig;

    private SoundManager sounds;
    private GameSoundEffects soundEffects;

    public ArcadeMsPacMan_GameVariantConfig() {
        translations = () -> ResourceBundle.getBundle("de.amr.pacmanfx.arcade.ms_pacman.localized_texts");
        assets = new AssetMap();
        factory3D = new ArcadeMsPacMan_Factory3D();
    }

    @Override
    public void init(GameAppContext appContext) {
        requireNonNull(appContext);

        gameSceneConfig = new ArcadeMsPacMan_GameSceneConfig(appContext);

        sounds = appContext.ui().sounds();
        loadSounds();

        assets.addAsset("app_icon",    RM.loadImage("graphics/icons/mspacman.png"));
        assets.addAsset("logo.midway", RM.loadImage("graphics/midway_logo.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);

        renderConfig = new ArcadeMsPacMan_RenderConfig(assets);
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
        return translations;
    }

    @Override
    public WorldSettings worldSettings() {
        return ArcadePacMan_GameVariantConfig.WORLD_SETTINGS;
    }

    // Private

    private void loadSounds() {
        sounds.setMediaPlayer(PacManGameSoundID.BONUS_ACTIVE, RM.url("sound/Fruit_Bounce.mp3"));
        sounds.setAudioClip(PacManGameSoundID.BONUS_EATEN, RM.url("sound/Fruit.mp3"));
        sounds.setAudioClip(PacManGameSoundID.COIN_INSERTED, RM.url("sound/credit.wav"));
        sounds.setAudioClip(PacManGameSoundID.EXTRA_LIFE, RM.url("sound/ExtraLife.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.GAME_OVER, RM.url("sound/game-over.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.GAME_READY, RM.url("sound/Start.mp3"));
        sounds.setAudioClip(PacManGameSoundID.GHOST_EATEN, RM.url("sound/Ghost.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.GHOST_RETURNS, RM.url("sound/GhostEyes.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.INTERMISSION_1, RM.url("sound/Act_1_They_Meet.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.INTERMISSION_2, RM.url("sound/Act_2_The_Chase.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.INTERMISSION_3, RM.url("sound/Act_3_Junior.mp3"));
        sounds.setAudioClip(PacManGameSoundID.LEVEL_CHANGED, RM.url("sound/sweep.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.LEVEL_COMPLETE, RM.url("sound/level-complete.mp3"));
        sounds.setMediaPlayer(PacManGameSoundID.PAC_MAN_DEATH, RM.url("sound/Died.mp3"));
        sounds.setAudioClip(PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav"));
        sounds.setMediaPlayer(PacManGameSoundID.PAC_MAN_POWER, RM.url("sound/ScaredGhost.mp3"));

        soundEffects = new GameSoundEffects(sounds);

        soundEffects.registerSirens(
            RM.url("sound/GhostNoise1.wav"),
            RM.url("sound/GhostNoise2.wav"),
            RM.url("sound/GhostNoise3.wav"),
            RM.url("sound/GhostNoise4.wav")
        );
    }

    private void unloadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.remove(entry);
        }
        soundEffects.dispose();
    }
}