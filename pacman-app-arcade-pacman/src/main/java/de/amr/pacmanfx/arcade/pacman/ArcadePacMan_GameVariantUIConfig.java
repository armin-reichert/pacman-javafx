/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.game.GameVariantUIConfig;
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

import java.util.*;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tile;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;
import static java.util.Objects.requireNonNull;

/**
 * The Arcade Pac‑Man game variant.
 */
public class ArcadePacMan_GameVariantUIConfig implements GameVariantUIConfig {

    /**
     * Top-left tile of ghost house in original Arcade maps (Pac-Man, Ms. Pac-Man).
     */
    public static final Vector2i ARCADE_MAP_HOUSE_MIN_TILE = tile(10, 15);

    public static final Vector2i DEFAULT_BONUS_TILE = new Vector2i(13, 20);

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Arcade Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    private final static ResourceManager RM = () -> ArcadePacMan_GameVariantUIConfig.class;

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        audioClip   (PacManGameSoundID.BONUS_EATEN,      RM.url("sound/eat_fruit.mp3")),
        audioClip   (PacManGameSoundID.COIN_INSERTED,    RM.url("sound/credit.wav")),
        audioClip   (PacManGameSoundID.EXTRA_LIFE,       RM.url("sound/extend.mp3")),
        audioClip   (PacManGameSoundID.GAME_OVER,        RM.url("sound/common/game-over.mp3")),
        mediaPlayer (PacManGameSoundID.GAME_READY,       RM.url("sound/game_start.mp3")),
        audioClip   (PacManGameSoundID.GHOST_EATEN,      RM.url("sound/eat_ghost.mp3")),
        mediaPlayer (PacManGameSoundID.GHOST_RETURNS,    RM.url("sound/retreating.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_1,   RM.url("sound/intermission.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_2,   RM.url("sound/intermission.mp3")),
        mediaPlayer (PacManGameSoundID.INTERMISSION_3,   RM.url("sound/intermission.mp3")),
        audioClip   (PacManGameSoundID.LEVEL_CHANGED,    RM.url("sound/common/sweep.mp3")),
        mediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,   RM.url("sound/common/level-complete.mp3")),
        mediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,    RM.url("sound/pacman_death.wav")),
        audioClip   (PacManGameSoundID.PAC_MAN_MUNCHING, RM.url("sound/munch.wav")),
        mediaPlayer (PacManGameSoundID.PAC_MAN_POWER,    RM.url("sound/ghost-turn-to-blue.mp3"))
    );

    private final TranslationManager translations = () -> ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman.localized_texts");
    private final Factory3D factory3D = new ArcadePacMan_Factory3D();
    private final GameSceneConfig gameSceneConfig = new ArcadePacMan_GameSceneConfig();

    private ArcadePacMan_RenderConfig renderConfig;
    private AssetMap assets;
    private GameSoundEffects soundEffects;

    private final Map<Named, Object> extensions = new HashMap<>();

    @Override
    public void init() {
        loadAssets();
        renderConfig = new ArcadePacMan_RenderConfig(assets);
        renderConfig.addAssets();
        assets.freeze();
        extensions.put(Arcade_GameExtensions.ACTIONS, new Arcade_Actions());
    }

    @Override
    public void loadSounds(SoundManager soundManager) {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            soundManager.add(entry);
        }
        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 9);
        soundEffects.registerSirens(
            RM.url("sound/siren_1.mp3"),
            RM.url("sound/siren_2.mp3"),
            RM.url("sound/siren_3.mp3"),
            RM.url("sound/siren_4.mp3")
        );
        soundEffects.setSirenVolume(0.33f);
    }

    @Override
    public void unloadSounds(SoundManager soundManager) {
        Logger.info("Unload sounds");
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            soundManager.remove(entry);
        }
        if (soundEffects != null) {
            soundEffects.dispose();
            soundEffects = null;
        }
    }

    @Override
    public void connectApp(GameAppContext app) {
        requireNonNull(app);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose game variant configuration {}:", getClass().getSimpleName());

        Logger.info("Dispose game scene configuration");
        gameSceneConfig.dispose();

        if (assets != null) {
            Logger.info("Dispose assets");
            assets.dispose();
            assets = null;
        }
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public <T> T extensionValue(Named id, Class<T> type) {
        final Object value = extensions.get(id);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("Extension value " + value + " of type " + type.getName() + " not found");
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
        return WorldSettings.DEFAULT_SETTINGS;
    }

    // private

    private void loadAssets() {
        assets = new AssetMap();
        assets.addAsset("app_icon", RM.loadImage("graphics/icons/pacman.png"));
        assets.addAsset("color.game_over_message", ARCADE_RED);
    }
}