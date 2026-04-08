/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.config.*;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.actor.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;

public class TengenMsPacMan_UIConfig implements UIConfig {

    // Local resources are inside resource folder subdirectories corresponding to package name of this class
    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

    // Map RGB color values to JavaFX color objects
    public static final Color[] NES_COLORS = Stream.of(NES_Palette.RGB_COLORS).map(Color::valueOf).toArray(Color[]::new);

    /**
     * @param index NES color palette index
     * @return RGB color for palette entry
     */
    public static Color nesColor(int index) {
        return NES_COLORS[index];
    }

    public static final EntityConfig TENGEN_ENTITY_CONFIG = new EntityConfig(
        new PacConfig(
            new PacComponentColors(
                nesColor(0x28), // head
                nesColor(0x2d), // palate
                nesColor(0x02)  // eyes
            ),
            new MsPacManComponentColors(
                nesColor(0x05), // hair bow
                nesColor(0x02), // hair bow pearls
                nesColor(0x28).deriveColor(0, 1.0, 0.96, 1.0) // boobs
            ),
            8.0f,
            16.0f),
        List.of(
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(nesColor(0x05), nesColor(0x20), nesColor(0x11)),
                new GhostComponentColors(nesColor(0x01), nesColor(0x20), nesColor(0x20)),
                new GhostComponentColors(nesColor(0x20), nesColor(0x20), nesColor(0x20))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(nesColor(0x25), nesColor(0x20), nesColor(0x11)),
                new GhostComponentColors(nesColor(0x01), nesColor(0x20), nesColor(0x20)),
                new GhostComponentColors(nesColor(0x20), nesColor(0x20), nesColor(0x20))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(nesColor(0x11), nesColor(0x20), nesColor(0x11)),
                new GhostComponentColors(nesColor(0x01), nesColor(0x20), nesColor(0x20)),
                new GhostComponentColors(nesColor(0x20), nesColor(0x20), nesColor(0x20))
            ),
            new GhostConfig(8.0f, 15.5f,
                new GhostComponentColors(nesColor(0x16), nesColor(0x20), nesColor(0x11)),
                new GhostComponentColors(nesColor(0x01), nesColor(0x20), nesColor(0x20)),
                new GhostComponentColors(nesColor(0x20), nesColor(0x20), nesColor(0x20))
            )
        ),
        new BonusConfig(8.0f, 14.5f),
        new EnergizerConfig3D(3, 3.5f, 6.0f, 0.2f, 1.0f),
        new FloorConfig3D(5f, 0.5f),
        new HouseConfig3D(12.0f, 0.4f, 12.0f, 2.5f),
        new LevelCounterConfig3D(10.0f, 6.0f),
        new LivesCounterConfig3D(
            5,
            Color.grayRgb(120),
            Color.grayRgb(180),
            12.0f),
        new MazeConfig3D(4.0f, 4.0f, 1.0f, 2.25f, "0x2a2a2a"),
        new PelletConfig3D(1.0f, 6.0f)
    );

    /** Defines additional Tengen-specific scene IDs */
    public enum TengenSceneID implements GameSceneConfig.SceneID { HALL_OF_FAME }

    /** Defines additional Tengen-specific dashboard IDs */
    public enum TengenMsPacMan_DashboardID implements DashboardID { JOYPAD }

    // TODO: should probably live somewhere else
    public static final Joypad JOYPAD = new Joypad(GameUI.KEYBOARD);

    // Note: Order of bonus symbols ins spritesheet is not 1:1 with order of bonus values!
    // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
    public static byte bonusValueSpriteIndex(byte bonusSymbol) {
        return switch (bonusSymbol) {
            case TengenMsPacMan_GameModel.BONUS_CHERRY -> 0;     // 100
            case TengenMsPacMan_GameModel.BONUS_STRAWBERRY -> 1; // 200
            case TengenMsPacMan_GameModel.BONUS_ORANGE -> 2;     // 500
            case TengenMsPacMan_GameModel.BONUS_PRETZEL -> 3;    // 700
            case TengenMsPacMan_GameModel.BONUS_APPLE -> 4;      // 1000
            case TengenMsPacMan_GameModel.BONUS_PEAR -> 5;       // 2000
            case TengenMsPacMan_GameModel.BONUS_BANANA -> 8;     // 6 -> 8 (5000)
            case TengenMsPacMan_GameModel.BONUS_MILK -> 6;       // 7 -> 6 (3000)
            case TengenMsPacMan_GameModel.BONUS_ICE_CREAM -> 7;  // 8 -> 7 (4000)
            case TengenMsPacMan_GameModel.BONUS_HIGH_HEELS -> 9; // 6000
            case TengenMsPacMan_GameModel.BONUS_STAR -> 10;      // 7000
            case TengenMsPacMan_GameModel.BONUS_HAND -> 11;      // 8000
            case TengenMsPacMan_GameModel.BONUS_RING -> 12;      // 9000
            case TengenMsPacMan_GameModel.BONUS_FLOWER -> 13;    // TEN!000
            default -> bonusSymbol;
        };
    }

    /** Path inside resources folder where map files (.world) are stored. */
    public static final String MAPS_FOLDER = "/de/amr/pacmanfx/tengenmspacman/maps/";

    // Relative paths under local resource folder
    public static final String REL_PATH_SPRITE_SHEET_IMAGE = "graphics/spritesheet.png";
    public static final String REL_PATH_ARCADE_MAPS_IMAGE = "graphics/arcade_mazes.png";
    public static final String REL_PATH_NON_ARCADE_MAPS_IMAGE = "graphics/non_arcade_mazes.png";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");

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
        /** Name of used NES color scheme e.g. _23_20_2B_VIOLET_WHITE_GREEN. */
        NES_COLOR_SCHEME
    }

    /** Size of NES screen in tiles (32x30). */
    public static final Vector2i NES_SCREEN_TILES = new Vector2i(32, 30);

    /** Size of NES screen in pixels (256x240). */
    public static final Vector2i NES_SCREEN_PIXELS = new Vector2i(256, 240);

    /** Aspect ratio of NES screen (32/30 = 1.066...) */
    public static final float NES_SCREEN_ASPECT_RATIO = 1.0666666666f;

    /** Shades of blue sequence used by animation. */
    private static final Color[] SHADES_OF_BLUE = {
        NES_COLORS[0x01], NES_COLORS[0x11], NES_COLORS[0x21], NES_COLORS[0x31]
    };

    /**
     * Blue color, changing from dark to brighter blue. Cycles through NES palette indices 0x01, 0x11, 0x21, 0x31 each 16 ticks.
     */
    public static Color shadeOfBlue(long tick) {
        return SHADES_OF_BLUE[(int) (tick % 64) / 16];
    }

    // end of static stuff

    private final AssetMap assets = new AssetMap();
    private final TengenMsPacMan_Factory3D factory3D = new TengenMsPacMan_Factory3D();
    private final TengenMsPacMan_GameSceneConfig gameSceneConfig = new TengenMsPacMan_GameSceneConfig();
    private GameSoundEffects soundEffects;

    public TengenMsPacMan_UIConfig() {
        Logger.info("Created Tengen UI configuration {}:", getClass().getSimpleName());
    }

    @Override
    public void init(GameUI ui) {
        loadAssets();
        initSound(ui.soundManager());
        Logger.info("Initialized Tengen UI configuration {} (loaded assets and sounds)", getClass().getSimpleName());
    }

    @Override
    public void dispose() {
        disposeAssets();
        gameSceneConfig.dispose();
        Logger.info("Disposed Tengen UI configuration {}:", getClass().getSimpleName());
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
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
    public Optional<GameSoundEffects> soundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public EntityConfig entityConfig() {
        return TENGEN_ENTITY_CONFIG;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case TengenMsPacMan_BootScene    ignored -> new TengenMsPacMan_BootScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_IntroScene   ignored -> new TengenMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_OptionsScene ignored -> new TengenMsPacMan_OptionsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_PlayScene2D  ignored -> new TengenMsPacMan_PlayScene2D_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CreditsScene ignored -> new TengenMsPacMan_CreditsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene1    ignored -> new TengenMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene2    ignored -> new TengenMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene3    ignored -> new TengenMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            case TengenMsPacMan_CutScene4    ignored -> new TengenMsPacMan_CutScene4_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        final var renderer = new TengenMsPacMan_GameLevelRenderer(canvas, this);
        renderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        return renderer;
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas) {
        return gameScene2D.adaptRenderer(new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas));
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(canvas);
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        final RectShort[] symbolSprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(symbolSprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        final byte spriteIndex = bonusValueSpriteIndex(symbol);
        final RectShort sprite = spriteSheet().sprites(SpriteID.BONUS_VALUES)[spriteIndex];
        return spriteSheet().image(sprite);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        final NES_ColorScheme scheme = worldMap.getConfigValue(MapConfigKey.NES_COLOR_SCHEME);
        final WorldMapColorScheme colorScheme = new WorldMapColorScheme(scheme.fillColorRGB(), scheme.strokeColorRGB(), scheme.strokeColorRGB(), scheme.pelletColorRGB());
        return enhanceContrast(colorScheme);
    }

    @Override
    public Ghost createGhostWithAnimations(SpriteAnimationTimer spriteAnimationTimer, byte personality) {
        final Ghost ghost = TengenMsPacMan_GameModel.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(spriteAnimationTimer, personality));
        ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationTimer spriteAnimationTimer, byte personality) {
        return new TengenMsPacMan_GhostAnimations(spriteAnimationTimer, personality);
    }

    @Override
    public TengenMsPacMan_PacAnimations createPacAnimations(SpriteAnimationTimer spriteAnimationTimer) {
        return new TengenMsPacMan_PacAnimations(spriteAnimationTimer);
    }

    // Helpers

    private void loadAssets() {
        assets.clear();
        assets.set("app_icon",                         LOCAL_RESOURCES.loadImage("graphics/icons/mspacman.png"));
        assets.set("startpage.image1",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.png"));
        assets.set("startpage.image2",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.png"));
        assets.set("color.game_over_message",          nesColor(0x11));
        assets.set("color.ready_message",              nesColor(0x28));
        assets.setLocalizedTexts(TEXT_BUNDLE);
    }

    private void initSound(SoundManager soundManager) {
        soundManager.registerAudioClipURL("audio.option.selection_changed",    LOCAL_RESOURCES.url("sound/ms-select1.wav"));
        soundManager.registerAudioClipURL("audio.option.value_changed",        LOCAL_RESOURCES.url("sound/ms-select2.wav"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,      LOCAL_RESOURCES.url("sound/fruitbounce.wav"));
        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      LOCAL_RESOURCES.url("sound/ms-fruit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       LOCAL_RESOURCES.url("sound/ms-extralife.wav"));
        soundManager.registerAudioClipURL(SoundID.GAME_OVER,        LOCAL_RESOURCES.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        LOCAL_RESOURCES.url("sound/ms-start.wav"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      LOCAL_RESOURCES.url("sound/ms-ghosteat.wav"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     LOCAL_RESOURCES.url("sound/ms-eyes.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    LOCAL_RESOURCES.url("sound/theymeet.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    LOCAL_RESOURCES.url("sound/thechase.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    LOCAL_RESOURCES.url("sound/junior.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4,    LOCAL_RESOURCES.url("sound/theend.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4 + ".junior.1", LOCAL_RESOURCES.url("sound/ms-theend1.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4 + ".junior.2", LOCAL_RESOURCES.url("sound/ms-theend2.wav"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,     LOCAL_RESOURCES.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,     LOCAL_RESOURCES.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,      LOCAL_RESOURCES.url("sound/ms-death.wav"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING,  LOCAL_RESOURCES.url("sound/ms-dot.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,      LOCAL_RESOURCES.url("sound/ms-power.wav"));

        //TODO fix the sound file instead
        final MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.registerSirens(
            LOCAL_RESOURCES.url("sound/ms-siren1.wav"),
            LOCAL_RESOURCES.url("sound/ms-siren2.wav"), // TODO
            LOCAL_RESOURCES.url("sound/ms-siren2.wav"), // TODO
            LOCAL_RESOURCES.url("sound/ms-siren2.wav")  // TODO
        );
        soundEffects.setSirenVolume(1.0f);
    }
}