/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.*;
import de.amr.pacmanfx.ui.*;
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
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUI_Config {

    private static final String NAMESPACE = "tengen";

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_TENGEN_MS_PAC_MAN = () -> TengenMsPacMan_UIConfig.class;

    public static final String MAP_PATH = "/de/amr/pacmanfx/tengen/ms_pacman/maps/";

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

    // Actions specific to Tengen Ms. Pac-Man

    public static final AbstractGameAction ACTION_QUIT_DEMO_LEVEL = new AbstractGameAction("QUIT_DEMO_LEVEL") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.gameContext().optGameLevel().isPresent() && ui.gameContext().theGameLevel().isDemoLevel();
        }
    };

    public static final AbstractGameAction ACTION_ENTER_START_SCREEN = new AbstractGameAction("ENTER_START_SCREEN") {
        @Override
        public void execute(GameUI ui) {
            ui.gameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
        }
    };

    public static final AbstractGameAction ACTION_START_PLAYING = new AbstractGameAction("START_PLAYING") {
        @Override
        public void execute(GameUI ui) {
            ui.soundManager().stopAll();
            ui.gameContext().theGame().playingProperty().set(false);
            ui.gameContext().theGameController().changeGameState(GameState.STARTING_GAME);
        }
    };

    public static final AbstractGameAction ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE = new AbstractGameAction("TOGGLE_PLAY_SCENE_DISPLAY_MODE") {
        @Override
        public void execute(GameUI ui) {
            var config = ui.<TengenMsPacMan_UIConfig>currentConfig();
            SceneDisplayMode mode = config.propertyPlaySceneDisplayMode.get();
            config.propertyPlaySceneDisplayMode.set(mode == SceneDisplayMode.SCROLLING
                ? SceneDisplayMode.SCALED_TO_FIT
                : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            return ui.isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_2D);
        }
    };

    public static final AbstractGameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY = new AbstractGameAction("TOGGLE_JOYPAD_BINDINGS_DISPLAYED") {
        @Override
        public void execute(GameUI ui) {
            toggle(ui.<TengenMsPacMan_UIConfig>currentConfig().propertyJoypadBindingsDisplayed);
        }
    };

    public static final AbstractGameAction ACTION_TOGGLE_PAC_BOOSTER = new AbstractGameAction("TOGGLE_PAC_BOOSTER") {
        @Override
        public void execute(GameUI ui) {
            var tengenGame = ui.gameContext().<TengenMsPacMan_GameModel>theGame();
            tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
            if (tengenGame.isBoosterActive()) {
                ui.showFlashMessage("Booster!");
            }
        }

        @Override
        public boolean isEnabled(GameUI ui) {
            var tengenGame = ui.gameContext().<TengenMsPacMan_GameModel>theGame();
            return tengenGame.pacBooster() == PacBooster.USE_A_OR_B;
        }
    };


    //TODO not sure these belong here
    public final BooleanProperty propertyJoypadBindingsDisplayed = new SimpleBooleanProperty(false);
    public final ObjectProperty<SceneDisplayMode> propertyPlaySceneDisplayMode = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private final GameUI ui;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private TengenMsPacMan_MapRepository mapRepository;
    private final List<ActionBinding> tengenMsPacManBindings;

    public TengenMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        Joypad joypad = ui.joypad();
        tengenMsPacManBindings = List.of(
            new ActionBinding(ACTION_STEER_UP,            joypad.key(JoypadButton.UP),    control(KeyCode.UP)),
            new ActionBinding(ACTION_STEER_DOWN,          joypad.key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
            new ActionBinding(ACTION_STEER_LEFT,          joypad.key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
            new ActionBinding(ACTION_STEER_RIGHT,         joypad.key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
            new ActionBinding(ACTION_QUIT_DEMO_LEVEL,     joypad.key(JoypadButton.START)),
            new ActionBinding(ACTION_ENTER_START_SCREEN,  joypad.key(JoypadButton.START)),
            new ActionBinding(ACTION_START_PLAYING,       joypad.key(JoypadButton.START)),
            new ActionBinding(ACTION_TOGGLE_PAC_BOOSTER,  joypad.key(JoypadButton.A), joypad.key(JoypadButton.B)),
            new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_DISPLAY_MODE, alt(KeyCode.C)),
            new ActionBinding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAY, nude(KeyCode.SPACE))
        );
    }

    public List<ActionBinding> tengenMsPacManBindings() {
        return tengenMsPacManBindings;
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    @Override
    public void storeAssets(AssetStorage assets) {
        mapRepository = new TengenMsPacMan_MapRepository(
            RES_TENGEN_MS_PAC_MAN.loadImage("graphics/arcade_mazes.png"),
            RES_TENGEN_MS_PAC_MAN.loadImage("graphics/non_arcade_mazes.png")
        );

        var spriteSheet = new TengenMsPacMan_SpriteSheet(RES_TENGEN_MS_PAC_MAN.loadImage("graphics/spritesheet.png"));
        storeAssetNS(assets, "spritesheet", spriteSheet);

        storeAssetNS(assets, "app_icon",         RES_TENGEN_MS_PAC_MAN.loadImage("graphics/icons/mspacman.png"));
        storeAssetNS(assets, "startpage.image1", RES_TENGEN_MS_PAC_MAN.loadImage("graphics/f1.png"));
        storeAssetNS(assets, "startpage.image2", RES_TENGEN_MS_PAC_MAN.loadImage("graphics/f2.png"));

        soundManager.registerAudioClip("audio.option.selection_changed", RES_TENGEN_MS_PAC_MAN.url("sound/ms-select1.wav"));
        soundManager.registerAudioClip("audio.option.value_changed",     RES_TENGEN_MS_PAC_MAN.url("sound/ms-select2.wav"));

        storeAssetNS(assets, "color.game_over_message", nesPaletteColor(0x11));
        storeAssetNS(assets, "color.ready_message",     nesPaletteColor(0x28));

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 13; ++symbol) {
            storeAssetNS(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeAssetNS(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeAssetNS(assets, "pac.color.head",                   nesPaletteColor(0x28));
        storeAssetNS(assets, "pac.color.eyes",                   nesPaletteColor(0x02));
        storeAssetNS(assets, "pac.color.palate",                 nesPaletteColor(0x2d));
        storeAssetNS(assets, "pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        storeAssetNS(assets, "pac.color.hairbow",                nesPaletteColor(0x05));
        storeAssetNS(assets, "pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeAssetNS(assets, "ghost_points_0",                   spriteSheet.image(numberSprites[0]));
        storeAssetNS(assets, "ghost_points_1",                   spriteSheet.image(numberSprites[1]));
        storeAssetNS(assets, "ghost_points_2",                   spriteSheet.image(numberSprites[2]));
        storeAssetNS(assets, "ghost_points_3",                   spriteSheet.image(numberSprites[3]));

        storeAssetNS(assets, "ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        storeAssetNS(assets, "ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        storeAssetNS(assets, "ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        storeAssetNS(assets, "ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        storeAssetNS(assets, "ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        storeAssetNS(assets, "ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        storeAssetNS(assets, "ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        storeAssetNS(assets, "ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        storeAssetNS(assets, "ghost.color.frightened.dress",     nesPaletteColor(0x01));
        storeAssetNS(assets, "ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO sprite sheet provides two flashing colors, when to use which?
        storeAssetNS(assets, "ghost.color.flashing.dress",       nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        storeAssetNS(assets, "ghost.color.flashing.pupils",      nesPaletteColor(0x20));

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,           RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,            RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,            RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,             RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,                 RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,            RES_TENGEN_MS_PAC_MAN.url("sound/fruitbounce.wav"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,               RES_TENGEN_MS_PAC_MAN.url("sound/ms-fruit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,                RES_TENGEN_MS_PAC_MAN.url("sound/ms-extralife.wav"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,               RES_TENGEN_MS_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,              RES_TENGEN_MS_PAC_MAN.url("sound/ms-start.wav"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,               RES_TENGEN_MS_PAC_MAN.url("sound/ms-ghosteat.wav"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,           RES_TENGEN_MS_PAC_MAN.url("sound/ms-eyes.wav"));
        soundManager.registerMediaPlayer("audio.intermission.1",          RES_TENGEN_MS_PAC_MAN.url("sound/theymeet.wav"));
        soundManager.registerMediaPlayer("audio.intermission.2",          RES_TENGEN_MS_PAC_MAN.url("sound/thechase.wav"));
        soundManager.registerMediaPlayer("audio.intermission.3",          RES_TENGEN_MS_PAC_MAN.url("sound/junior.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4",          RES_TENGEN_MS_PAC_MAN.url("sound/theend.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4.junior.1", RES_TENGEN_MS_PAC_MAN.url("sound/ms-theend1.wav"));
        soundManager.registerMediaPlayer("audio.intermission.4.junior.2", RES_TENGEN_MS_PAC_MAN.url("sound/ms-theend2.wav"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,             RES_TENGEN_MS_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,          RES_TENGEN_MS_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,           RES_TENGEN_MS_PAC_MAN.url("sound/ms-death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,        RES_TENGEN_MS_PAC_MAN.url("sound/ms-dot.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,           RES_TENGEN_MS_PAC_MAN.url("sound/ms-power.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,                 RES_TENGEN_MS_PAC_MAN.url("sound/ms-siren1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,                 RES_TENGEN_MS_PAC_MAN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,                 RES_TENGEN_MS_PAC_MAN.url("sound/ms-siren2.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,                 RES_TENGEN_MS_PAC_MAN.url("sound/ms-siren2.wav"));// TODO

        //TODO fix this in the sound file
        MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25);
        }
    }

    @Override
    public void dispose() {
        ui.assets().removeAll(NAMESPACE + ".");
        soundManager.dispose();
    }

    @Override
    public String assetNamespace() { return NAMESPACE; }

    @Override
    public boolean hasGameCanvasRoundedBorder() { return false; }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return getAssetNS("spritesheet");
    }

    @Override
    public TengenMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_GameRenderer(ui.assets(), spriteSheet(), mapRepository, canvas);
        renderer.backgroundColorProperty().bind(PROPERTY_CANVAS_BACKGROUND_COLOR);
        return renderer;
    }

    @Override
    public Image killedGhostPointsImage(Ghost ghost, int killedIndex) {
        return getAssetNS("ghost_points_" + killedIndex);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return getAssetNS("bonus_symbol_" + symbol);
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
        return getAssetNS("bonus_value_" + usedSymbol);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
        return new WorldMapColorScheme(
            nesColorScheme.fillColorRGB(),
            nesColorScheme.strokeColorRGB(),
            nesColorScheme.strokeColorRGB(),
            nesColorScheme.pelletColorRGB()
        );
    }

    @Override
    public TengenMsPacMan_GhostAnimationMap createGhostAnimations(Ghost ghost) {
        return new TengenMsPacMan_GhostAnimationMap(spriteSheet(), ghost.personality());
    }

    @Override
    public TengenMsPacMan_PacAnimationMap createPacAnimations(Pac pac) {
        return new TengenMsPacMan_PacAnimationMap(spriteSheet());
    }

    @Override
    public MsPacManBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createMsPacManBody(
            ui.uiPreferences().getFloat("3d.lives_counter.shape_size"),
            getAssetNS("pac.color.head"),
            getAssetNS("pac.color.eyes"),
            getAssetNS("pac.color.palate"),
            getAssetNS("pac.color.hairbow"),
            getAssetNS("pac.color.hairbow.pearls"),
            getAssetNS("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac) {
        var pac3D = new MsPacMan3D(
            ui.assets().theModel3DRepository(),
            animationRegistry,
            pac,
            ui.uiPreferences().getFloat("3d.pac.size"),
            getAssetNS("pac.color.head"),
            getAssetNS("pac.color.eyes"),
            getAssetNS("pac.color.palate"),
            getAssetNS("pac.color.hairbow"),
            getAssetNS("pac.color.hairbow.pearls"),
            getAssetNS("pac.color.boobs")
        );
        pac3D.light().setColor(this.<Color>getAssetNS("pac.color.head").desaturate());
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

        //TODO where is the best place to do that?
        var playScene2D = (TengenMsPacMan_PlayScene2D) scenesByID.get(SCENE_ID_PLAY_SCENE_2D);
        playScene2D.displayModeProperty().bind(propertyPlaySceneDisplayMode);
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.theGameState()) {
            case BOOT -> SCENE_ID_BOOT_SCENE_2D;
            case SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE_2D;
            case SHOWING_CREDITS -> SCENE_ID_CREDITS_SCENE_2D;
            case INTRO -> SCENE_ID_INTRO_SCENE_2D;
            case GameState.INTERMISSION -> {
                if (gameContext.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = gameContext.theGameLevel().number();
                OptionalInt optCutSceneNumber = gameContext.theGame().cutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield SCENE_ID_CUT_SCENE_N_2D.formatted(optCutSceneNumber.getAsInt());
            }
            case GameState.TESTING_CUT_SCENES -> {
                int cutSceneNumber = gameContext.theGame().<Integer>getProperty("intermissionTestNumber");
                yield SCENE_ID_CUT_SCENE_N_2D.formatted(cutSceneNumber);
            }
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
}