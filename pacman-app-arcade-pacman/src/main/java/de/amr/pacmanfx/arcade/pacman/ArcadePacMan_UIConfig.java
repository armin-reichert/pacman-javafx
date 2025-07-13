/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.rendering.*;
import de.amr.pacmanfx.arcade.pacman.scenes.*;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.GameUI.PY_3D_ENABLED;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements PacManGames_UIConfig {

    private static final String NAMESPACE = "pacman";

    private static final ResourceManager RES_PACMAN_UI = () -> GameUI.class;
    private static final ResourceManager RES_ARCADE_PAC_MAN = () -> ArcadePacMan_UIConfig.class;

    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288); // 28x36 tiles

    public static final String ANIM_BIG_PAC_MAN               = "big_pac_man";
    public static final String ANIM_BLINKY_DAMAGED            = "blinky_damaged";
    public static final String ANIM_BLINKY_PATCHED            = "blinky_patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "blinky_nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "blinky_naked";

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public void storeAssets(AssetStorage assets) {
        storeAssetNS(assets, "app_icon", RES_ARCADE_PAC_MAN.loadImage("graphics/icons/pacman.png"));

        storeAssetNS(assets, "startpage.image1", RES_ARCADE_PAC_MAN.loadImage("graphics/f1.jpg"));
        storeAssetNS(assets, "startpage.image2", RES_ARCADE_PAC_MAN.loadImage("graphics/f2.jpg"));
        storeAssetNS(assets, "startpage.image3", RES_ARCADE_PAC_MAN.loadImage("graphics/f3.jpg"));

        var spriteSheet = new ArcadePacMan_SpriteSheet(RES_ARCADE_PAC_MAN.loadImage("graphics/pacman_spritesheet.png"));
        storeAssetNS(assets, "spritesheet", spriteSheet);
        storeAssetNS(assets, "flashing_maze", RES_ARCADE_PAC_MAN.loadImage("graphics/maze_flashing.png"));
        storeAssetNS(assets, "color.game_over_message", ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 7; ++symbol) {
            storeAssetNS(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeAssetNS(assets, "bonus_value_" + symbol,  spriteSheet.image(valueSprites[symbol]));
        }

        storeAssetNS(assets, "pac.color.head",   ARCADE_YELLOW);
        storeAssetNS(assets, "pac.color.eyes",   Color.grayRgb(33));
        storeAssetNS(assets, "pac.color.palate", ARCADE_BROWN);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeAssetNS(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeAssetNS(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeAssetNS(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeAssetNS(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeAssetNS(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeAssetNS(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeAssetNS(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);

        storeAssetNS(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeAssetNS(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeAssetNS(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);

        storeAssetNS(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeAssetNS(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeAssetNS(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);

        storeAssetNS(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeAssetNS(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeAssetNS(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);

        storeAssetNS(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeAssetNS(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeAssetNS(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeAssetNS(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeAssetNS(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeAssetNS(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_OFF,       RES_PACMAN_UI.url("sound/voice/autopilot-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_AUTOPILOT_ON,        RES_PACMAN_UI.url("sound/voice/autopilot-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_OFF,        RES_PACMAN_UI.url("sound/voice/immunity-off.mp3"));
        soundManager.registerVoice(SoundID.VOICE_IMMUNITY_ON,         RES_PACMAN_UI.url("sound/voice/immunity-on.mp3"));
        soundManager.registerVoice(SoundID.VOICE_EXPLAIN,             RES_PACMAN_UI.url("sound/voice/press-key.mp3"));

        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         RES_ARCADE_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            RES_ARCADE_PAC_MAN.url("sound/extend.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           RES_ARCADE_PAC_MAN.url("sound/common/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          RES_ARCADE_PAC_MAN.url("sound/game_start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           RES_ARCADE_PAC_MAN.url("sound/eat_ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       RES_ARCADE_PAC_MAN.url("sound/retreating.mp3"));
        soundManager.registerMediaPlayer("audio.intermission",        RES_ARCADE_PAC_MAN.url("sound/intermission.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         RES_ARCADE_PAC_MAN.url("sound/common/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      RES_ARCADE_PAC_MAN.url("sound/common/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       RES_ARCADE_PAC_MAN.url("sound/pacman_death.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,    RES_ARCADE_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       RES_ARCADE_PAC_MAN.url("sound/ghost-turn-to-blue.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             RES_ARCADE_PAC_MAN.url("sound/siren_1.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             RES_ARCADE_PAC_MAN.url("sound/siren_2.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             RES_ARCADE_PAC_MAN.url("sound/siren_3.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             RES_ARCADE_PAC_MAN.url("sound/siren_4.mp3"));
    }

    @Override
    public void destroy() {
        theUI().theAssets().removeAll(NAMESPACE + ".");
        soundManager.destroy();
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public String assetNamespace() {
        return NAMESPACE;
    }

    @Override
    public ArcadePacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new ArcadePacMan_GameRenderer(spriteSheet(), canvas);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return MAP_COLORING;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return getAssetNS("spritesheet");
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
        return getAssetNS("ghost_points_" + killedIndex);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return getAssetNS("bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return getAssetNS("bonus_value_" + symbol);
    }

    @Override
    public PacBody createLivesCounterShape3D(Model3DRepository model3DRepository) {
        return model3DRepository.createPacBody(
                GameUI.Settings3D.LIVES_COUNTER_3D_SHAPE_SIZE,
                getAssetNS("pac.color.head"),
                getAssetNS("pac.color.eyes"),
                getAssetNS("pac.color.palate")
        );
    }

    @Override
    public PacMan3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new PacMan3D(model3DRepository,
            animationManager,
            pac,
            GameUI.Settings3D.PAC_3D_SIZE,
            getAssetNS("pac.color.head"),
            getAssetNS("pac.color.eyes"),
            getAssetNS("pac.color.palate"));
        pac3D.light().setColor(this.<Color>getAssetNS("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scene config

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public void createGameScenes(GameContext gameContext) {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D(gameContext));
        scenesByID.put("IntroScene",  new ArcadePacMan_IntroScene(gameContext));
        scenesByID.put("StartScene",  new ArcadePacMan_StartScene(gameContext));
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D(gameContext));
        scenesByID.put("PlayScene3D", new PlayScene3D(gameContext));
        scenesByID.put("CutScene1",   new ArcadePacMan_CutScene1(gameContext));
        scenesByID.put("CutScene2",   new ArcadePacMan_CutScene2(gameContext));
        scenesByID.put("CutScene3",   new ArcadePacMan_CutScene3(gameContext));
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.theGameState()) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> {
                if (gameContext.optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = gameContext.theGameLevel().number();
                if (gameContext.theGame().cutSceneNumber(levelNumber).isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + gameContext.theGame().cutSceneNumber(levelNumber).getAsInt();
            }
            case GameState.TESTING_CUT_SCENES -> "CutScene" + gameContext.theGame().<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
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