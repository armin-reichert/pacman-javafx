/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.ArcadeCommon_PlayScene2D;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui._3d.Settings3D;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.optGameLevel;
import static de.amr.pacmanfx.Globals.theGameLevel;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements PacManGames_UIConfig, ResourceManager {

    private static final String ANS = "ms_pacman";

    private static final List<WorldMapColorScheme> WORLD_MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColorScheme("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColorScheme("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

    private ArcadeMsPacMan_SpriteSheet spriteSheet;
    private BrightMazesSpriteSheet brightMazesSpriteSheet;

    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public void loadAssets(AssetStorage assets) {
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(loadImage("graphics/mspacman_spritesheet.png"));
        brightMazesSpriteSheet = new BrightMazesSpriteSheet(loadImage("graphics/mazes_flashing.png"));

        storeLocalAsset(assets, "app_icon",                        loadImage("graphics/icons/mspacman.png"));
        storeLocalAsset(assets, "logo.midway",                     loadImage("graphics/midway_logo.png"));
        storeLocalAsset(assets, "startpage.image1",                loadImage("graphics/f1.jpg"));
        storeLocalAsset(assets, "startpage.image2",                loadImage("graphics/f2.jpg"));

        storeLocalAsset(assets, "color.game_over_message",         ARCADE_RED);

        Sprite[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        Sprite[] valueSprites = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES);
        for (byte symbol = 0; symbol <= 6; ++symbol) {
            storeLocalAsset(assets, "bonus_symbol_" + symbol, spriteSheet.image(symbolSprites[symbol]));
            storeLocalAsset(assets, "bonus_value_" + symbol,  spriteSheet.image(valueSprites[symbol]));
        }

        storeLocalAsset(assets, "pac.color.head",                  ARCADE_YELLOW);
        storeLocalAsset(assets, "pac.color.eyes",                  Color.grayRgb(33));
        storeLocalAsset(assets, "pac.color.palate",                Color.rgb(240, 180, 160));
        storeLocalAsset(assets, "pac.color.boobs",                 ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        storeLocalAsset(assets, "pac.color.hairbow",               ARCADE_RED);
        storeLocalAsset(assets, "pac.color.hairbow.pearls",        ARCADE_BLUE);

        Sprite[] numberSprites = spriteSheet.spriteSeq(SpriteID.GHOST_NUMBERS);
        storeLocalAsset(assets, "ghost_points_0", spriteSheet.image(numberSprites[0]));
        storeLocalAsset(assets, "ghost_points_1", spriteSheet.image(numberSprites[1]));
        storeLocalAsset(assets, "ghost_points_2", spriteSheet.image(numberSprites[2]));
        storeLocalAsset(assets, "ghost_points_3", spriteSheet.image(numberSprites[3]));

        storeLocalAsset(assets, "ghost.0.color.normal.dress",      ARCADE_RED);
        storeLocalAsset(assets, "ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.0.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.1.color.normal.dress",      ARCADE_PINK);
        storeLocalAsset(assets, "ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.1.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.2.color.normal.dress",      ARCADE_CYAN);
        storeLocalAsset(assets, "ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.2.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.3.color.normal.dress",      ARCADE_ORANGE);
        storeLocalAsset(assets, "ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.3.color.normal.pupils",     ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.color.frightened.dress",    ARCADE_BLUE);
        storeLocalAsset(assets, "ghost.color.frightened.eyeballs", ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.frightened.pupils",   ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.flashing.dress",      ARCADE_WHITE);
        storeLocalAsset(assets, "ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        storeLocalAsset(assets, "ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        storeLocalAsset(assets, "audio.bonus_eaten",             loadAudioClip("sound/Fruit.mp3"));
        storeLocalAsset(assets, "audio.credit",                  loadAudioClip("sound/credit.wav"));
        storeLocalAsset(assets, "audio.extra_life",              loadAudioClip("sound/ExtraLife.mp3"));
        storeLocalAsset(assets, "audio.ghost_eaten",             loadAudioClip("sound/Ghost.mp3"));
        storeLocalAsset(assets, "audio.sweep",                   loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        storeLocalAsset(assets, "audio.bonus_bouncing",          url("sound/Fruit_Bounce.mp3"));
        storeLocalAsset(assets, "audio.game_ready",              url("sound/Start.mp3"));
        storeLocalAsset(assets, "audio.game_over",               url("sound/game-over.mp3"));
        storeLocalAsset(assets, "audio.intermission.1",          url("sound/Act_1_They_Meet.mp3"));
        storeLocalAsset(assets, "audio.intermission.2",          url("sound/Act_2_The_Chase.mp3"));
        storeLocalAsset(assets, "audio.intermission.3",          url("sound/Act_3_Junior.mp3"));
        storeLocalAsset(assets, "audio.level_complete",          url("sound/level-complete.mp3"));
        storeLocalAsset(assets, "audio.pacman_death",            url("sound/Died.mp3"));
        storeLocalAsset(assets, "audio.pacman_munch",            url("sound/munch.wav"));
        storeLocalAsset(assets, "audio.pacman_power",            url("sound/ScaredGhost.mp3"));
        storeLocalAsset(assets, "audio.siren.1",                 url("sound/GhostNoise1.wav"));
        storeLocalAsset(assets, "audio.siren.2",                 url("sound/GhostNoise1.wav"));// TODO
        storeLocalAsset(assets, "audio.siren.3",                 url("sound/GhostNoise1.wav"));// TODO
        storeLocalAsset(assets, "audio.siren.4",                 url("sound/GhostNoise1.wav"));// TODO
        storeLocalAsset(assets, "audio.ghost_returns",           url("sound/GhostEyes.mp3"));
    }

    @Override
    public void unloadAssets(AssetStorage assetStorage) {
        assetStorage.removeAll(ANS + ".");
    }

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacMan_UIConfig.class;
    }

    @Override
    public String assetNamespace() {
        return ANS;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {return spriteSheet;}

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        return WORLD_MAP_COLOR_SCHEMES.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public ArcadeMsPacMan_GameRenderer createGameRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_GameRenderer(spriteSheet, brightMazesSpriteSheet, canvas);
    }

    @Override
    public SpriteAnimationMap<SpriteID> createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<SpriteID> createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationMap(spriteSheet);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        return theAssets().image(ANS + ".bonus_symbol_" + symbol);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        return theAssets().image(ANS + ".bonus_value_" + symbol);
    }

    @Override
    public Node createLivesCounter3D(Model3DRepository model3DRepository) {
        return model3DRepository.createMsPacMan(
            Settings3D.LIVES_COUNTER_3D_SHAPE_SIZE,
            theAssets().color(ANS + ".pac.color.head"),
            theAssets().color(ANS + ".pac.color.eyes"),
            theAssets().color(ANS + ".pac.color.palate"),
            theAssets().color(ANS + ".pac.color.hairbow"),
            theAssets().color(ANS + ".pac.color.hairbow.pearls"),
            theAssets().color(ANS + ".pac.color.boobs")
        );
    }

    @Override
    public PacBase3D createPac3D(Model3DRepository model3DRepository, AnimationManager animationManager, Pac pac) {
        var pac3D = new MsPacMan3D(model3DRepository, animationManager, pac, Settings3D.PAC_3D_SIZE, theAssets(), ANS);
        pac3D.light().setColor(theAssets().color(ANS + ".pac.color.head").desaturate());
        return pac3D;
    }

    // Game scenes

    @Override
    public void createGameScenes() {
        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3());
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
    public GameScene selectGameScene(GameModel game, GameState gameState) {
        String sceneID = switch (gameState) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
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
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }
}