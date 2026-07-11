/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.config;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.MapColorScheme;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManSoundID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Factory3D;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ResourceManager;
import de.amr.pacmanfx.tengenmspacman.gamescene.*;
import de.amr.pacmanfx.tengenmspacman.model.BonusSymbol;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.config.world.WorldSettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class TengenMsPacManGameVariant implements GameVariantConfig {

    // Local resources are stored inside main resource folder subdirectories named after package name of this class
    private static final ResourceManager LOCAL_RESOURCES = TengenMsPacMan_ResourceManager.instance();

    private static final WorldSettings WORLD_CONFIG = TengenSettingsLoader.load(
        TengenMsPacManGameVariant.class.getResource("/de/amr/pacmanfx/tengenmspacman/world.json"), WorldSettings.class);

    // Note: Order of bonus symbols in spritesheet is not 1:1 with order of bonus values!
    // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
    private static final Map<BonusSymbol, Integer> BONUS_VALUE_SPRITE_INDEX = new EnumMap<>(BonusSymbol.class);
    static {
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.CHERRY,      0); // "100"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.STRAWBERRY,  1); // "200"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.ORANGE,      2); // "500"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.PRETZEL,     3); // "700"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.APPLE,       4); // "1000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.PEAR,        5); // "2000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.BANANA,      8); // 6 -> 8 ("5000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.MILK,        6); // 7 -> 6 ("3000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.ICE_CREAM,   7); // 8 -> 7 ("4000")
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.HIGH_HEELS,  9); // "6000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.STAR,       10); // "7000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.HAND,       11); // "8000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.RING,       12); // "9000"
        BONUS_VALUE_SPRITE_INDEX.put(BonusSymbol.FLOWER,     13); // "TEN!000"

    }

    public static int bonusValueSpriteIndex(int bonusSymbolCode) {
        if (bonusSymbolCode < 0 || bonusSymbolCode >= BonusSymbol.values().length) {
            throw new IllegalArgumentException("Illegal bonus symbol code: " + bonusSymbolCode);
        }
        final BonusSymbol symbol = BonusSymbol.values()[bonusSymbolCode];
        return BONUS_VALUE_SPRITE_INDEX.getOrDefault(symbol, bonusSymbolCode);
    }

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

    /** Shades of blue sequence used by animation. */
    private static final Color[] SHADES_OF_BLUE = {
        NES_Palette.color(0x01), NES_Palette.color(0x11), NES_Palette.color(0x21), NES_Palette.color(0x31)
    };

    /**
     * Blue color, changing from dark to brighter blue. Cycles through NES palette indices 0x01, 0x11, 0x21, 0x31 each 16 ticks.
     */
    public static Color shadeOfBlue(long tick) {
        return SHADES_OF_BLUE[(int) (tick % 64) / 16];
    }

    // Non-static members

    private final ResourceBundle textBundle;
    private final AssetMap assets = new AssetMap();
    private final TengenMsPacMan_Factory3D factory3D = new TengenMsPacMan_Factory3D();
    private GameSceneConfig gameSceneConfig;
    private GameSoundEffects soundEffects;

    public TengenMsPacManGameVariant() {
        textBundle = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");
        Logger.info("Created Tengen UI configuration {}:", getClass().getSimpleName());
    }

    @Override
    public TranslationManager translations() {
        return () -> textBundle;
    }

    @Override
    public void init(Game game) {
        loadAssets();
        registerSoundObjects(game.ui().sounds());
        gameSceneConfig = new GameSceneConfig(game);
        Logger.info("Initialized Tengen UI configuration {} (loaded assets and sounds)", getClass().getSimpleName());
    }

    @Override
    public void dispose() {
        assets().dispose();
        gameSceneConfig.dispose();
        Logger.info("Disposed Tengen UI configuration {}:", getClass().getSimpleName());
    }

    @Override
    public de.amr.pacmanfx.ui.gamescene.common.GameSceneConfig gameSceneConfig() {
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
    public Optional<GameSoundEffects> optSoundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public WorldSettings worldSettings() {
        return WORLD_CONFIG;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
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
        return gameScene2D.configureRenderer(renderer);
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new TengenMsPacMan_GameLevelRenderer(canvas, this);
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay_Renderer createHUDRenderer(AbstractGameScene2D gameScene2D, Canvas canvas) {
        return gameScene2D.configureRenderer(new TengenMsPacMan_HeadsUpDisplay_Renderer(canvas));
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
    public Image bonusSymbolImage(int symbolCode) {
        final RectShort[] symbolSprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(symbolSprites[symbolCode]);
    }

    @Override
    public Image bonusValueImage(int symbolCode) {
        final int spriteIndex = bonusValueSpriteIndex(symbolCode);
        final RectShort sprite = spriteSheet().sprites(SpriteID.BONUS_VALUES)[spriteIndex];
        return spriteSheet().image(sprite);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        final MapColorScheme scheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final WorldMapColorScheme colorScheme = new WorldMapColorScheme(
            scheme.wallFill(), scheme.wallStroke(), scheme.door(), scheme.pellet());
        return enhanceContrast(colorScheme);
    }

    @Override
    public Ghost createAnimatedGhost(SpriteAnimationContainer animationSet, byte personality) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(animationSet, personality));
        ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimations createGhostAnimations(SpriteAnimationContainer animationSet, byte personality) {
        return new TengenMsPacMan_GhostAnimations(animationSet, personality);
    }

    @Override
    public TengenMsPacMan_PacAnimations createPacAnimations(SpriteAnimationContainer animationSet) {
        return new TengenMsPacMan_PacAnimations(animationSet);
    }

    // Helpers

    private void loadAssets() {
        assets.clear();
        assets.register("app_icon",                         LOCAL_RESOURCES.loadImage("graphics/icons/mspacman.png"));
        assets.register("startpage.image1",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.png"));
        assets.register("startpage.image2",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.png"));
        assets.register("color.game_over_message",          NES_Palette.color(0x11));
        assets.register("color.ready_message",              NES_Palette.color(0x28));
    }

    private void registerSoundObjects(SoundManager soundManager) {
        soundManager.setMediaPlayer (PacManGameSoundID.BONUS_ACTIVE,                LOCAL_RESOURCES.url("sound/fruitbounce.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.BONUS_EATEN,                 LOCAL_RESOURCES.url("sound/ms-fruit.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.EXTRA_LIFE,                  LOCAL_RESOURCES.url("sound/ms-extralife.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.GAME_OVER,                   LOCAL_RESOURCES.url("sound/common/game-over.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.GAME_READY,                  LOCAL_RESOURCES.url("sound/ms-start.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.GHOST_EATEN,                 LOCAL_RESOURCES.url("sound/ms-ghosteat.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.GHOST_RETURNS,               LOCAL_RESOURCES.url("sound/ms-eyes.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_1,              LOCAL_RESOURCES.url("sound/theymeet.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_2,              LOCAL_RESOURCES.url("sound/thechase.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_3,              LOCAL_RESOURCES.url("sound/junior.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.INTERMISSION_4,              LOCAL_RESOURCES.url("sound/theend.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.LEVEL_CHANGED,               LOCAL_RESOURCES.url("sound/common/sweep.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.LEVEL_COMPLETE,              LOCAL_RESOURCES.url("sound/common/level-complete.mp3"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_DEATH,               LOCAL_RESOURCES.url("sound/ms-death.wav"));
        soundManager.setAudioClip   (PacManGameSoundID.PAC_MAN_MUNCHING,            LOCAL_RESOURCES.url("sound/ms-dot.wav"));
        soundManager.setMediaPlayer (PacManGameSoundID.PAC_MAN_POWER,               LOCAL_RESOURCES.url("sound/ms-power.wav"));

        soundManager.setMediaPlayer (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1, LOCAL_RESOURCES.url("sound/ms-theend1.wav"));
        soundManager.setMediaPlayer (TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2, LOCAL_RESOURCES.url("sound/ms-theend2.wav"));
        soundManager.setAudioClip   (TengenMsPacManSoundID.OPTION_SELECTION_CHANGE, LOCAL_RESOURCES.url("sound/ms-select1.wav"));
        soundManager.setAudioClip   (TengenMsPacManSoundID.OPTION_VALUE_CHANGE,     LOCAL_RESOURCES.url("sound/ms-select2.wav"));

        //TODO fix the sound file instead
        final MediaPlayer bounceSound = soundManager.mediaPlayer(PacManGameSoundID.BONUS_ACTIVE);
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