/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.config.TengenJsonConfigLoader;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_GameSceneConfig;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.media.MediaPlayer;
import org.tinylog.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameVariantConfig implements GameVariantConfig {

    public static GameFlowController createGameFlow() {
        final var gameFlow = new GameFlowController("Tengen Ms. Pac-Man Game Flow");
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            gameFlow.addState(gameState.state());
        }
        return gameFlow;
    }

    // Local resources are stored inside main resource folder subdirectories named after package name of this class
    private static final ResourceManager RM = TengenMsPacMan_ResourceManager.instance();

    private static final List<SoundManager.SoundEntry> SOUND_ENTRIES = Arrays.asList(
        mediaPlayer  (PacManGameSoundID.BONUS_ACTIVE,                RM.url("sound/fruitbounce.wav")),
        audioClip    (PacManGameSoundID.BONUS_EATEN,                 RM.url("sound/ms-fruit.wav")),
        audioClip    (PacManGameSoundID.EXTRA_LIFE,                  RM.url("sound/ms-extralife.wav")),
        audioClip    (PacManGameSoundID.GAME_OVER,                   RM.url("sound/common/game-over.mp3")),
        mediaPlayer  (PacManGameSoundID.GAME_READY,                  RM.url("sound/ms-start.wav")),
        audioClip    (PacManGameSoundID.GHOST_EATEN,                 RM.url("sound/ms-ghosteat.wav")),
        mediaPlayer  (PacManGameSoundID.GHOST_RETURNS,               RM.url("sound/ms-eyes.wav")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_1,              RM.url("sound/theymeet.wav")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_2,              RM.url("sound/thechase.wav")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_3,              RM.url("sound/junior.wav")),
        mediaPlayer  (PacManGameSoundID.INTERMISSION_4,              RM.url("sound/theend.wav")),
        audioClip    (PacManGameSoundID.LEVEL_CHANGED,               RM.url("sound/common/sweep.mp3")),
        mediaPlayer  (PacManGameSoundID.LEVEL_COMPLETE,              RM.url("sound/common/level-complete.mp3")),
        mediaPlayer  (PacManGameSoundID.PAC_MAN_DEATH,               RM.url("sound/ms-death.wav")),
        audioClip    (PacManGameSoundID.PAC_MAN_MUNCHING,            RM.url("sound/ms-dot.wav")),
        mediaPlayer  (PacManGameSoundID.PAC_MAN_POWER,               RM.url("sound/ms-power.wav")),

        mediaPlayer  (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1, RM.url("sound/ms-theend1.wav")),
        mediaPlayer  (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2, RM.url("sound/ms-theend2.wav")),
        audioClip    (TengenMsPacManSoundID.OPTION_SELECTION_CHANGE, RM.url("sound/ms-select1.wav")),
        audioClip    (TengenMsPacManSoundID.OPTION_VALUE_CHANGE,     RM.url("sound/ms-select2.wav"))
    );

    private static final WorldSettings WORLD_SETTINGS = TengenJsonConfigLoader.load(
        TengenMsPacMan_GameVariantConfig.class.getResource("/de/amr/pacmanfx/tengenmspacman/world.json"), WorldSettings.class);

    /** Path inside resources folder where map files (.world) are stored. */
    public static final String MAPS_FOLDER = "/de/amr/pacmanfx/tengenmspacman/maps/";

    // Relative paths under local resource folder
    public static final String REL_PATH_SPRITE_SHEET_IMAGE = "graphics/spritesheet.png";
    public static final String REL_PATH_ARCADE_MAPS_IMAGE = "graphics/arcade_mazes.png";
    public static final String REL_PATH_NON_ARCADE_MAPS_IMAGE = "graphics/non_arcade_mazes.png";

    /** Additional property keys used inside world map files. Values are set at runtime by the map selector. */
    public enum MapConfigKey {
        /** Map category. One of ARCADE, MINI, BIG, STRANGE. */
        MAP_CATEGORY,
        /** ID of correctly recolored maze sprite set */
        MAP_ID,
        /** The map image set (normal + flash images) used by the map renderer. */
        MAP_IMAGE_SET,
        /** Boolean value defining if multiple (random) flash colors are used. */
        MULTIPLE_FLASH_COLORS,
    }

    /** Size of NES screen in tiles (32x30). */
    public static final Vector2i NES_SCREEN_TILES = new Vector2i(32, 30);

    public static final int NES_SCREEN_WIDTH  = 256;
    public static final int NES_SCREEN_HEIGHT = 240;

    /** Aspect ratio of NES screen (32/30 = 1.066...) */
    public static final float NES_SCREEN_ASPECT_RATIO = 1.0666666666f;

    // Non-static members

    private final ResourceBundle textBundle;
    private final AssetMap assets;
    private final TengenMsPacMan_Factory3D factory3D;

    private TengenMsPacMan_GameSceneConfig gameSceneConfig;
    private TengenMsPacMan_RenderConfig renderConfig;

    private SoundManager sounds;
    private GameSoundEffects soundEffects;

    public TengenMsPacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");
        assets = new AssetMap();
        factory3D = new TengenMsPacMan_Factory3D();
    }

    @Override
    public void init(GameAppContext appContext) {
        requireNonNull(appContext);
        
        gameSceneConfig = new TengenMsPacMan_GameSceneConfig(appContext);

        sounds = appContext.ui().sounds();
        loadSounds();
        
        assets.addAsset("app_icon",                         RM.loadImage("graphics/icons/mspacman.png"));
        assets.addAsset("startpage.image1",                 RM.loadImage("graphics/flyer-page-1.png"));
        assets.addAsset("startpage.image2",                 RM.loadImage("graphics/flyer-page-2.png"));
        assets.addAsset("color.game_over_message",          NES_Palette.color(0x11));
        assets.addAsset("color.ready_message",              NES_Palette.color(0x28));

        renderConfig = new TengenMsPacMan_RenderConfig(assets);
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
    public TengenMsPacMan_Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public TengenMsPacMan_GameSceneConfig gameSceneConfig() {
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
        return WORLD_SETTINGS;
    }

    // Helpers

    private void loadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.add(entry);
        }

        //TODO fix the sound file instead
        final MediaPlayer bounceSound = sounds.mediaPlayer(PacManGameSoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }

        soundEffects = new GameSoundEffects(sounds);
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.registerSirens(
            RM.url("sound/ms-siren1.wav"),
            RM.url("sound/ms-siren2.wav"), // TODO
            RM.url("sound/ms-siren2.wav"), // TODO
            RM.url("sound/ms-siren2.wav")  // TODO
        );
        soundEffects.setSirenVolume(1.0f);
    }

    private void unloadSounds() {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            sounds.remove(entry);
        }
        soundEffects.dispose();
    }
}