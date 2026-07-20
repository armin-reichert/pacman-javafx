/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.d3.Factory3D;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.JsonConfigLoader;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.AudioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.MediaPlayer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

/**
 * The Arcade Pac‑Man game variant.
 */
public class ArcadePacMan_GameVariantConfig implements GameVariantConfig {

    private final static ResourceManager RM = () -> ArcadePacMan_GameVariantConfig.class;

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        AudioClip    (PacManGameSoundID.BONUS_EATEN,      RM.url("sound/eat_fruit.mp3")),
        AudioClip    (PacManGameSoundID.COIN_INSERTED,    RM.url("sound/credit.wav")),
        AudioClip    (PacManGameSoundID.EXTRA_LIFE,       RM.url("sound/extend.mp3")),
        AudioClip    (PacManGameSoundID.GAME_OVER,        RM.url("sound/common/game-over.mp3")),
        MediaPlayer  (PacManGameSoundID.GAME_READY,       RM.url("sound/game_start.mp3")),
        AudioClip    (PacManGameSoundID.GHOST_EATEN,      RM.url("sound/eat_ghost.mp3")),
        MediaPlayer  (PacManGameSoundID.GHOST_RETURNS,    RM.url("sound/retreating.mp3")),
        MediaPlayer  (PacManGameSoundID.INTERMISSION_1,   RM.url("sound/intermission.mp3")),
        MediaPlayer  (PacManGameSoundID.INTERMISSION_2,   RM.url("sound/intermission.mp3")),
        MediaPlayer  (PacManGameSoundID.INTERMISSION_3,   RM.url("sound/intermission.mp3")),
        AudioClip    (PacManGameSoundID.LEVEL_CHANGED,    RM.url("sound/common/sweep.mp3")),
        MediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,   RM.url("sound/common/level-complete.mp3")),
        MediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,    RM.url("sound/pacman_death.wav")),
        AudioClip    (PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav")),
        MediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,    RM.url("sound/ghost-turn-to-blue.mp3"))
    );

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    public static final WorldSettings WORLD_SETTINGS = JsonConfigLoader.load(
        GameUI.class.getResource("/de/amr/pacmanfx/ui/world.json"), WorldSettings.class);

    private final TranslationManager translations;
    private final AssetMap assets;
    private final Factory3D factory3D;

    private ArcadePacMan_RenderConfig renderConfig;
    private GameSceneConfig gameSceneConfig;

    private SoundManager sounds;
    private GameSoundEffects soundEffects;

    public ArcadePacMan_GameVariantConfig() {
        translations = () -> ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts");
        assets = new AssetMap();
        factory3D = new ArcadePacMan_Factory3D();
    }

    // GameVariant interface

    @Override
    public void init(GameAppContext appContext) {
        requireNonNull(appContext);

        gameSceneConfig = new ArcadePacMan_GameSceneConfig(appContext);

        sounds = appContext.ui().sounds();
        loadSounds();

        assets.addAsset("app_icon", RM.loadImage("graphics/icons/pacman.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);

        renderConfig = new ArcadePacMan_RenderConfig(assets);
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
        return WORLD_SETTINGS;
    }

    // private

    private void loadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.add(entry);
        }
        soundEffects = new GameSoundEffects(sounds);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            RM.url("sound/siren_1.mp3"),
            RM.url("sound/siren_2.mp3"),
            RM.url("sound/siren_3.mp3"),
            RM.url("sound/siren_4.mp3")
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