/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.controller.CutScenesTestState;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements GameUI_Config {

    public static final String ASSET_NAMESPACE = "pacman";

    private static final ResourceManager RES_GAME_UI = () -> GameUI_Implementation.class;
    private static final ResourceManager RES_ARCADE_PAC_MAN = () -> ArcadePacMan_UIConfig.class;

    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288); // 28x36 tiles

    public static final String ANIM_BIG_PAC_MAN               = "big_pac_man";
    public static final String ANIM_BLINKY_DAMAGED            = "blinky_damaged";
    public static final String ANIM_BLINKY_PATCHED            = "blinky_patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "blinky_nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "blinky_naked";

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final GameUI ui;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
        spriteSheet = new ArcadePacMan_SpriteSheet(RES_ARCADE_PAC_MAN.loadImage("graphics/pacman_spritesheet.png"));
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    public void storeAssets(AssetStorage assets) {
        storeLocalAssetValue(assets, "spritesheet", spriteSheet);
        storeLocalAssetValue(assets, "app_icon",         RES_ARCADE_PAC_MAN.loadImage("graphics/icons/pacman.png"));
        storeLocalAssetValue(assets, "startpage.image1", RES_ARCADE_PAC_MAN.loadImage("graphics/f1.jpg"));
        storeLocalAssetValue(assets, "startpage.image2", RES_ARCADE_PAC_MAN.loadImage("graphics/f2.jpg"));
        storeLocalAssetValue(assets, "startpage.image3", RES_ARCADE_PAC_MAN.loadImage("graphics/f3.jpg"));
        storeLocalAssetValue(assets, "flashing_maze",    RES_ARCADE_PAC_MAN.loadImage("graphics/maze_flashing.png"));
        storeLocalAssetValue(assets, "color.game_over_message", ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 7; ++symbol) {
            storeLocalAssetValue(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeLocalAssetValue(assets, "bonus_value_"  + symbol, spriteSheet.image(valueSprites[symbol]));
        }

        storeLocalAssetValue(assets, "pac.color.head",   ARCADE_YELLOW);
        storeLocalAssetValue(assets, "pac.color.eyes",   Color.grayRgb(33));
        storeLocalAssetValue(assets, "pac.color.palate", ARCADE_BROWN);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeLocalAssetValue(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeLocalAssetValue(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeLocalAssetValue(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeLocalAssetValue(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeLocalAssetValue(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeLocalAssetValue(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeLocalAssetValue(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeLocalAssetValue(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeLocalAssetValue(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);

        storeLocalAssetValue(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeLocalAssetValue(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeLocalAssetValue(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeLocalAssetValue(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF, RES_GAME_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,  RES_GAME_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,  RES_GAME_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,   RES_GAME_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,       RES_GAME_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_EATEN,        RES_ARCADE_PAC_MAN.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,      RES_ARCADE_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,         RES_ARCADE_PAC_MAN.url("sound/extend.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,        RES_ARCADE_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,       RES_ARCADE_PAC_MAN.url("sound/game_start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,        RES_ARCADE_PAC_MAN.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,    RES_ARCADE_PAC_MAN.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer("audio.intermission",     RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,      RES_ARCADE_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,   RES_ARCADE_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,    RES_ARCADE_PAC_MAN.url("sound/pacman_death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING, RES_ARCADE_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,    RES_ARCADE_PAC_MAN.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,          RES_ARCADE_PAC_MAN.url("sound/siren_1.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,          RES_ARCADE_PAC_MAN.url("sound/siren_2.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_3,          RES_ARCADE_PAC_MAN.url("sound/siren_3.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_4,          RES_ARCADE_PAC_MAN.url("sound/siren_4.mp3"));
    }

    @Override
    public void dispose() {
        ui.assets().removeAll(ASSET_NAMESPACE + ".");
        soundManager.dispose();
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public String assetNamespace() {
        return ASSET_NAMESPACE;
    }

    @Override
    public ArcadePacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new ArcadePacMan_GameRenderer(ui.assets(), spriteSheet(), canvas);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return MAP_COLORING;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public ArcadePacMan_GhostAnimationMap createGhostAnimations(Ghost ghost) {
        return new ArcadePacMan_GhostAnimationMap(spriteSheet(), ghost.personality());
    }

    @Override
    public ArcadePacMan_PacAnimationMap createPacAnimations(Pac pac) {
        return new ArcadePacMan_PacAnimationMap(spriteSheet());
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
        return localAssetImage("bonus_value_" + symbol);
    }

    @Override
    public PacBody createLivesCounterShape3D() {
        return ui.assets().theModel3DRepository().createPacBody(
            ui.uiPreferences().getFloat("3d.lives_counter.shape_size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate")
        );
    }

    @Override
    public PacMan3D createPac3D(AnimationRegistry animationRegistry, Pac pac) {
        var pac3D = new PacMan3D(ui.assets().theModel3DRepository(),
            animationRegistry,
            pac,
            ui.uiPreferences().getFloat("3d.pac.size"),
            localAssetColor("pac.color.head"),
            localAssetColor("pac.color.eyes"),
            localAssetColor("pac.color.palate"));
        pac3D.light().setColor(localAssetColor("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scene config

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public void createGameScenes() {
        scenesByID.put(SCENE_ID_BOOT_SCENE_2D,               new ArcadePacMan_BootScene2D(ui));
        scenesByID.put(SCENE_ID_INTRO_SCENE_2D,              new ArcadePacMan_IntroScene(ui));
        scenesByID.put(SCENE_ID_START_SCENE_2D,              new ArcadePacMan_StartScene(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_2D,               new ArcadePacMan_PlayScene2D(ui));
        scenesByID.put(SCENE_ID_PLAY_SCENE_3D,               new PlayScene3D(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(1), new ArcadePacMan_CutScene1(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(2), new ArcadePacMan_CutScene2(ui));
        scenesByID.put(SCENE_ID_CUT_SCENE_N_2D.formatted(3), new ArcadePacMan_CutScene3(ui));
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.gameState()) {
            case GamePlayState.BOOT -> SCENE_ID_BOOT_SCENE_2D;
            case GamePlayState.SETTING_OPTIONS_FOR_START -> SCENE_ID_START_SCENE_2D;
            case GamePlayState.INTRO -> SCENE_ID_INTRO_SCENE_2D;
            case GamePlayState.INTERMISSION -> {
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
}