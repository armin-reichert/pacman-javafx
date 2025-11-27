/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.Blinky;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.Inky;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.Pinky;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.Sue;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.*;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameStateMachine.GameState.*;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUI_Config, GameScene_Config {

    public enum AnimationID {
        ANIM_MS_PAC_MAN_BOOSTER,
        ANIM_MS_PAC_MAN_WAVING_HAND,
        ANIM_MS_PAC_MAN_TURNING_AWAY,
        ANIM_PAC_MAN_MUNCHING,
        ANIM_PAC_MAN_BOOSTER,
        ANIM_PAC_MAN_WAVING_HAND,
        ANIM_PAC_MAN_TURNING_AWAY,
        ANIM_JUNIOR
    }

    public static final String SPRITE_SHEET_PATH           = "graphics/spritesheet.png";
    public static final String ARCADE_MAZES_IMAGE_PATH     = "graphics/arcade_mazes.png";
    public static final String NON_ARCADE_MAZES_IMAGE_PATH = "graphics/non_arcade_mazes.png";

    public static final String MAPS_PATH = "/de/amr/pacmanfx/tengen/ms_pacman/maps/";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.tengen.ms_pacman.localized_texts");

    public static final String CONFIG_KEY_MAP_CATEGORY = "mapCategory";
    // Name of configuration property under which the correctly recolored maze sprite set is stored
    public static final String CONFIG_KEY_MAZE_ID = "mazeID";
    public static final String CONFIG_KEY_MAZE_SPRITE_SET = "mazeSpriteSet";
    public static final String CONFIG_KEY_MULTIPLE_FLASH_COLORS = "multipleFlashColors";
    public static final String CONFIG_KEY_NES_COLOR_SCHEME = "nesColorScheme";

    /** 32x30 */
    public static final Vector2i NES_TILES = new Vector2i(32, 30);

    /** 256x240 */
    public static final Vector2i NES_SIZE_PX = new Vector2i(256, 240);

    /** 32/30 = 1.0666 */
    public static final float NES_ASPECT = 32f / 30f;

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_TENGEN  = () -> TengenMsPacMan_UIConfig.class;

    private static final Color[] NES_COLORS = IntStream.range(0, 64)
        .mapToObj(NES_Palette::color).map(Color::web).toArray(Color[]::new);

    /**
     * @param index NES color palette index
     * @return RGB color for palette entry
     */
    public static Color nesColor(int index) {
        return NES_COLORS[index];
    }

    private static final Color[] BLUE_SHADES = {
        NES_COLORS[0x01], NES_COLORS[0x11], NES_COLORS[0x21], NES_COLORS[0x31]
    };

    /**
     * Blue color, changing from dark to brighter blue. Cycles through NES palette indices 0x01, 0x11, 0x21, 0x31 each 16 ticks.
     */
    public static Color shadeOfBlue(long tick) {
        return BLUE_SHADES[(int) (tick % 64) / 16];
    }

    private final GameUI ui;

    private final SoundManager soundManager = new SoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final AssetMap assets = new AssetMap();
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final ArcadeMapsSpriteSheet arcadeMapsSpriteSheet;
    private final NonArcadeMapsSpriteSheet nonArcadeMapsSpriteSheet;
    private final Set<ActionBinding> tengenActionBindings;
    private final MapColoringService coloringService = new MapColoringService();

    public TengenMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);

        assets.setLocalizedTexts(TEXT_BUNDLE);

        spriteSheet              = new TengenMsPacMan_SpriteSheet(RES_TENGEN.loadImage(SPRITE_SHEET_PATH));
        arcadeMapsSpriteSheet    = new ArcadeMapsSpriteSheet(RES_TENGEN.loadImage(ARCADE_MAZES_IMAGE_PATH));
        nonArcadeMapsSpriteSheet = new NonArcadeMapsSpriteSheet(RES_TENGEN.loadImage(NON_ARCADE_MAZES_IMAGE_PATH));

        Joypad jp = ui.joypad();
        tengenActionBindings = Set.of(
            new ActionBinding(ACTION_STEER_UP,            jp.key(JoypadButton.UP),    control(KeyCode.UP)),
            new ActionBinding(ACTION_STEER_DOWN,          jp.key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
            new ActionBinding(ACTION_STEER_LEFT,          jp.key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
            new ActionBinding(ACTION_STEER_RIGHT,         jp.key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
            new ActionBinding(ACTION_QUIT_DEMO_LEVEL,     jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_ENTER_START_SCREEN,  jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_START_PLAYING,       jp.key(JoypadButton.START)),
            new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,  jp.key(JoypadButton.A), jp.key(JoypadButton.B)),
            new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
            new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
        );
    }

    public Set<ActionBinding> tengenActionBindings() {
        return tengenActionBindings;
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public void loadAssets() {
        assets.set("app_icon",                         RES_TENGEN.loadImage("graphics/icons/mspacman.png"));
        assets.set("startpage.image1",                 RES_TENGEN.loadImage("graphics/flyer-page-1.png"));
        assets.set("startpage.image2",                 RES_TENGEN.loadImage("graphics/flyer-page-2.png"));

        assets.set("color.game_over_message",          nesColor(0x11));
        assets.set("color.ready_message",              nesColor(0x28));

        assets.set("pac.color.head",                   nesColor(0x28));
        assets.set("pac.color.eyes",                   nesColor(0x02));
        assets.set("pac.color.palate",                 nesColor(0x2d));
        assets.set("pac.color.boobs",                  nesColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        assets.set("pac.color.hairbow",                nesColor(0x05));
        assets.set("pac.color.hairbow.pearls",         nesColor(0x02));

        assets.set("ghost.0.color.normal.dress",       nesColor(0x05));
        assets.set("ghost.0.color.normal.eyeballs",    nesColor(0x20));
        assets.set("ghost.0.color.normal.pupils",      nesColor(0x16));

        assets.set("ghost.1.color.normal.dress",       nesColor(0x25));
        assets.set("ghost.1.color.normal.eyeballs",    nesColor(0x20));
        assets.set("ghost.1.color.normal.pupils",      nesColor(0x11));

        assets.set("ghost.2.color.normal.dress",       nesColor(0x11));
        assets.set("ghost.2.color.normal.eyeballs",    nesColor(0x20));
        assets.set("ghost.2.color.normal.pupils",      nesColor(0x11));

        assets.set("ghost.3.color.normal.dress",       nesColor(0x16));
        assets.set("ghost.3.color.normal.eyeballs",    nesColor(0x20));
        assets.set("ghost.3.color.normal.pupils",      nesColor(0x05));

        assets.set("ghost.color.frightened.dress",     nesColor(0x01));
        assets.set("ghost.color.frightened.eyeballs",  nesColor(0x20));
        assets.set("ghost.color.frightened.pupils",    nesColor(0x20));

        //TODO sprite sheet provides two flashing colors, when to use which?
        assets.set("ghost.color.flashing.dress",       nesColor(0x20));
        assets.set("ghost.color.flashing.eyeballs",    nesColor(0x20));
        assets.set("ghost.color.flashing.pupils",      nesColor(0x20));

        soundManager.registerAudioClip("audio.option.selection_changed",  RES_TENGEN.url("sound/ms-select1.wav"));
        soundManager.registerAudioClip("audio.option.value_changed",      RES_TENGEN.url("sound/ms-select2.wav"));

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,           RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,            RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,            RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,             RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        //TODO this is not used in Tengen Ms. Pac-Man, remove?
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,                 RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,            RES_TENGEN.url("sound/fruitbounce.wav"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,               RES_TENGEN.url("sound/ms-fruit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,                RES_TENGEN.url("sound/ms-extralife.wav"));
        soundManager.registerAudioClip(SoundID.GAME_OVER,                 RES_TENGEN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,              RES_TENGEN.url("sound/ms-start.wav"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,               RES_TENGEN.url("sound/ms-ghosteat.wav"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,           RES_TENGEN.url("sound/ms-eyes.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,          RES_TENGEN.url("sound/theymeet.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,          RES_TENGEN.url("sound/thechase.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,          RES_TENGEN.url("sound/junior.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4,          RES_TENGEN.url("sound/theend.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4 + ".junior.1", RES_TENGEN.url("sound/ms-theend1.wav"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_4 + ".junior.2", RES_TENGEN.url("sound/ms-theend2.wav"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,             RES_TENGEN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,          RES_TENGEN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,           RES_TENGEN.url("sound/ms-death.wav"));
        soundManager.registerAudioClip(SoundID.PAC_MAN_MUNCHING,          RES_TENGEN.url("sound/ms-dot.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,           RES_TENGEN.url("sound/ms-power.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,                 RES_TENGEN.url("sound/ms-siren1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,                 RES_TENGEN.url("sound/ms-siren2.wav"));// TODO

        //TODO fix the sound file instead
        MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }
    }

    @Override
    public void dispose() {
        Logger.info("Disposing {}", getClass().getSimpleName());
        assets.removeAll();
        coloringService.dispose();
        soundManager.dispose();
    }

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
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_GameLevelRenderer(canvas, this);
        renderer.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        return renderer;
    }

    @Override
    public TengenMsPacMan_HUD_Renderer createHUDRenderer(Canvas canvas) {
        return new TengenMsPacMan_HUD_Renderer(canvas, spriteSheet, ui.clock());
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(canvas, spriteSheet);
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        RectShort[] numberSprites = spriteSheet.spriteSequence(SpriteID.GHOST_NUMBERS);
        return spriteSheet.image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        RectShort[] symbolSprites = spriteSheet.spriteSequence(SpriteID.BONUS_SYMBOLS);
        return spriteSheet.image(symbolSprites[symbol]);
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
        RectShort[] sprites = spriteSheet.spriteSequence(SpriteID.BONUS_VALUES);
        return spriteSheet.image(sprites[usedSymbol]);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        NES_ColorScheme scheme = worldMap.getConfigValue(CONFIG_KEY_NES_COLOR_SCHEME);
        return new WorldMapColorScheme(scheme.fillColorRGB(), scheme.strokeColorRGB(), scheme.strokeColorRGB(), scheme.pelletColorRGB());
    }

    @Override
    public Ghost createAnimatedGhost(byte personality) {
        requireValidGhostPersonality(personality);
        Ghost ghost = switch (personality) {
            case RED_GHOST_SHADOW   -> new Blinky();
            case PINK_GHOST_SPEEDY  -> new Pinky();
            case CYAN_GHOST_BASHFUL -> new Inky();
            case ORANGE_GHOST_POKEY -> new Sue();
            default -> throw new IllegalArgumentException("Illegal ghost personality " + personality);
        };
        ghost.setAnimationManager(createGhostAnimations(personality));
        ghost.selectAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimationManager createGhostAnimations(byte personality) {
        return new TengenMsPacMan_GhostAnimationManager(spriteSheet(), personality);
    }

    @Override
    public TengenMsPacMan_PacAnimationManager createPacAnimations() {
        return new TengenMsPacMan_PacAnimationManager(spriteSheet());
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return PacManModel3DRepository.theRepository().createMsPacManBody(
            ui.preferences().getFloat("3d.lives_counter.shape_size"),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac, double size) {
        var pac3D = new MsPacMan3D(
            PacManModel3DRepository.theRepository(),
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
        pac3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public GameScene_Config sceneConfig() {
        return this;
    }

    @Override
    public void createGameScenes(GameUI ui) {
        scenesByID.put(SCENE_ID_BOOT_SCENE,    new TengenMsPacMan_BootScene(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE,   new TengenMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE,   new TengenMsPacMan_OptionsScene(ui));
        scenesByID.put(SCENE_ID_HALL_OF_FAME, new TengenMsPacMan_CreditsScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,    new TengenMsPacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,    new TengenMsPacMan_PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),       new TengenMsPacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),       new TengenMsPacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),       new TengenMsPacMan_CutScene3(ui));
        scenesByID.put(sceneID_CutScene(4),       new TengenMsPacMan_CutScene4(ui));
    }

    @Override
    public boolean canvasDecorated(GameScene gameScene) {
        return false;
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.currentGameState()) {
            case BOOT -> SCENE_ID_BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE;
            case SHOWING_HALL_OF_FAME -> SCENE_ID_HALL_OF_FAME;
            case INTRO -> SCENE_ID_INTRO_SCENE;
            case INTERMISSION -> {
                if (gameContext.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = gameContext.gameLevel().number();
                Optional<Integer> optCutSceneNumber = gameContext.currentGame().optCutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield sceneID_CutScene(optCutSceneNumber.get());
            }
            case CutScenesTestState testState -> sceneID_CutScene(testState.testedCutSceneNumber);
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

    private static NES_ColorScheme colorSchemeOfNonArcadeMap(NonArcadeMapsSpriteSheet.MazeID mazeID){
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

    public void configureHighlightedMazeRenderInfo(RenderInfo info, GameLevel gameLevel, int frame) {
        WorldMap worldMap = gameLevel.worldMap();
        MazeSpriteSet mazeSpriteSet = worldMap.getConfigValue(CONFIG_KEY_MAZE_SPRITE_SET);
        ColoredSpriteImage flashingMazeSprite = mazeSpriteSet.flashingMazeImages().get(frame);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, flashingMazeSprite.spriteSheetImage());
        info.put(CommonRenderInfoKey.MAZE_SPRITE, flashingMazeSprite.sprite());
    }

    public void configureNormalMazeRenderInfo(RenderInfo info, MapCategory mapCategory, WorldMap worldMap, long tick) {
        int mapNumber = worldMap.getConfigValue(CONFIG_KEY_MAP_NUMBER);
        MazeSpriteSet mazeSpriteSet = worldMap.getConfigValue(CONFIG_KEY_MAZE_SPRITE_SET);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, mazeSpriteSet.mazeImage().spriteSheetImage());
        if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
            int spriteIndex = mazeAnimationSpriteIndex(tick);
            info.put(CommonRenderInfoKey.MAZE_SPRITE, nonArcadeMapsSpriteSheet().spriteSequence(MAZE32_ANIMATED)[spriteIndex]);
        } else {
            info.put(CommonRenderInfoKey.MAZE_SPRITE, mazeSpriteSet.mazeImage().sprite());
        }
    }

    /**
     * Strange map #15 (maze #32): psychedelic animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private int mazeAnimationSpriteIndex(long tick) {
        long block = (tick % 32) / 8;
        return (int) (block < 3 ? block : 1);
    }

    /*
     * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
     * These files contain the images for all mazes used in the different map categories, but only in the colors
     * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
     * <p>The MINI and BIG maps use different color schemes.
     * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
     * the recolored maze images are stored.
     */

    public MazeSpriteSet createMazeSpriteSet(WorldMap worldMap, int flashCount) {
        final MapCategory mapCategory = worldMap.getConfigValue(CONFIG_KEY_MAP_CATEGORY);
        final int mapNumber = worldMap.getConfigValue(CONFIG_KEY_MAP_NUMBER);
        final NES_ColorScheme requestedColorScheme = worldMap.getConfigValue(CONFIG_KEY_NES_COLOR_SCHEME);
        // for randomly colored maps (levels 28-31, non-ARCADE maps), multiple random flash colors appear
        final boolean randomFlashColors = worldMap.getConfigValue(CONFIG_KEY_MULTIPLE_FLASH_COLORS);

        return switch (mapCategory) {
            case ARCADE  -> arcadeMazeSpriteSet(mapNumber, requestedColorScheme, flashCount);
            case MINI    -> miniMazeSpriteSet(mapNumber, requestedColorScheme, flashCount, randomFlashColors);
            case BIG     -> bigMazeSpriteSet(mapNumber, requestedColorScheme, flashCount, randomFlashColors);
            case STRANGE -> {
                NonArcadeMapsSpriteSheet.MazeID mazeID = worldMap.getConfigValue(CONFIG_KEY_MAZE_ID); // set by map selector!
                yield strangeMazeSpriteSet(
                    mazeID,
                    randomFlashColors ? requestedColorScheme : null,
                    flashCount,
                    randomFlashColors);
            }
        };
    }

    private MazeSpriteSet arcadeMazeSpriteSet(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        // All requested maze color schemes exist in the sprite sheet, we only have to select the right sprite for the
        // requested (map number, color scheme) combination:

        ArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
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

        RectShort mazeSprite = arcadeMapsSpriteSheet.sprite(mazeID);
        var coloredMaze = new ColoredSpriteImage(arcadeMapsSpriteSheet.sourceImage(), mazeSprite, colorScheme);

        //TODO: Handle case when color scheme is already black & white
        List<ColoredSpriteImage> flashingMazes = coloringService.createFlashingMazeList(
            MapCategory.ARCADE, mazeID,
            arcadeMapsSpriteSheet, mazeSprite,
            colorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK,
            false, flashCount);

        return new MazeSpriteSet(coloredMaze, flashingMazes);
    }

    private MazeSpriteSet miniMazeSpriteSet(
        int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        NES_ColorScheme originalColorScheme = switch (mapNumber) {
            case 1 -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2 -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 4 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 5 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 6 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        return coloringService.createMazeSet(
            MapCategory.MINI, mazeID,
            nonArcadeMapsSpriteSheet, nonArcadeMapsSpriteSheet.sprite(mazeID),
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }

    private MazeSpriteSet bigMazeSpriteSet(
        int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

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

        NES_ColorScheme originalColorScheme = switch (mapNumber) {
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

        return coloringService.createMazeSet(
            MapCategory.BIG, mazeID,
            nonArcadeMapsSpriteSheet, nonArcadeMapsSpriteSheet.sprite(mazeID),
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }

    private MazeSpriteSet strangeMazeSpriteSet(
        NonArcadeMapsSpriteSheet.MazeID mazeID,
        NES_ColorScheme optionalRandomColorScheme,
        int flashCount,
        boolean randomFlashColors) {

        final RectShort mazeSprite = mazeID == NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED
            ? nonArcadeMapsSpriteSheet.spriteSequence(mazeID)[0]
            : nonArcadeMapsSpriteSheet.sprite(mazeID);

        final NES_ColorScheme originalColorScheme = colorSchemeOfNonArcadeMap(mazeID);
        final NES_ColorScheme requestedColorScheme = optionalRandomColorScheme == null
            ? originalColorScheme : optionalRandomColorScheme;

        return coloringService.createMazeSet(
            MapCategory.STRANGE, mazeID,
            nonArcadeMapsSpriteSheet, mazeSprite,
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }
}