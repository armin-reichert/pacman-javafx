/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_PlayScene2D;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectShort;
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
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.MsPacManBody;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements PacManGames_UIConfig {

    private static final String NAMESPACE = "ms_pacman";

    private static final ResourceManager RES_PACMAN_UI = () -> GameUI.class;
    private static final ResourceManager RES_ARCADE_MS_PAC_MAN = () -> ArcadeMsPacMan_UIConfig.class;

    private static final List<WorldMapColorScheme> WORLD_MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColorScheme("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColorScheme("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

    private final GameUI ui;
    private final DefaultSoundManager soundManager = new DefaultSoundManager();
    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private BrightMazesSpriteSheet spriteSheetBrightMazes;

    public ArcadeMsPacMan_UIConfig(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    @Override
    public GameUI theUI() {
        return ui;
    }

    public void storeAssets(AssetStorage assets) {
        var spriteSheet = new ArcadeMsPacMan_SpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mspacman_spritesheet.png"));
        storeAssetNS(assets, "spritesheet", spriteSheet);
        spriteSheetBrightMazes = new BrightMazesSpriteSheet(RES_ARCADE_MS_PAC_MAN.loadImage("graphics/mazes_flashing.png"));

        storeAssetNS(assets, "app_icon",                        RES_ARCADE_MS_PAC_MAN.loadImage("graphics/icons/mspacman.png"));

        storeAssetNS(assets, "startpage.image1",                RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f1.jpg"));
        storeAssetNS(assets, "startpage.image2",                RES_ARCADE_MS_PAC_MAN.loadImage("graphics/f2.jpg"));

        storeAssetNS(assets, "logo.midway",                     RES_ARCADE_MS_PAC_MAN.loadImage("graphics/midway_logo.png"));

        storeAssetNS(assets, "color.game_over_message",         ARCADE_RED);

        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        RectShort[] valueSprites  = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 6; ++symbol) {
            storeAssetNS(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeAssetNS(assets, "bonus_value_" + symbol,  spriteSheet.image(valueSprites[symbol]));
        }

        storeAssetNS(assets, "pac.color.head",                  ARCADE_YELLOW);
        storeAssetNS(assets, "pac.color.eyes",                  Color.grayRgb(33));
        storeAssetNS(assets, "pac.color.palate",                ARCADE_BROWN);
        storeAssetNS(assets, "pac.color.boobs",                 ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        storeAssetNS(assets, "pac.color.hairbow",               ARCADE_RED);
        storeAssetNS(assets, "pac.color.hairbow.pearls",        ARCADE_BLUE);

        RectShort[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeAssetNS(assets, "ghost_points_0",                  spriteSheet.image(numberSprites[0]));
        storeAssetNS(assets, "ghost_points_1",                  spriteSheet.image(numberSprites[1]));
        storeAssetNS(assets, "ghost_points_2",                  spriteSheet.image(numberSprites[2]));
        storeAssetNS(assets, "ghost_points_3",                  spriteSheet.image(numberSprites[3]));

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

        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,        RES_ARCADE_MS_PAC_MAN.url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClip(SoundID.BONUS_EATEN,           RES_ARCADE_MS_PAC_MAN.url("sound/Fruit.mp3"));
        soundManager.registerAudioClip(SoundID.COIN_INSERTED,         RES_ARCADE_MS_PAC_MAN.url("sound/credit.wav"));
        soundManager.registerAudioClip(SoundID.EXTRA_LIFE,            RES_ARCADE_MS_PAC_MAN.url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,           RES_ARCADE_MS_PAC_MAN.url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,          RES_ARCADE_MS_PAC_MAN.url("sound/Start.mp3"));
        soundManager.registerAudioClip(SoundID.GHOST_EATEN,           RES_ARCADE_MS_PAC_MAN.url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,       RES_ARCADE_MS_PAC_MAN.url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.1",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.2",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer("audio.intermission.3",      RES_ARCADE_MS_PAC_MAN.url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClip(SoundID.LEVEL_CHANGED,         RES_ARCADE_MS_PAC_MAN.url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,      RES_ARCADE_MS_PAC_MAN.url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,       RES_ARCADE_MS_PAC_MAN.url("sound/Died.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_MUNCHING,    RES_ARCADE_MS_PAC_MAN.url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,       RES_ARCADE_MS_PAC_MAN.url("sound/ScaredGhost.mp3"));
        soundManager.registerMediaPlayer(SoundID.SIREN_1,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));
        soundManager.registerMediaPlayer(SoundID.SIREN_2,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_3,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
        soundManager.registerMediaPlayer(SoundID.SIREN_4,             RES_ARCADE_MS_PAC_MAN.url("sound/GhostNoise1.wav"));// TODO
    }

    @Override
    public void destroy() {
        ui.theAssets().removeAll(NAMESPACE + ".");
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
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return getAssetNS("spritesheet");
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return WORLD_MAP_COLOR_SCHEMES.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public ArcadeMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_GameRenderer(ui.theAssets(), this, spriteSheetBrightMazes, canvas);
    }

    @Override
    public ArcadeMsPacMan_GhostAnimationMap createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationMap(spriteSheet(), ghost.personality());
    }

    @Override
    public ArcadeMsPacMan_PacAnimationMap createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationMap(spriteSheet());
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
    public MsPacManBody createLivesCounterShape3D() {
        return ui.theModel3DRepository().createMsPacManBody(
            GameUI.Settings3D.LIVES_COUNTER_3D_SHAPE_SIZE,
            getAssetNS("pac.color.head"),
            getAssetNS("pac.color.eyes"),
            getAssetNS("pac.color.palate"),
            getAssetNS("pac.color.hairbow"),
            getAssetNS("pac.color.hairbow.pearls"),
            getAssetNS("pac.color.boobs")
        );
    }

    @Override
    public MsPacMan3D createPac3D(AnimationManager animationManager, Pac pac) {
        var pac3D = new MsPacMan3D(
            ui.theModel3DRepository(),
            animationManager,
            pac,
            GameUI.Settings3D.PAC_3D_SIZE,
            getAssetNS("pac.color.head"),
            getAssetNS("pac.color.eyes"),
            getAssetNS("pac.color.palate"),
            getAssetNS("pac.color.hairbow"),
            getAssetNS("pac.color.hairbow.pearls"),
            getAssetNS("pac.color.boobs"));
        pac3D.light().setColor(this.<Color>getAssetNS("pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes(GameUI ui) {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D(ui));
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene(ui));
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene(ui));
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D(ui));
        scenesByID.put("PlayScene3D", new PlayScene3D(ui));
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1(ui));
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2(ui));
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3(ui));
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public boolean gameSceneHasID(GameScene gameScene, String sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public GameScene selectGameScene(GameContext gameContext) {
        String sceneID = switch (gameContext.theGameState()) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS_FOR_START -> "StartScene";
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
            default -> ui.property3DEnabled().get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }
}