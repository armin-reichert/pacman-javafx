/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.PacBooster;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.*;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.ActionBindingSupport.binding;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_CANVAS_BG_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.Ufx.toggle;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements PacManGames_UIConfig, ResourceManager {

    private static final String NAMESPACE = "tengen";

    public static final String MAP_PATH = "/de/amr/pacmanfx/tengen/ms_pacman/maps/";

    public static final BooleanProperty PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED = new SimpleBooleanProperty(false);
    public static final ObjectProperty<SceneDisplayMode> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    public static Color nesPaletteColor(int index) {
        return Color.web(NES_Palette.color(index));
    }

    /** 32x30 */
    public static final Vector2i NES_TILES = new Vector2i(32, 30);

    /** 256x240 */
    public static final Vector2f NES_SIZE_PX = NES_TILES.scaled(TS).toVector2f();

    /** 32/30 */
    public static final float NES_ASPECT = NES_SIZE_PX.x() / NES_SIZE_PX.y();

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private TengenMsPacMan_MapRepository mapRepository;
    private final DefaultSoundManager soundManager = new DefaultSoundManager(assetNamespace());

    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return TengenMsPacMan_UIConfig.class;
    }

    public void loadAssets(AssetStorage assets) {
        spriteSheet = new TengenMsPacMan_SpriteSheet(loadImage("graphics/spritesheet.png"));
        mapRepository = new TengenMsPacMan_MapRepository(
            loadImage("graphics/arcade_mazes.png"),
            loadImage("graphics/non_arcade_mazes.png")
        );

        storeInMyNamespace(assets, "app_icon",                         loadImage("graphics/icons/mspacman.png"));
        storeInMyNamespace(assets, "startpage.image1",                 loadImage("graphics/f1.png"));
        storeInMyNamespace(assets, "startpage.image2",                 loadImage("graphics/f2.png"));

        storeInMyNamespace(assets, "color.game_over_message",          nesPaletteColor(0x11));
        storeInMyNamespace(assets, "color.ready_message",              nesPaletteColor(0x28));

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 13; ++symbol) {
            storeInMyNamespace(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeInMyNamespace(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeInMyNamespace(assets, "pac.color.head",                   nesPaletteColor(0x28));
        storeInMyNamespace(assets, "pac.color.eyes",                   nesPaletteColor(0x02));
        storeInMyNamespace(assets, "pac.color.palate",                 nesPaletteColor(0x2d));
        storeInMyNamespace(assets, "pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        storeInMyNamespace(assets, "pac.color.hairbow",                nesPaletteColor(0x05));
        storeInMyNamespace(assets, "pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeInMyNamespace(assets, "ghost_points_0",                   spriteSheet.image(numberSprites[0]));
        storeInMyNamespace(assets, "ghost_points_1",                   spriteSheet.image(numberSprites[1]));
        storeInMyNamespace(assets, "ghost_points_2",                   spriteSheet.image(numberSprites[2]));
        storeInMyNamespace(assets, "ghost_points_3",                   spriteSheet.image(numberSprites[3]));

        storeInMyNamespace(assets, "ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        storeInMyNamespace(assets, "ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        storeInMyNamespace(assets, "ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        storeInMyNamespace(assets, "ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        storeInMyNamespace(assets, "ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        storeInMyNamespace(assets, "ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        storeInMyNamespace(assets, "ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        storeInMyNamespace(assets, "ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        storeInMyNamespace(assets, "ghost.color.frightened.dress",     nesPaletteColor(0x01));
        storeInMyNamespace(assets, "ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        storeInMyNamespace(assets, "ghost.color.flashing.dress",       nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        storeInMyNamespace(assets, "ghost.color.flashing.pupils",      nesPaletteColor(0x20));

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,            url("sound/fruitbounce.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,               url("sound/ms-fruit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,                url("sound/ms-extralife.wav"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,               url("sound/common/game-over.mp3"), 1);
        soundManager.registerMediaPlayer(SoundID.GAME_READY,              url("sound/ms-start.wav"), 1);
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,               url("sound/ms-ghosteat.wav"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,           url("sound/ms-eyes.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerMediaPlayer("audio.intermission.1",          url("sound/theymeet.wav"), 1);
        soundManager.registerMediaPlayer("audio.intermission.2",          url("sound/thechase.wav"), 1);
        soundManager.registerMediaPlayer("audio.intermission.3",          url("sound/junior.wav"), 1);
        soundManager.registerMediaPlayer("audio.intermission.4",          url("sound/theend.wav"), 1);
        soundManager.registerMediaPlayer("audio.intermission.4.junior.1", url("sound/ms-theend1.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerMediaPlayer("audio.intermission.4.junior.2", url("sound/ms-theend2.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerAudioClip("audio.option.selection_changed",  url("sound/ms-select1.wav"));
        soundManager.registerAudioClip("audio.option.value_changed",      url("sound/ms-select2.wav"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,             url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,          url("sound/common/level-complete.mp3"), 1);
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,           url("sound/ms-death.wav"), 1);
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,        url("sound/ms-dot.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,           url("sound/ms-power.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerMediaPlayer(SoundID.SIREN_1,                 url("sound/ms-siren1.wav"), MediaPlayer.INDEFINITE);
        soundManager.registerMediaPlayer(SoundID.SIREN_2,                 url("sound/ms-siren2.wav"), MediaPlayer.INDEFINITE);// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,                 url("sound/ms-siren2.wav"), MediaPlayer.INDEFINITE);// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,                 url("sound/ms-siren2.wav"), MediaPlayer.INDEFINITE);// TODO

        MediaPlayer bounceSound = soundManager.mediaPlayer(SoundID.BONUS_ACTIVE);
        if (bounceSound != null) {
            bounceSound.setRate(0.25); // TODO ugly!
        }
    }

    @Override
    public void unloadAssets(AssetStorage assetStorage) {
        assetStorage.removeAll(NAMESPACE + ".");
    }

    @Override
    public String assetNamespace() { return NAMESPACE; }

    @Override
    public boolean hasGameCanvasRoundedBorder() { return false; }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {return spriteSheet;}

    @Override
    public TengenMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_GameRenderer(spriteSheet, mapRepository, canvas);
        renderer.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        return renderer;
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return theAssets().image(NAMESPACE + ".bonus_symbol_" + symbol);
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
        return theAssets().image(NAMESPACE + ".bonus_value_" + usedSymbol);
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        NES_ColorScheme colorScheme = worldMap.getConfigValue("nesColorScheme");
        return new WorldMapColorScheme(
            colorScheme.fillColor(), colorScheme.strokeColor(), colorScheme.strokeColor(), colorScheme.pelletColor());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new TengenMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new TengenMsPacMan_PacAnimationMap(spriteSheet);
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(Model3DRepository model3DRepository) {
        return model3DRepository.createMsPacManBody(
            PacManGames_UI.LIVES_COUNTER_3D_SHAPE_SIZE,
            theAssets().color(NAMESPACE + ".pac.color.head"),
            theAssets().color(NAMESPACE + ".pac.color.eyes"),
            theAssets().color(NAMESPACE + ".pac.color.palate"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow.pearls"),
            theAssets().color(NAMESPACE + ".pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new MsPacMan3D(model3DRepository, animationManager, pac, PacManGames_UI.PAC_3D_SIZE,
            theAssets().color(NAMESPACE + ".pac.color.head"),
            theAssets().color(NAMESPACE + ".pac.color.eyes"),
            theAssets().color(NAMESPACE + ".pac.color.palate"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow"),
            theAssets().color(NAMESPACE + ".pac.color.hairbow.pearls"),
            theAssets().color(NAMESPACE + ".pac.color.boobs"));
        pac3D.light().setColor(theAssets().color(NAMESPACE + ".pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put("BootScene",      new TengenMsPacMan_BootScene());
        scenesByID.put("IntroScene",     new TengenMsPacMan_IntroScene());
        scenesByID.put("StartScene",     new TengenMsPacMan_OptionsScene());
        scenesByID.put("ShowingCredits", new TengenMsPacMan_CreditsScene());
        scenesByID.put("PlayScene2D",    new TengenMsPacMan_PlayScene2D());
        scenesByID.put("PlayScene3D",    new TengenMsPacMan_PlayScene3D());
        scenesByID.put("CutScene1",      new TengenMsPacMan_CutScene1());
        scenesByID.put("CutScene2",      new TengenMsPacMan_CutScene2());
        scenesByID.put("CutScene3",      new TengenMsPacMan_CutScene3());
        scenesByID.put("CutScene4",      new TengenMsPacMan_CutScene4());

        //TODO where is the best place to do that?
        var playScene2D = (TengenMsPacMan_PlayScene2D) scenesByID.get("PlayScene2D");
        playScene2D.displayModeProperty().bind(PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
    }

    @Override
    public GameScene selectGameScene(GameModel game, GameState gameState) {
        String sceneID = switch (gameState) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> {
                if (optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = theGameLevel().number();
                if (game.cutSceneNumber(levelNumber).isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + game.cutSceneNumber(levelNumber).getAsInt();
            }
            case GameState.TESTING_CUT_SCENES -> "CutScene" + game.<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
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

    // Actions

    public static final GameAction ACTION_QUIT_DEMO_LEVEL = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return optGameLevel().isPresent() && optGameLevel().get().isDemoLevel();
        }

        @Override
        public String name() {
            return "QUIT_DEMO_LEVEL";
        }
    };

    public static final GameAction ACTION_START_GAME = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theGameController().changeGameState(GameState.SETTING_OPTIONS);
        }

        @Override
        public String name() {
            return "START_GAME";
        }
    };

    public static final GameAction ACTION_START_PLAYING = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            theSound().stopAll();
            theGame().playingProperty().set(false);
            theGameController().changeGameState(GameState.STARTING_GAME);
        }

        @Override
        public String name() {
            return "START_PLAYING";
        }
    };

    public static final GameAction ACTION_TOGGLE_DISPLAY_MODE = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            SceneDisplayMode mode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();
            PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(mode == SceneDisplayMode.SCROLLING
                    ? SceneDisplayMode.SCALED_TO_FIT : SceneDisplayMode.SCROLLING);
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            return ui.currentGameSceneIsPlayScene2D();
        }

        @Override
        public String name() {
            return "TOGGLE_DISPLAY_MODE";
        }
    };

    public static final  GameAction ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            toggle(PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED);
        }

        @Override
        public String name() {
            return "TOGGLE_JOYPAD_BINDINGS_DISPLAYED";
        }
    };

    public static final GameAction ACTION_TOGGLE_PAC_BOOSTER = new GameAction() {
        @Override
        public void execute(PacManGames_UI ui) {
            var tengenGame = (TengenMsPacMan_GameModel) theGame();
            tengenGame.activatePacBooster(!tengenGame.isBoosterActive());
        }

        @Override
        public boolean isEnabled(PacManGames_UI ui) {
            var tengenGame = (TengenMsPacMan_GameModel) theGame();
            return tengenGame.pacBooster() == PacBooster.USE_A_OR_B;
        }

        @Override
        public String name() {
            return "TOGGLE_PAC_BOOSTER";
        }
    };

    // Key bindings

    public static final Map<GameAction, Set<KeyCombination>> TENGEN_ACTION_BINDINGS = Map.ofEntries(
        binding(ACTION_STEER_UP,            theJoypad().key(JoypadButton.UP),    control(KeyCode.UP)),
        binding(ACTION_STEER_DOWN,          theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN)),
        binding(ACTION_STEER_LEFT,          theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT)),
        binding(ACTION_STEER_RIGHT,         theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT)),
        binding(ACTION_QUIT_DEMO_LEVEL,     theJoypad().key(JoypadButton.START)),
        binding(ACTION_START_GAME,          theJoypad().key(JoypadButton.START)),
        binding(ACTION_START_PLAYING,       theJoypad().key(JoypadButton.START)),
        binding(ACTION_TOGGLE_PAC_BOOSTER,  theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B)),
        binding(ACTION_TOGGLE_DISPLAY_MODE, alt(KeyCode.C)),
        binding(ACTION_TOGGLE_JOYPAD_BINDINGS_DISPLAYED, nude(KeyCode.SPACE))
    );

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

}