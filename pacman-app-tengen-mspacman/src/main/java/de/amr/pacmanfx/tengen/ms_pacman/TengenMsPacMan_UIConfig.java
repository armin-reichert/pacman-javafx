/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.teststates.CutScenesTestState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.*;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.controller.GamePlayState.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.exchangeNES_ColorScheme;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUI_Config {

    public static final String MAPS_PATH = "/de/amr/pacmanfx/tengen/ms_pacman/maps/";

    private static final String ASSET_NAMESPACE = "tengen";

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_TENGEN  = () -> TengenMsPacMan_UIConfig.class;

    /** 32x30 */
    public static final Vector2i NES_TILES = new Vector2i(32, 30);

    /** 256x240 */
    public static final Vector2f NES_SIZE_PX = NES_TILES.scaled(TS).toVector2f();

    /** 32/30 */
    public static final float NES_ASPECT = NES_SIZE_PX.x() / NES_SIZE_PX.y();

    /**
     * @param index NES color palette index
     * @return RGB color for palette entry
     */
    public static Color nesPaletteColor(int index) {
        return Color.web(NES_Palette.color(index));
    }

    public static Color blueShadedColor(long tick) {
        // Blue color, changing from dark blue to brighter blue.
        // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each 16 ticks.
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + i * 0x10);
    }

    private final GameUI ui;

    private DefaultSoundManager soundManager = new DefaultSoundManager();
    private Map<String, GameScene> scenesByID = new HashMap<>();
    private Set<ActionBinding> tengenMsPacManBindings;

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private ArcadeMapsSpriteSheet arcadeMapsSpriteSheet;
    private NonArcadeMapsSpriteSheet nonArcadeMapsSpriteSheet;
    private ColoredMazeSpriteSet recoloredMaze;
    private Map<CacheKey, RecoloredSpriteImage> recoloredMazeImageCache = new WeakHashMap<>();


    public TengenMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new TengenMsPacMan_SpriteSheet(RES_TENGEN.loadImage("graphics/spritesheet.png"));
        arcadeMapsSpriteSheet = new ArcadeMapsSpriteSheet(RES_TENGEN.loadImage("graphics/arcade_mazes.png"));
        nonArcadeMapsSpriteSheet = new NonArcadeMapsSpriteSheet(RES_TENGEN.loadImage("graphics/non_arcade_mazes.png"));

        Joypad jp = ui.joypad();
        tengenMsPacManBindings = Set.of(
            new ActionBinding(ACTION_STEER_UP,            jp.key(JoypadButton.UP),    control(KeyCode.UP)),
            new ActionBinding(ACTION_STEER_DOWN,          jp.key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
            new ActionBinding(ACTION_STEER_LEFT,          jp.key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
            new ActionBinding(ACTION_STEER_RIGHT,         jp.key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
            new ActionBinding(ACTION_QUIT_DEMO_LEVEL,     jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_ENTER_START_SCREEN,  jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_START_PLAYING,       jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,  jp.key(JoypadButton.A), jp.key(JoypadButton.B)),
            new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
            new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, nude(KeyCode.SPACE))
        );
    }

    public Set<ActionBinding> tengenMsPacManBindings() {
        return tengenMsPacManBindings;
    }

    public ColoredMazeSpriteSet recoloredMazeSprites() {
        return recoloredMaze;
    }

    public void createMazeSpriteSet(GameLevel gameLevel) {
        recoloredMaze = createMazeSpriteSet(gameLevel.worldMap(), gameLevel.data().numFlashes());
        Logger.info("Created recolored maze sprites for game level #{} ({} flash colors: {})",
            gameLevel.number(),
            gameLevel.data().numFlashes(),
            recoloredMaze.mazeSprite());
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    @Override
    public void storeAssets(AssetStorage assets) {
        storeLocalAssetValue(assets, "spritesheet", spriteSheet);
        storeLocalAssetValue(assets, "app_icon",         RES_TENGEN.loadImage("graphics/icons/mspacman.png"));
        storeLocalAssetValue(assets, "startpage.image1", RES_TENGEN.loadImage("graphics/f1.png"));
        storeLocalAssetValue(assets, "startpage.image2", RES_TENGEN.loadImage("graphics/f2.png"));
        storeLocalAssetValue(assets, "color.game_over_message", nesPaletteColor(0x11));
        storeLocalAssetValue(assets, "color.ready_message",     nesPaletteColor(0x28));

        RectShort[] symbolSprites = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSequence(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 13; ++symbol) {
            storeLocalAssetValue(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeLocalAssetValue(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeLocalAssetValue(assets, "pac.color.head",                   nesPaletteColor(0x28));
        storeLocalAssetValue(assets, "pac.color.eyes",                   nesPaletteColor(0x02));
        storeLocalAssetValue(assets, "pac.color.palate",                 nesPaletteColor(0x2d));
        storeLocalAssetValue(assets, "pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        storeLocalAssetValue(assets, "pac.color.hairbow",                nesPaletteColor(0x05));
        storeLocalAssetValue(assets, "pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        RectShort[] numberSprites = spriteSheet.spriteSequence(SpriteID.GHOST_NUMBERS);
        storeLocalAssetValue(assets, "ghost_points_0",                   spriteSheet.image(numberSprites[0]));
        storeLocalAssetValue(assets, "ghost_points_1",                   spriteSheet.image(numberSprites[1]));
        storeLocalAssetValue(assets, "ghost_points_2",                   spriteSheet.image(numberSprites[2]));
        storeLocalAssetValue(assets, "ghost_points_3",                   spriteSheet.image(numberSprites[3]));

        storeLocalAssetValue(assets, "ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        storeLocalAssetValue(assets, "ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        storeLocalAssetValue(assets, "ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        storeLocalAssetValue(assets, "ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        storeLocalAssetValue(assets, "ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        storeLocalAssetValue(assets, "ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        storeLocalAssetValue(assets, "ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        storeLocalAssetValue(assets, "ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        storeLocalAssetValue(assets, "ghost.color.frightened.dress",     nesPaletteColor(0x01));
        storeLocalAssetValue(assets, "ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO sprite sheet provides two flashing colors, when to use which?
        storeLocalAssetValue(assets, "ghost.color.flashing.dress",       nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        storeLocalAssetValue(assets, "ghost.color.flashing.pupils",      nesPaletteColor(0x20));

        soundManager.registerAudioClip("audio.option.selection_changed",  RES_TENGEN.url("sound/ms-select1.wav"));
        soundManager.registerAudioClip("audio.option.value_changed",      RES_TENGEN.url("sound/ms-select2.wav"));

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,           RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,            RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,            RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,             RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,                 RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,            RES_TENGEN.url("sound/fruitbounce.wav"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,               RES_TENGEN.url("sound/ms-fruit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,                RES_TENGEN.url("sound/ms-extralife.wav"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,               RES_TENGEN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,              RES_TENGEN.url("sound/ms-start.wav"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,               RES_TENGEN.url("sound/ms-ghosteat.wav"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,           RES_TENGEN.url("sound/ms-eyes.wav"));
        soundManager.registerMediaPlayer("audio.intermission.1",          RES_TENGEN.url("sound/theymeet.wav"));
        soundManager.registerMediaPlayer("audio.intermission.2",          RES_TENGEN.url("sound/thechase.wav"));
        soundManager.registerMediaPlayer("audio.intermission.3",          RES_TENGEN.url("sound/junior.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4",          RES_TENGEN.url("sound/theend.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4.junior.1", RES_TENGEN.url("sound/ms-theend1.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4.junior.2", RES_TENGEN.url("sound/ms-theend2.wav"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,             RES_TENGEN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,          RES_TENGEN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,           RES_TENGEN.url("sound/ms-death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,        RES_TENGEN.url("sound/ms-dot.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,           RES_TENGEN.url("sound/ms-power.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,                 RES_TENGEN.url("sound/ms-siren1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO

        //TODO fix this in the sound file
        MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }
    }

    @Override
    public void dispose() {
        //TODO this method is not yet called!
        ui.assets().removeAll(ASSET_NAMESPACE + ".");
        if (arcadeMapsSpriteSheet != null) {
            //arcadeMapsSpriteSheet.dispose();
            arcadeMapsSpriteSheet = null;
        }
        if (nonArcadeMapsSpriteSheet != null) {
            //nonArcadeMapsSpriteSheet.dispose();
            nonArcadeMapsSpriteSheet = null;
        }
        if (recoloredMazeImageCache != null) {
            recoloredMazeImageCache.clear();
            recoloredMazeImageCache = null;
        }
        if (recoloredMaze != null) {
            recoloredMaze.dispose();
            recoloredMaze = null;
        }
        if (spriteSheet != null) {
            spriteSheet = null;
        }
        if (scenesByID != null) {
            scenesByID.clear();
            scenesByID = null;
        }
        if (tengenMsPacManBindings != null) {
            tengenMsPacManBindings.clear();
            tengenMsPacManBindings = null;
        }
        if (soundManager != null) {
            soundManager.dispose();
            soundManager = null;
        }
    }

    @Override
    public String assetNamespace() { return ASSET_NAMESPACE; }

    @Override
    public boolean hasGameCanvasRoundedBorder() { return false; }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public NonArcadeMapsSpriteSheet nonArcadeMapsSpriteSheet() {
        return nonArcadeMapsSpriteSheet;
    }

    @Override
    public TengenMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_GameRenderer(this, canvas);
        renderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        return renderer;
    }

    @Override
    public Image killedGhostPointsImage(Ghost ghost, int killedIndex) {
        return localAssetImage("ghost_points_" + killedIndex);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return localAssetImage("bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        //TODO: should this logic be implemented here?
        // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
        byte usedSymbol = switch (symbol) {
            case TengenMsPacMan_GameModel.BONUS_BANANA    -> 8; // 5000!
            case TengenMsPacMan_GameModel.BONUS_MILK      -> 6; // 3000!
            case TengenMsPacMan_GameModel.BONUS_ICE_CREAM -> 7; // 4000!
            default -> symbol;
        };
        return localAssetImage("bonus_value_" + usedSymbol);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        NES_ColorScheme scheme = worldMap.getConfigValue("nesColorScheme");
        return new WorldMapColorScheme(scheme.fillColorRGB(), scheme.strokeColorRGB(), scheme.strokeColorRGB(), scheme.pelletColorRGB());
    }

    @Override
    public TengenMsPacMan_GhostAnimationManager createGhostAnimations(Ghost ghost) {
        return new TengenMsPacMan_GhostAnimationManager(spriteSheet(), ghost.id().personality());
    }

    @Override
    public TengenMsPacMan_PacAnimationManager createPacAnimations(Pac pac) {
        return new TengenMsPacMan_PacAnimationManager(spriteSheet());
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createMsPacManBody(
            ui.uiPreferences().getFloat("3d.lives_counter.shape_size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"),
            localAssetColor("pac.color.hairbow"),
            localAssetColor("pac.color.hairbow.pearls"),
            localAssetColor("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, GameLevel gameLevel, Pac pac) {
        var pac3D = new MsPacMan3D(
            ui.assets().theModel3DRepository(),
            animationRegistry,
            pac,
            ui.uiPreferences().getFloat("3d.pac.size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"),
            localAssetColor("pac.color.hairbow"),
            localAssetColor("pac.color.hairbow.pearls"),
            localAssetColor("pac.color.boobs")
        );
        pac3D.light().setColor(localAssetColor("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,               new TengenMsPacMan_BootScene(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D,              new TengenMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D,              new TengenMsPacMan_OptionsScene(ui));
        scenesByID.put(SCENE_ID_CREDITS_SCENE_2D,            new TengenMsPacMan_CreditsScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,               new TengenMsPacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,               new TengenMsPacMan_PlayScene3D(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(1), new TengenMsPacMan_CutScene1(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(2), new TengenMsPacMan_CutScene2(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(3), new TengenMsPacMan_CutScene3(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(4), new TengenMsPacMan_CutScene4(ui));
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.gameState()) {
            case BOOT -> SCENE_ID_BOOT_SCENE_2D;
            case SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE_2D;
            case SHOWING_CREDITS -> SCENE_ID_CREDITS_SCENE_2D;
            case INTRO -> SCENE_ID_INTRO_SCENE_2D;
            case INTERMISSION -> {
                if (gameContext.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = gameContext.gameLevel().number();
                OptionalInt optCutSceneNumber = gameContext.game().optCutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield SCENE_ID_CUT_SCENE_N_2D.formatted(optCutSceneNumber.getAsInt());
            }
            case CutScenesTestState testState -> SCENE_ID_CUT_SCENE_N_2D.formatted(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? SCENE_ID_PLAY_SCENE_3D : SCENE_ID_PLAY_SCENE_2D;
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    // Map repository

    private record CacheKey(MapCategory mapCategory, Object mazeID, NES_ColorScheme colorScheme) {}

    private static NES_ColorScheme colorSchemeForNonArcadeMap(NonArcadeMapsSpriteSheet.MazeID mazeID){
        return switch (mazeID) {
            case MAZE1                  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE2                  -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case MAZE3                  -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case MAZE4                  -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case MAZE5                  -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case MAZE6                  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE7                  -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case MAZE8                  -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case MAZE9                  -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case MAZE10_BIG             -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case MAZE11                 -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case MAZE12                 -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case MAZE13                 -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case MAZE14_BIG             -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case MAZE15                 -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case MAZE16_MINI            -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case MAZE17_BIG             -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case MAZE18                 -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case MAZE19_BIG             -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case MAZE20_BIG             -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case MAZE21_BIG             -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case MAZE22_BIG             -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case MAZE23_BIG             -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case MAZE24                 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case MAZE25_BIG             -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case MAZE26_BIG             -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case MAZE27                 -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case MAZE28_MINI            -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case MAZE29                 -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case MAZE30_MINI            -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case MAZE31                 -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case MAZE32_ANIMATED        -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;

            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mazeID);
        };
    }

    private static List<NES_ColorScheme> randomColorSchemesOtherThan(int count, NES_ColorScheme colorScheme) {
        var randomColorSchemes = new HashSet<NES_ColorScheme>();
        while (randomColorSchemes.size() < count) {
            NES_ColorScheme randomColorScheme = NES_ColorScheme.randomScheme();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }

    /**
     * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
     * These files contain the images for all mazes used in the different map categories, but only in the colors
     * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
     * <p>The MINI and BIG maps use different color schemes.
     * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
     * the recolored maze images are stored.
     */

    public ColoredMazeSpriteSet createMazeSpriteSet(WorldMap worldMap, int flashCount) {
        MapCategory mapCategory = worldMap.getConfigValue("mapCategory");
        int mapNumber = worldMap.getConfigValue("mapNumber");
        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
        // for randomly colored maps (levels 28-31, non-ARCADE map categories), multiple flash colors appear
        boolean multipleFlashColors = worldMap.getConfigValue("multipleFlashColors");
        return switch (mapCategory) {
            case ARCADE  -> arcadeMazeSpriteSet(mapNumber, nesColorScheme, flashCount);
            case MINI    -> miniMazeSpriteSet(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case BIG     -> bigMazeSpriteSet(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case STRANGE -> strangeMazeSpriteSet(
                               worldMap.getConfigValue("mazeID"), // set by map selector!
                               multipleFlashColors ? worldMap.getConfigValue("nesColorScheme") : null,
                               flashCount,
                               multipleFlashColors);
        };
    }

    private ColoredMazeSpriteSet arcadeMazeSpriteSet(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        ArcadeMapsSpriteSheet.MazeID id = switch (mapNumber) {
            case 1 -> ArcadeMapsSpriteSheet.MazeID.MAZE1;
            case 2 -> ArcadeMapsSpriteSheet.MazeID.MAZE2;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> ArcadeMapsSpriteSheet.MazeID.MAZE3;
                case _35_28_20_PINK_YELLOW_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE5;
                case _17_20_20_BROWN_WHITE_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE7;
                case _0F_20_28_BLACK_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE9;
                default -> throw new IllegalArgumentException("No maze image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> ArcadeMapsSpriteSheet.MazeID.MAZE4;
                case _36_15_20_PINK_RED_WHITE      -> ArcadeMapsSpriteSheet.MazeID.MAZE6;
                case _13_20_28_VIOLET_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE8;
                default -> throw new IllegalArgumentException("No maze image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };
        RectShort originalMazeSprite = arcadeMapsSpriteSheet.sprite(id);
        var mazeSprite = new RecoloredSpriteImage(arcadeMapsSpriteSheet.sourceImage(), arcadeMapsSpriteSheet.sprite(id), colorScheme);
        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        //TODO: Handle case when color scheme is already black & white
        RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(
                MapCategory.ARCADE, mapNumber, originalMazeSprite, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorScheme);
        for (int i = 0; i < flashCount; ++i) {
            flashingMazeSprites.add(blackWhiteMazeSprite);
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet miniMazeSpriteSet(
        int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors)
    {
        NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case 1 -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2 -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 4 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 5 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 6 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };
        RectShort originalMazeSprite = nonArcadeMapsSpriteSheet.sprite(mazeID);
        RecoloredSpriteImage mazeSprite = colorScheme.equals(availableColorScheme)
                ? new RecoloredSpriteImage(nonArcadeMapsSpriteSheet.sourceImage(), originalMazeSprite, colorScheme)
                : recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite, colorScheme, availableColorScheme);

        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemesOtherThan(flashCount, colorScheme)) {
                RecoloredSpriteImage randomMazeSprite = recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite,
                        randomScheme, availableColorScheme);
                flashingMazeSprites.add(randomMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite,
                    NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, availableColorScheme);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet bigMazeSpriteSet(
        int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors
    ) {
        NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case  1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE19_BIG;
            case  2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE20_BIG;
            case  3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE21_BIG;
            case  4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE22_BIG;
            case  5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE23_BIG;
            case  6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE17_BIG;
            case  7 -> NonArcadeMapsSpriteSheet.MazeID.MAZE10_BIG;
            case  8 -> NonArcadeMapsSpriteSheet.MazeID.MAZE14_BIG;
            case  9 -> NonArcadeMapsSpriteSheet.MazeID.MAZE26_BIG;
            case 10 -> NonArcadeMapsSpriteSheet.MazeID.MAZE25_BIG;
            case 11 -> NonArcadeMapsSpriteSheet.MazeID.MAZE33_BIG;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };
        NES_ColorScheme colorSchemeInSpriteSheet = switch (mapNumber) {
            case  1 -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case  2 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case  3 -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case  5 -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case  7 -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case 10 -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            default -> null;
        };
        RectShort originalMazeSprite = nonArcadeMapsSpriteSheet.sprite(mazeID);
        RecoloredSpriteImage mazeSprite = colorScheme.equals(colorSchemeInSpriteSheet)
                ? new RecoloredSpriteImage(nonArcadeMapsSpriteSheet.sourceImage(), originalMazeSprite, colorScheme)
                : recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite, colorScheme, colorSchemeInSpriteSheet);

        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemesOtherThan(flashCount, colorScheme)) {
                RecoloredSpriteImage randomColorMazeSprite = recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite,
                        randomScheme, colorSchemeInSpriteSheet);
                flashingMazeSprites.add(randomColorMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite,
                    NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorSchemeInSpriteSheet);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet strangeMazeSpriteSet(
        NonArcadeMapsSpriteSheet.MazeID mazeID, NES_ColorScheme randomColorScheme, int flashCount, boolean multipleFlashColors
    ) {
        final RectShort originalMazeSprite = mazeID == NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED
                ? nonArcadeMapsSpriteSheet.spriteSequence(mazeID)[0]
                : nonArcadeMapsSpriteSheet.sprite(mazeID);
        final NES_ColorScheme originalColorScheme = colorSchemeForNonArcadeMap(mazeID);
        final NES_ColorScheme requestedColorScheme = randomColorScheme != null ? randomColorScheme : originalColorScheme;
        final RecoloredSpriteImage mazeSprite = requestedColorScheme.equals(originalColorScheme)
                ? new RecoloredSpriteImage(nonArcadeMapsSpriteSheet.sourceImage(), originalMazeSprite, originalColorScheme)
                : recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite, requestedColorScheme, originalColorScheme);

        final var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (NES_ColorScheme colorScheme : randomColorSchemesOtherThan(flashCount, requestedColorScheme)) {
                RecoloredSpriteImage randomColorMazeSprite = recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite,
                        colorScheme, originalColorScheme);
                flashingMazeSprites.add(randomColorMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite,
                    NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, originalColorScheme);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private RecoloredSpriteImage recoloredMazeImage(
            MapCategory mapCategory, Object mazeID, RectShort mazeSprite,
            NES_ColorScheme requestedScheme, NES_ColorScheme existingScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedScheme);
        RecoloredSpriteImage mazeImage = recoloredMazeImageCache.get(key);
        if (mazeImage == null) {
            SpriteSheet<?> spriteSheet = mapCategory == MapCategory.ARCADE ? arcadeMapsSpriteSheet : nonArcadeMapsSpriteSheet;
            mazeImage = new RecoloredSpriteImage(
                    exchangeNES_ColorScheme(spriteSheet.image(mazeSprite), existingScheme, requestedScheme),
                    new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                    requestedScheme);
            recoloredMazeImageCache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedScheme, recoloredMazeImageCache.size());
        }
        return mazeImage;
    }
}