/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.tengenmspacman.config.TengenJsonConfigLoader;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_GameSceneConfig;
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

import java.util.*;

import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.audioClip;
import static de.amr.pacmanfx.ui.sound.SoundManager.SoundEntry.mediaPlayer;

public class TengenMsPacMan_UIConfig implements GameVariantUIConfig {

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";
    public static final String READY_MESSAGE_TEXT = "READY!";

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
        TengenMsPacMan_UIConfig.class.getResource("/de/amr/pacmanfx/tengenmspacman/world.json"), WorldSettings.class);

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

    private final ResourceBundle textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");
    private final TengenMsPacMan_Factory3D factory3D = new TengenMsPacMan_Factory3D();
    private final TengenMsPacMan_GameSceneConfig gameSceneConfig = new TengenMsPacMan_GameSceneConfig();

    private TengenMsPacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;
    private AssetMap assets;

    private final Map<Named, Object> extensions = new HashMap<>();

    public void init() {
        loadAssets();
        renderConfig = new TengenMsPacMan_RenderConfig(assets);
        renderConfig.addAssets();
        assets.freeze();
    }

    @Override
    public void loadSounds(SoundManager soundManager) {
        for (SoundManager.SoundEntry entry : SOUND_ENTRIES) {
            soundManager.add(entry);
        }

        //TODO fix the sound file instead
        final MediaPlayer bounceSound = soundManager.mediaPlayer(PacManGameSoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.registerSirens(
            RM.url("sound/ms-siren1.wav"),
            RM.url("sound/ms-siren2.wav"), // TODO
            RM.url("sound/ms-siren2.wav"), // TODO
            RM.url("sound/ms-siren2.wav")  // TODO
        );
        soundEffects.setSirenVolume(1.0f);
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
        //TODO get rid of this crap and of app dependency!
        extensions.put(TengenMsPacMan_GameExtension.UI_SETTINGS, new TengenMsPacMan_UISettings(null));
        extensions.put(TengenMsPacMan_GameExtension.ACTIONS, new TengenMsPacMan_Actions(
            app.input().joypad(), app.commonActions())
        );
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

    private void loadAssets()  {
        assets = new AssetMap();
        assets.addAsset("app_icon",                RM.loadImage("graphics/icons/mspacman.png"));
        assets.addAsset("startpage.image1",        RM.loadImage("graphics/flyer-page-1.png"));
        assets.addAsset("startpage.image2",        RM.loadImage("graphics/flyer-page-2.png"));
    }

}