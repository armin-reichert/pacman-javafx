/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameAssets;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfig;
import de.amr.games.pacman.ui._2d.ArcadeBootScene2D;
import de.amr.games.pacman.ui._2d.ArcadePlayScene2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.WorldMapColorScheme;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_UIConfig implements GameUIConfig, ResourceManager {

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final Image appIcon;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return ArcadePacMan_UIConfig.class;
    }

    public ArcadePacMan_UIConfig(GameAssets assets) {
        appIcon = loadImage("graphics/icons/pacman.png");
        spriteSheet = new ArcadePacMan_SpriteSheet(loadImage("graphics/pacman_spritesheet.png"));

        assets.store("pacman.flashing_maze",                   loadImage("graphics/maze_flashing.png"));

        assets.store("pacman.startpage.image1",                loadImage("graphics/f1.jpg"));
        assets.store("pacman.startpage.image2",                loadImage("graphics/f2.jpg"));
        assets.store("pacman.startpage.image3",                loadImage("graphics/f3.jpg"));

        assets.store("pacman.color.game_over_message",         Color.web(Arcade.Palette.RED));

        assets.store("pacman.pac.color.head",                  Color.web(Arcade.Palette.YELLOW));
        assets.store("pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress",      Color.web(Arcade.Palette.RED));
        assets.store("pacman.ghost.0.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.0.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.1.color.normal.dress",      Color.web(Arcade.Palette.PINK));
        assets.store("pacman.ghost.1.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.1.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.2.color.normal.dress",      Color.web(Arcade.Palette.CYAN));
        assets.store("pacman.ghost.2.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.2.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.3.color.normal.dress",      Color.web(Arcade.Palette.ORANGE));
        assets.store("pacman.ghost.3.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.3.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.dress",    Color.web(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.eyeballs", Color.web(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.frightened.pupils",   Color.web(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.dress",      Color.web(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.color.flashing.eyeballs",   Color.web(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.pupils",     Color.web(Arcade.Palette.RED));

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

        scenesByID.put("BootScene",   new ArcadeBootScene2D());
        scenesByID.put("IntroScene",  new ArcadePacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadePacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadePlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadePacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadePacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadePacMan_CutScene3());
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
        var gameScene = new ArcadePlayScene2D();
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
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameController gameController) {
        String sceneID = switch (gameController.state()) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> "CutScene" + gameController.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case GameState.TESTING_CUT_SCENES -> "CutScene" + gameController.state().<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets, double size) {
        String namespace = assetNamespace();
        return PacModel3D.createPacShape(
                assets.get("model3D.pacman"), size,
                assets.color(namespace + ".pac.color.head"),
                assets.color(namespace + ".pac.color.eyes"),
                assets.color(namespace + ".pac.color.palate")
        );
    }
}