/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ArcadeCommon_BootScene2D;
import de.amr.pacmanfx.arcade.ArcadeCommon_PlayScene2D;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Group;
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
import static de.amr.pacmanfx.arcade.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.theAssets;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements PacManGames_UIConfig, ResourceManager {

    private static final List<WorldMapColorScheme> WORLD_MAP_COLOR_SCHEMES = List.of(
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("47B7FF", "DEDEFF", "FCB5FF", "FFFF00"),
        new WorldMapColorScheme("DE9751", "DEDEFF", "FCB5FF", "FF0000"),
        new WorldMapColorScheme("2121FF", "FFB751", "FCB5FF", "DEDEFF"),
        new WorldMapColorScheme("FFB7FF", "FFFF00", "FCB5FF", "00FFFF"),
        new WorldMapColorScheme("FFB7AE", "FF0000", "FCB5FF", "DEDEFF")
    );

    private final Image appIcon;
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacMan_UIConfig.class;
    }

    public ArcadeMsPacMan_UIConfig(PacManGames_Assets assets) {
        appIcon = loadImage("graphics/icons/mspacman.png");
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(loadImage("graphics/mspacman_spritesheet.png"));

        assets.store("ms_pacman.flashing_mazes",                  loadImage("graphics/mazes_flashing.png"));
        assets.store("ms_pacman.logo.midway",                     loadImage("graphics/midway_logo.png"));
        assets.store("ms_pacman.startpage.image1",                loadImage("graphics/f1.jpg"));
        assets.store("ms_pacman.startpage.image2",                loadImage("graphics/f2.jpg"));

        assets.store("ms_pacman.color.score",                     ARCADE_WHITE);
        assets.store("ms_pacman.color.game_over_message",         ARCADE_RED);

        assets.store("ms_pacman.pac.color.head",                  ARCADE_YELLOW);
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 ARCADE_YELLOW.deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow",               ARCADE_RED);
        assets.store("ms_pacman.pac.color.hairbow.pearls",        ARCADE_BLUE);

        assets.store("ms_pacman.ghost.0.color.normal.dress",      ARCADE_RED);
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman.ghost.1.color.normal.dress",      ARCADE_PINK);
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman.ghost.2.color.normal.dress",      ARCADE_CYAN);
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman.ghost.3.color.normal.dress",      ARCADE_ORANGE);
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     ARCADE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.dress",    ARCADE_BLUE);
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.frightened.pupils",   ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.dress",      ARCADE_WHITE);
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        assets.store("ms_pacman.ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        assets.store("ms_pacman.audio.bonus_eaten",             loadAudioClip("sound/Fruit.mp3"));
        assets.store("ms_pacman.audio.credit",                  loadAudioClip("sound/credit.wav"));
        assets.store("ms_pacman.audio.extra_life",              loadAudioClip("sound/ExtraLife.mp3"));
        assets.store("ms_pacman.audio.ghost_eaten",             loadAudioClip("sound/Ghost.mp3"));
        assets.store("ms_pacman.audio.sweep",                   loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman.audio.bonus_bouncing",          url("sound/Fruit_Bounce.mp3"));
        assets.store("ms_pacman.audio.game_ready",              url("sound/Start.mp3"));
        assets.store("ms_pacman.audio.game_over",               url("sound/game-over.mp3"));
        assets.store("ms_pacman.audio.intermission.1",          url("sound/Act_1_They_Meet.mp3"));
        assets.store("ms_pacman.audio.intermission.2",          url("sound/Act_2_The_Chase.mp3"));
        assets.store("ms_pacman.audio.intermission.3",          url("sound/Act_3_Junior.mp3"));
        assets.store("ms_pacman.audio.level_complete",          url("sound/level-complete.mp3"));
        assets.store("ms_pacman.audio.pacman_death",            url("sound/Died.mp3"));
        assets.store("ms_pacman.audio.pacman_munch",            url("sound/munch.wav"));
        assets.store("ms_pacman.audio.pacman_power",            url("sound/ScaredGhost.mp3"));
        assets.store("ms_pacman.audio.siren.1",                 url("sound/GhostNoise1.wav"));
        assets.store("ms_pacman.audio.siren.2",                 url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.3",                 url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.siren.4",                 url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman.audio.ghost_returns",           url("sound/GhostEyes.mp3"));

        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3());

        // Well, you know...
        var playScene2D = (ArcadeCommon_PlayScene2D) scenesByID.get("PlayScene2D");
        playScene2D.setLivesCounterSprite(ArcadeMsPacMan_SpriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL));
    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public String assetNamespace() {
        return "ms_pacman";
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

    @Override
    public GameScene2D createPiPScene(Canvas canvas) {
        var gameScene = new ArcadeCommon_PlayScene2D();
        gameScene.setGameRenderer(createRenderer(canvas));
        return gameScene;
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        return WORLD_MAP_COLOR_SCHEMES.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public ArcadeMsPacMan_GameRenderer createRenderer(Canvas canvas) {
        return new ArcadeMsPacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public Image createGhostNumberImage(int ghostIndex) {
        RectArea[] sprites = ArcadeMsPacMan_SpriteSheet.sprites(SpriteID.GHOST_NUMBERS);
        return Ufx.subImage(spriteSheet.sourceImage(), sprites[ghostIndex]);
    }

    @Override
    public RectArea createBonusSymbolSprite(byte symbol) {
        return ArcadeMsPacMan_SpriteSheet.sprites(SpriteID.BONUS_SYMBOLS)[symbol];
    }

    @Override
    public RectArea createBonusValueSprite(byte symbol) {
        return ArcadeMsPacMan_SpriteSheet.sprites(SpriteID.BONUS_VALUES)[symbol];
    }

    @Override
    public Node createLivesCounter3D() {
        String namespace = assetNamespace();
        return new Group(
            Model3DRepository.get().createPacShape(
                    LIVES_COUNTER_3D_SIZE,
                    theAssets().color(namespace + ".pac.color.head"),
                    theAssets().color(namespace + ".pac.color.eyes"),
                    theAssets().color(namespace + ".pac.color.palate")
            ),
            Model3DRepository.get().createFemaleBodyParts(LIVES_COUNTER_3D_SIZE,
                    theAssets().color(namespace + ".pac.color.hairbow"),
                    theAssets().color(namespace + ".pac.color.hairbow.pearls"),
                    theAssets().color(namespace + ".pac.color.boobs")
            )
        );
    }

    @Override
    public PacBase3D createPac3D(Pac pac) {
        var pac3D = new MsPacMan3D(pac, PAC_3D_SIZE, theAssets(), assetNamespace());
        pac3D.light().setColor(theAssets().color(assetNamespace() + ".pac.color.head").desaturate());
        return pac3D;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends SpriteSheet> T spriteSheet() {
        return (T) spriteSheet;
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

    @Override
    public SpriteAnimationMap<?> createGhostAnimations(Ghost ghost) {
        return new ArcadeMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality());
    }

    @Override
    public SpriteAnimationMap<?> createPacAnimations(Pac pac) {
        return new ArcadeMsPacMan_PacAnimationMap(spriteSheet);
    }
}