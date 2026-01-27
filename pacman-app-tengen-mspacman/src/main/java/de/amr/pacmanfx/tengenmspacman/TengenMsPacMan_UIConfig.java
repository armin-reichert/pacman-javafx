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
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.actors.Blinky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Inky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Pinky;
import de.amr.pacmanfx.tengenmspacman.model.actors.Sue;
import de.amr.pacmanfx.tengenmspacman.rendering.*;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
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
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.GameUI.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUI_Config, GameScene_Config {

    public enum TengenSceneID implements SceneID {
        HALL_OF_FAME
    }

    public enum TengenMsPacMan_DashboardID implements DashboardID { JOYPAD }

    public static final Joypad JOYPAD = new Joypad(GameUI.KEYBOARD);

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

    public static final Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    JOYPAD.key(JoypadButton.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  JOYPAD.key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  JOYPAD.key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, JOYPAD.key(JoypadButton.RIGHT), control(KeyCode.RIGHT))
    );

    public static final Set<ActionBinding> ACTION_BINDINGS = Set.of(
        new ActionBinding(ACTION_QUIT_DEMO_LEVEL,     JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_ENTER_START_SCREEN,  JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_START_PLAYING,       JOYPAD.key(JoypadButton.START)),
        new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,  JOYPAD.key(JoypadButton.A), JOYPAD.key(JoypadButton.B)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
        new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, bare(KeyCode.SPACE))
    );

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

    public static final String SPRITE_SHEET_PATH          = "graphics/spritesheet.png";
    public static final String ARCADE_MAPS_IMAGE_PATH     = "graphics/arcade_mazes.png";
    public static final String NON_ARCADE_MAPS_IMAGE_PATH = "graphics/non_arcade_mazes.png";

    public static final String MAPS_PATH = "/de/amr/pacmanfx/tengenmspacman/maps/";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.tengenmspacman.localized_texts");

    public enum ConfigKey {
        MAP_CATEGORY,
        /** ID of correctly recolored maze sprite set */
        MAP_ID,
        MAP_SPRITE_SET,
        MULTIPLE_FLASH_COLORS,
        NES_COLOR_SCHEME
    }

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
    private final Map<SceneID, GameScene> scenesByID = new HashMap<>();
    private final SoundManager soundManager = new SoundManager();

    @Override
    public void init() {
        Logger.info("Init UI configuration {}", getClass().getSimpleName());
        loadAssets();
        registerSounds();
    }

    @Override
    public void dispose() {
        GameUI_Config.super.dispose();
        Logger.info("Dispose {} game scenes", scenesByID.size());
        scenesByID.values().forEach(GameScene::dispose);
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

        soundManager.registerSirens(
            LOCAL_RESOURCES.url("sound/ms-siren1.wav"),
            LOCAL_RESOURCES.url("sound/ms-siren2.wav"), // TODO
            LOCAL_RESOURCES.url("sound/ms-siren2.wav"), // TODO
            LOCAL_RESOURCES.url("sound/ms-siren2.wav")  // TODO
        );
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
    public TengenMsPacMan_HeadsUpDisplay_Renderer createHUDRenderer(Canvas canvas, GameScene2D gameScene2D) {
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
        final NES_ColorScheme scheme = worldMap.getConfigValue(ConfigKey.NES_COLOR_SCHEME);
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
        ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public TengenMsPacMan_GhostAnimations createGhostAnimations(byte personality) {
        return new TengenMsPacMan_GhostAnimations(personality);
    }

    @Override
    public TengenMsPacMan_PacAnimations createPacAnimations() {
        return new TengenMsPacMan_PacAnimations();
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return PacManModel3DRepository.instance().createMsPacManBody(
            GameUI_PreferencesManager.instance().getFloat("3d.lives_counter.shape_size"),
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
            PacManModel3DRepository.instance(),
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

    private GameScene createGameScene(SceneID sceneID) {
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE    -> new TengenMsPacMan_BootScene();
            case CommonSceneID.INTRO_SCENE   -> new TengenMsPacMan_IntroScene();
            case CommonSceneID.START_SCENE   -> new TengenMsPacMan_OptionsScene();
            case TengenSceneID.HALL_OF_FAME  -> new TengenMsPacMan_CreditsScene();
            case CommonSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D();
            case CommonSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D();
            case CommonSceneID.CUTSCENE_1    -> new TengenMsPacMan_CutScene1();
            case CommonSceneID.CUTSCENE_2    -> new TengenMsPacMan_CutScene2();
            case CommonSceneID.CUTSCENE_3    -> new TengenMsPacMan_CutScene3();
            case CommonSceneID.CUTSCENE_4    -> new TengenMsPacMan_CutScene4();
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        return false;
    }

    @Override
    public Optional<GameScene> selectGameScene(Game game) {
        final SceneID sceneID = switch (game.control().state()) {
            case BOOT -> CommonSceneID.BOOT_SCENE;
            case SETTING_OPTIONS_FOR_START -> CommonSceneID.START_SCENE;
            case SHOWING_HALL_OF_FAME -> TengenSceneID.HALL_OF_FAME;
            case INTRO -> CommonSceneID.INTRO_SCENE;
            case INTERMISSION -> {
                if (game.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                final int cutSceneNumber = game.level().cutSceneNumber();
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(game.level().number()));
                }
                yield GameScene_Config.cutSceneID(cutSceneNumber);
            }
            case CutScenesTestState testState -> GameScene_Config.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
        final GameScene gameScene = scenesByID.computeIfAbsent(sceneID, this::createGameScene);
        return Optional.of(gameScene);
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }
}