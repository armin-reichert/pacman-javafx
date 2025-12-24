/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.actors.Blinky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Inky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Pinky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Sue;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
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
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameController.GameState.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.sceneID_CutScene;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUI_Config, GameScene_Config {

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

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

    public static final Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    GameUI.JOYPAD.key(JoypadButton.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  GameUI.JOYPAD.key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  GameUI.JOYPAD.key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, GameUI.JOYPAD.key(JoypadButton.RIGHT), control(KeyCode.RIGHT))
    );

    public static final Set<ActionBinding> ACTION_BINDINGS = Set.of(
        new ActionBinding(ACTION_QUIT_DEMO_LEVEL,     GameUI.JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_ENTER_START_SCREEN,  GameUI.JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_START_PLAYING,       GameUI.JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,  GameUI.JOYPAD.key(JoypadButton.A), GameUI.JOYPAD.key(JoypadButton.B)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
        new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
    );

    public static final String SPRITE_SHEET_PATH           = "graphics/spritesheet.png";
    public static final String ARCADE_MAZES_IMAGE_PATH     = "graphics/arcade_mazes.png";
    public static final String NON_ARCADE_MAZES_IMAGE_PATH = "graphics/non_arcade_mazes.png";

    public static final String MAPS_PATH = "/de/amr/pacmanfx/tengenmspacman/maps/";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");

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

    private final AssetMap assets = new AssetMap();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final SoundManager soundManager = new SoundManager();
    private final GameUI ui;

    private final MapColoringService coloringService = new MapColoringService();

    public TengenMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    @Override
    public void init() {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        registerSounds();
        createGameScenes(ui);
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}", getClass().getSimpleName());
        coloringService.dispose();
        assets.dispose();
        soundManager.dispose();
        scenesByID.clear();
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    private void loadAssets() {
        assets.clear();

        assets.set("app_icon",                         LOCAL_RESOURCES.loadImage("graphics/icons/mspacman.png"));
        assets.set("startpage.image1",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-1.png"));
        assets.set("startpage.image2",                 LOCAL_RESOURCES.loadImage("graphics/flyer-page-2.png"));

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

        assets.setLocalizedTexts(TEXT_BUNDLE);
    }

    private void registerSounds() {
        soundManager.registerAudioClipURL("audio.option.selection_changed",    LOCAL_RESOURCES.url("sound/ms-select1.wav"));
        soundManager.registerAudioClipURL("audio.option.value_changed",        LOCAL_RESOURCES.url("sound/ms-select2.wav"));

        soundManager.register(SoundID.VOICE_AUTOPILOT_OFF,          GameUI.VOICE_AUTOPILOT_OFF);
        soundManager.register(SoundID.VOICE_AUTOPILOT_ON,           GameUI.VOICE_AUTOPILOT_ON);
        soundManager.register(SoundID.VOICE_EXPLAIN_GAME_START,     GameUI.VOICE_EXPLAIN_GAME_START);
        soundManager.register(SoundID.VOICE_IMMUNITY_OFF,           GameUI.VOICE_IMMUNITY_OFF);
        soundManager.register(SoundID.VOICE_IMMUNITY_ON,            GameUI.VOICE_IMMUNITY_ON);

        soundManager.registerMedia(SoundID.VOICE_FLYER_TEXT,        LOCAL_RESOURCES.url("sound/flyer-text.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,            LOCAL_RESOURCES.url("sound/fruitbounce.wav"));
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
        soundManager.registerMedia(SoundID.SIREN_1,                  LOCAL_RESOURCES.url("sound/ms-siren1.wav"));
        soundManager.registerMedia(SoundID.SIREN_2,                  LOCAL_RESOURCES.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMedia(SoundID.SIREN_3,                  LOCAL_RESOURCES.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMedia(SoundID.SIREN_4,                  LOCAL_RESOURCES.url("sound/ms-siren2.wav"));// TODO

        //TODO fix the sound file instead
        final MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(Canvas canvas, GameScene2D gameScene2D) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case TengenMsPacMan_BootScene ignored -> new TengenMsPacMan_BootScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_IntroScene ignored -> new TengenMsPacMan_IntroScene_Renderer(gameScene2D, canvas, TengenMsPacMan_SpriteSheet.INSTANCE);
            case TengenMsPacMan_OptionsScene ignored -> new TengenMsPacMan_OptionsScene_Renderer(gameScene2D, canvas, TengenMsPacMan_SpriteSheet.INSTANCE);
            case TengenMsPacMan_PlayScene2D ignored -> new TengenMsPacMan_PlayScene2D_Renderer(gameScene2D, canvas, TengenMsPacMan_SpriteSheet.INSTANCE);
            case TengenMsPacMan_CreditsScene ignored -> new TengenMsPacMan_CreditsScene_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene1 ignored -> new TengenMsPacMan_CutScene1_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene2 ignored -> new TengenMsPacMan_CutScene2_Renderer(gameScene2D, canvas);
            case TengenMsPacMan_CutScene3 ignored -> new TengenMsPacMan_CutScene3_Renderer(gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public TengenMsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        final var renderer = new TengenMsPacMan_GameLevelRenderer(canvas, this);
        renderer.backgroundProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        return renderer;
    }

    @Override
    public TengenMsPacMan_HUD_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
        if (   ui.isCurrentGameSceneID(sceneID_CutScene(1))
            || ui.isCurrentGameSceneID(sceneID_CutScene(2))
            || ui.isCurrentGameSceneID(sceneID_CutScene(3))
            || ui.isCurrentGameSceneID(sceneID_CutScene(4)) )
        {
            final var hudRenderer = new TengenMsPacMan_HUD_Renderer(canvas, TengenMsPacMan_SpriteSheet.INSTANCE, ui.clock());
            hudRenderer.setOffsetY(-2*TS); //TODO this is ugly
            return gameScene2D.adaptRenderer(hudRenderer);
        }
        if (ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D)) {
            return gameScene2D.adaptRenderer(new TengenMsPacMan_HUD_Renderer(canvas, TengenMsPacMan_SpriteSheet.INSTANCE, ui.clock()));
        }
        return null;
    }

    @Override
    public TengenMsPacMan_ActorRenderer createActorRenderer(Canvas canvas) {
        return new TengenMsPacMan_ActorRenderer(canvas, TengenMsPacMan_SpriteSheet.INSTANCE);
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = TengenMsPacMan_SpriteSheet.INSTANCE.spriteSequence(SpriteID.GHOST_NUMBERS);
        return TengenMsPacMan_SpriteSheet.INSTANCE.image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        final RectShort[] symbolSprites = TengenMsPacMan_SpriteSheet.INSTANCE.spriteSequence(SpriteID.BONUS_SYMBOLS);
        return TengenMsPacMan_SpriteSheet.INSTANCE.image(symbolSprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        //TODO: should this logic be implemented here?
        // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
        final byte usedSymbol = switch (symbol) {
            case TengenMsPacMan_GameModel.BONUS_BANANA    -> 8; // 5000!
            case TengenMsPacMan_GameModel.BONUS_MILK      -> 6; // 3000!
            case TengenMsPacMan_GameModel.BONUS_ICE_CREAM -> 7; // 4000!
            default -> symbol;
        };
        final RectShort[] sprites = TengenMsPacMan_SpriteSheet.INSTANCE.spriteSequence(SpriteID.BONUS_VALUES);
        return TengenMsPacMan_SpriteSheet.INSTANCE.image(sprites[usedSymbol]);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        final NES_ColorScheme scheme = worldMap.getConfigValue(CONFIG_KEY_NES_COLOR_SCHEME);
        return new WorldMapColorScheme(scheme.fillColorRGB(), scheme.strokeColorRGB(), scheme.strokeColorRGB(), scheme.pelletColorRGB());
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
        requireValidGhostPersonality(personality);
        final Ghost ghost = switch (personality) {
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
        return new TengenMsPacMan_GhostAnimationManager(TengenMsPacMan_SpriteSheet.INSTANCE, personality);
    }

    @Override
    public TengenMsPacMan_PacAnimationManager createPacAnimations() {
        return new TengenMsPacMan_PacAnimationManager(TengenMsPacMan_SpriteSheet.INSTANCE);
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

    private void createGameScenes(GameUI ui) {
        scenesByID.put(SCENE_ID_BOOT_SCENE,    new TengenMsPacMan_BootScene(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE,   new TengenMsPacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE,   new TengenMsPacMan_OptionsScene(ui));
        scenesByID.put(SCENE_ID_HALL_OF_FAME,  new TengenMsPacMan_CreditsScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D, new TengenMsPacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D, new TengenMsPacMan_PlayScene3D(ui));
        scenesByID.put(sceneID_CutScene(1),    new TengenMsPacMan_CutScene1(ui));
        scenesByID.put(sceneID_CutScene(2),    new TengenMsPacMan_CutScene2(ui));
        scenesByID.put(sceneID_CutScene(3),    new TengenMsPacMan_CutScene3(ui));
        scenesByID.put(sceneID_CutScene(4),    new TengenMsPacMan_CutScene4(ui));
    }

    @Override
    public GameScene_Config sceneConfig() {
        return this;
    }

    @Override
    public boolean canvasDecorated(GameScene gameScene) {
        return false;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        final String sceneID = switch (game.control().state()) {
            case BOOT -> SCENE_ID_BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE;
            case SHOWING_HALL_OF_FAME -> SCENE_ID_HALL_OF_FAME;
            case INTRO -> SCENE_ID_INTRO_SCENE;
            case INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                final int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield sceneID_CutScene(cutSceneNumber);
            }
            case CutScenesTestState testState -> sceneID_CutScene(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? SCENE_ID_PLAY_SCENE_3D : SCENE_ID_PLAY_SCENE_2D;
        };
        return Optional.ofNullable(scenesByID.get(sceneID));
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
            case MAZE1           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE2           -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case MAZE3           -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case MAZE4           -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case MAZE5           -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case MAZE6           -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE7           -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case MAZE8           -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case MAZE9           -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case MAZE10_BIG      -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case MAZE11          -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case MAZE12          -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case MAZE13          -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case MAZE14_BIG      -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case MAZE15          -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case MAZE16_MINI     -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case MAZE17_BIG      -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case MAZE18          -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case MAZE19_BIG      -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case MAZE20_BIG      -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case MAZE21_BIG      -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case MAZE22_BIG      -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case MAZE23_BIG      -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case MAZE24          -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case MAZE25_BIG      -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case MAZE26_BIG      -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case MAZE27          -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case MAZE28_MINI     -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case MAZE29          -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case MAZE30_MINI     -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case MAZE31          -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case MAZE32_ANIMATED -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;

            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mazeID);
        };
    }

    public void configureHighlightedMazeRenderInfo(RenderInfo info, GameLevel gameLevel, int frame) {
        final WorldMap worldMap = gameLevel.worldMap();
        final MazeSpriteSet mazeSpriteSet = worldMap.getConfigValue(CONFIG_KEY_MAZE_SPRITE_SET);
        final ColoredSpriteImage flashingMazeSprite = mazeSpriteSet.flashingMazeImages().get(frame);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, flashingMazeSprite.spriteSheetImage());
        info.put(CommonRenderInfoKey.MAZE_SPRITE, flashingMazeSprite.sprite());
    }

    public void configureNormalMazeRenderInfo(RenderInfo info, MapCategory mapCategory, WorldMap worldMap, long tick) {
        final int mapNumber = worldMap.getConfigValue(CONFIG_KEY_MAP_NUMBER);
        final MazeSpriteSet mazeSpriteSet = worldMap.getConfigValue(CONFIG_KEY_MAZE_SPRITE_SET);
        info.put(CommonRenderInfoKey.MAZE_IMAGE, mazeSpriteSet.mazeImage().spriteSheetImage());
        if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
            final int spriteIndex = mazeAnimationSpriteIndex(tick);
            info.put(CommonRenderInfoKey.MAZE_SPRITE, NonArcadeMapsSpriteSheet.INSTANCE.spriteSequence(MAZE32_ANIMATED)[spriteIndex]);
        } else {
            info.put(CommonRenderInfoKey.MAZE_SPRITE, mazeSpriteSet.mazeImage().sprite());
        }
    }

    /**
     * Strange map #15 (maze #32): psychedelic animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private int mazeAnimationSpriteIndex(long tick) {
        final long block = (tick % 32) / 8;
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
                final NonArcadeMapsSpriteSheet.MazeID mazeID = worldMap.getConfigValue(CONFIG_KEY_MAZE_ID); // set by map selector!
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

        final ArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
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

        final RectShort mazeSprite = ArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID);
        final var coloredMaze = new ColoredSpriteImage(ArcadeMapsSpriteSheet.INSTANCE.sourceImage(), mazeSprite, colorScheme);

        //TODO: Handle case when color scheme is already black & white
        final List<ColoredSpriteImage> flashingMazes = coloringService.createFlashingMazeList(
            MapCategory.ARCADE, mazeID,
            ArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
            colorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK,
            false, flashCount);

        return new MazeSpriteSet(coloredMaze, flashingMazes);
    }

    private MazeSpriteSet miniMazeSpriteSet(
        int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };

        final NES_ColorScheme originalColorScheme = switch (mapNumber) {
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
            NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID),
            originalColorScheme, requestedColorScheme,
            randomFlashColors, flashCount
        );
    }

    private MazeSpriteSet bigMazeSpriteSet(
        int mapNumber, NES_ColorScheme requestedColorScheme, int flashCount, boolean randomFlashColors) {

        final NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
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

        final NES_ColorScheme originalColorScheme = switch (mapNumber) {
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
            NonArcadeMapsSpriteSheet.INSTANCE, NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID),
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
            ? NonArcadeMapsSpriteSheet.INSTANCE.spriteSequence(mazeID)[0]
            : NonArcadeMapsSpriteSheet.INSTANCE.sprite(mazeID);

        final NES_ColorScheme original = colorSchemeOfNonArcadeMap(mazeID);
        final NES_ColorScheme requested = optionalRandomColorScheme == null ? original : optionalRandomColorScheme;

        return coloringService.createMazeSet(
            MapCategory.STRANGE, mazeID,
            NonArcadeMapsSpriteSheet.INSTANCE, mazeSprite,
            original, requested,
            randomFlashColors, flashCount
        );
    }
}