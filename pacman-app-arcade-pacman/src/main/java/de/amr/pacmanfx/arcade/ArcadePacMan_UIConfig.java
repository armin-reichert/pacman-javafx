/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui.PacManGames_UIConfiguration;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements PacManGames_UIConfiguration, ResourceManager {

    public static final Vector2i ARCADE_MAP_SIZE_IN_TILES  = new Vector2i(28, 36);
    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288);

    public static final String ANIM_BIG_PAC_MAN               = "big_pac_man";
    public static final String ANIM_BLINKY_DAMAGED            = "blinky_damaged";
    public static final String ANIM_BLINKY_PATCHED            = "blinky_patched";
    public static final String ANIM_BLINKY_NAIL_DRESS_RAPTURE = "blinky_nail_dress_rapture";
    public static final String ANIM_BLINKY_NAKED              = "blinky_naked";

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final Image appIcon;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    public ArcadePacMan_UIConfig(PacManGames_Assets assets) {
        appIcon = loadImage("graphics/icons/pacman.png");
        spriteSheet = new ArcadePacMan_SpriteSheet(loadImage("graphics/pacman_spritesheet.png"));

        assets.store("pacman.flashing_maze",                   loadImage("graphics/maze_flashing.png"));

        assets.store("pacman.startpage.image1",                loadImage("graphics/f1.jpg"));
        assets.store("pacman.startpage.image2",                loadImage("graphics/f2.jpg"));
        assets.store("pacman.startpage.image3",                loadImage("graphics/f3.jpg"));

        assets.store("pacman.color.score",                     ARCADE_WHITE);
        assets.store("pacman.color.game_over_message",         ARCADE_RED);

        assets.store("pacman.pac.color.head",                  ARCADE_YELLOW);
        assets.store("pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress",      ARCADE_RED);
        assets.store("pacman.ghost.0.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("pacman.ghost.0.color.normal.pupils",     ARCADE_BLUE);
        assets.store("pacman.ghost.1.color.normal.dress",      ARCADE_PINK);
        assets.store("pacman.ghost.1.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("pacman.ghost.1.color.normal.pupils",     ARCADE_BLUE);
        assets.store("pacman.ghost.2.color.normal.dress",      ARCADE_CYAN);
        assets.store("pacman.ghost.2.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("pacman.ghost.2.color.normal.pupils",     ARCADE_BLUE);
        assets.store("pacman.ghost.3.color.normal.dress",      ARCADE_ORANGE);
        assets.store("pacman.ghost.3.color.normal.eyeballs",   ARCADE_WHITE);
        assets.store("pacman.ghost.3.color.normal.pupils",     ARCADE_BLUE);
        assets.store("pacman.ghost.color.frightened.dress",    ARCADE_BLUE);
        assets.store("pacman.ghost.color.frightened.eyeballs", ARCADE_ROSE);
        assets.store("pacman.ghost.color.frightened.pupils",   ARCADE_ROSE);
        assets.store("pacman.ghost.color.flashing.dress",      ARCADE_WHITE);
        assets.store("pacman.ghost.color.flashing.eyeballs",   ARCADE_ROSE);
        assets.store("pacman.ghost.color.flashing.pupils",     ARCADE_RED);

        // Clips
        assets.store("pacman.audio.bonus_eaten",               loadAudioClip("sound/eat_fruit.mp3"));
        assets.store("pacman.audio.credit",                    loadAudioClip("sound/credit.wav"));
        assets.store("pacman.audio.extra_life",                loadAudioClip("sound/extend.mp3"));
        assets.store("pacman.audio.ghost_eaten",               loadAudioClip("sound/eat_ghost.mp3"));
        assets.store("pacman.audio.sweep",                     loadAudioClip("sound/common/sweep.mp3"));

        // Media player URL
        assets.store("pacman.audio.game_ready",                url("sound/game_start.mp3"));
        assets.store("pacman.audio.game_over",                 url("sound/common/game-over.mp3"));
        assets.store("pacman.audio.intermission",              url("sound/intermission.mp3"));
        assets.store("pacman.audio.pacman_death",              url("sound/pacman_death.wav"));
        assets.store("pacman.audio.pacman_munch",              url("sound/munch.wav"));
        assets.store("pacman.audio.pacman_power",              url("sound/ghost-turn-to-blue.mp3"));
        assets.store("pacman.audio.level_complete",            url("sound/common/level-complete.mp3"));
        assets.store("pacman.audio.siren.1",                   url("sound/siren_1.mp3"));
        assets.store("pacman.audio.siren.2",                   url("sound/siren_2.mp3"));
        assets.store("pacman.audio.siren.3",                   url("sound/siren_3.mp3"));
        assets.store("pacman.audio.siren.4",                   url("sound/siren_4.mp3"));
        assets.store("pacman.audio.ghost_returns",             url("sound/retreating.mp3"));

        scenesByID.put("BootScene",   new ArcadeCommon_BootScene2D());
        scenesByID.put("IntroScene",  new ArcadePacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadePacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadeCommon_PlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadePacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadePacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadePacMan_CutScene3());
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
                int cutSceneNumber = theGame().cutSceneNumber(levelNumber);
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + game.cutSceneNumber(levelNumber);
            }
            case GameState.TESTING_CUT_SCENES -> {
                if (optGameLevel().isEmpty()) {
                    throw new IllegalStateException("Cannot determine cut scene, no game level available");
                }
                int levelNumber = theGameLevel().number();
                int cutSceneNumber = gameState.<Integer>getProperty("intermissionTestNumber");
                if (cutSceneNumber == 0) {
                    throw new IllegalStateException("Cannot determine cut scene after level %d".formatted(levelNumber));
                }
                yield "CutScene" + cutSceneNumber;
            }
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public String assetNamespace() {
        return "pacman";
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
    public ArcadePacMan_GameRenderer createRenderer(Canvas canvas) {
        return new ArcadePacMan_GameRenderer(spriteSheet, canvas);
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        return MAP_COLORING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameSpriteSheet> T spriteSheet() {
        return (T) spriteSheet;
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new ArcadePacMan_PacAnimationMap(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new ArcadePacMan_GhostAnimationMap(spriteSheet, ghost.personality())));
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets, double size) {
        String namespace = assetNamespace();
        return Model3DRepository.get().createPacShape(
                size,
                assets.color(namespace + ".pac.color.head"),
                assets.color(namespace + ".pac.color.eyes"),
                assets.color(namespace + ".pac.color.palate")
        );
    }
}