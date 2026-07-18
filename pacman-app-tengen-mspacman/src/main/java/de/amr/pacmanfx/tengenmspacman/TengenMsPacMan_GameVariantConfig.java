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
import de.amr.pacmanfx.tengenmspacman.gamescene.GameSceneConfig;
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
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

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

    private static final WorldSettings WORLD_CONFIG = TengenJsonConfigLoader.load(
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
    private final AssetMap assets = new AssetMap();
    private final TengenMsPacMan_Factory3D factory3D = new TengenMsPacMan_Factory3D();
    private GameSceneConfig gameSceneConfig;
    private TengenMsPacMan_RenderConfig renderConfig;
    private GameSoundEffects soundEffects;

    public TengenMsPacMan_GameVariantConfig() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");
        Logger.info("Created Tengen UI configuration {}:", getClass().getSimpleName());
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public void init(GameAppContext appContext) {
        loadAssets();
        registerSoundObjects(appContext.ui().sounds());
        gameSceneConfig = new GameSceneConfig(appContext);
        renderConfig = new TengenMsPacMan_RenderConfig(assets);
        Logger.info("Initialized Tengen UI configuration {} (loaded assets and sounds)", getClass().getSimpleName());
    }

    @Override
    public void dispose() {
        assets().dispose();
        gameSceneConfig.dispose();
        Logger.info("Disposed Tengen UI configuration {}:", getClass().getSimpleName());
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
    public TengenMsPacMan_Factory3D factory3D() {
        return factory3D;
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public WorldSettings worldSettings() {
        return WORLD_CONFIG;
    }

    // Helpers

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon",                         RM.loadImage("graphics/icons/mspacman.png"));
        assets.register("startpage.image1",                 RM.loadImage("graphics/flyer-page-1.png"));
        assets.register("startpage.image2",                 RM.loadImage("graphics/flyer-page-2.png"));
        assets.register("color.game_over_message",          NES_Palette.color(0x11));
        assets.register("color.ready_message",              NES_Palette.color(0x28));
    }

    private void registerSoundObjects(SoundManager soundManager) {
        soundManager.setMediaPlayer (PacManGameSoundID.BONUS_ACTIVE,                RM.url("sound/fruitbounce.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.BONUS_EATEN,                 RM.url("sound/ms-fruit.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.EXTRA_LIFE,                  RM.url("sound/ms-extralife.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.GAME_OVER,                   RM.url("sound/common/game-over.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_READY,                  RM.url("sound/ms-start.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.GHOST_EATEN,                 RM.url("sound/ms-ghosteat.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.GHOST_RETURNS,               RM.url("sound/ms-eyes.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_1,              RM.url("sound/theymeet.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_2,              RM.url("sound/thechase.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_3,              RM.url("sound/junior.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_4,              RM.url("sound/theend.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.LEVEL_CHANGED,               RM.url("sound/common/sweep.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,              RM.url("sound/common/level-complete.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,               RM.url("sound/ms-death.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.PAC_MAN_MUNCHING,            RM.url("sound/ms-dot.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_POWER,               RM.url("sound/ms-power.wav"));

        soundManager.setMediaPlayer (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1, RM.url("sound/ms-theend1.wav"));
        soundManager.setMediaPlayer (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2, RM.url("sound/ms-theend2.wav"));
        soundManager.setAudioClip   (TengenMsPacManSoundID.OPTION_SELECTION_CHANGE, RM.url("sound/ms-select1.wav"));
        soundManager.setAudioClip   (TengenMsPacManSoundID.OPTION_VALUE_CHANGE,     RM.url("sound/ms-select2.wav"));

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
}