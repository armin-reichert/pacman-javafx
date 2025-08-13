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
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_MapRepository;
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
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.controller.GamePlayState.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.*;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_CANVAS_BACKGROUND_COLOR;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
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

    private final GameUI ui;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final Set<ActionBinding> tengenMsPacManBindings;
    private final TengenMsPacMan_MapRepository mapRepository;
    private final TengenMsPacMan_SpriteSheet spriteSheet;

    public TengenMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new TengenMsPacMan_SpriteSheet(RES_TENGEN.loadImage("graphics/spritesheet.png"));
        mapRepository = new TengenMsPacMan_MapRepository(
            RES_TENGEN.loadImage("graphics/arcade_mazes.png"),
            RES_TENGEN.loadImage("graphics/non_arcade_mazes.png")
        );

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

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
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

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
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
        ui.assets().removeAll(ASSET_NAMESPACE + ".");
        soundManager.dispose();
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

    @Override
    public TengenMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_GameRenderer(ui.assets(), spriteSheet(), mapRepository, canvas);
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
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"),
            localAssetColor("pac.color.hairbow"),
            localAssetColor("pac.color.hairbow.pearls"),
            localAssetColor("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac) {
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
                OptionalInt optCutSceneNumber = gameContext.game().cutSceneNumber(levelNumber);
                if (optCutSceneNumber.isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield SCENE_ID_CUT_SCENE_N_2D.formatted(optCutSceneNumber.getAsInt());
            }
            case CutScenesTestState ignored -> {
                //TODO: move field into test state
                if (gameContext.game() instanceof AbstractGameModel gameModel) {
                    yield SCENE_ID_CUT_SCENE_N_2D.formatted(gameModel.testedCutSceneNumber);
                }
                throw new IllegalStateException("Illegal game model");
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