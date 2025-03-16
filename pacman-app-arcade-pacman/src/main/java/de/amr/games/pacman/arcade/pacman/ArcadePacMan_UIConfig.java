/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfiguration;
import de.amr.games.pacman.ui._2d.ArcadeBootScene;
import de.amr.games.pacman.ui._2d.ArcadePlayScene2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
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

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class ArcadePacMan_UIConfig implements GameUIConfiguration {

    private static final WorldMapColorScheme MAP_COLORING = new WorldMapColorScheme("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final Image appIcon;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public ArcadePacMan_UIConfig(AssetStorage assets) {
        scenesByID.put("BootScene",   new ArcadeBootScene());
        scenesByID.put("IntroScene",  new IntroScene());
        scenesByID.put("StartScene",  new StartScene());
        scenesByID.put("PlayScene2D", new ArcadePlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new CutScene1());
        scenesByID.put("CutScene2",   new CutScene2());
        scenesByID.put("CutScene3",   new CutScene3());

        ResourceManager rm = () -> ResourceRoot.class;
        appIcon = rm.loadImage("graphics/icons/pacman.png");
        spriteSheet = new ArcadePacMan_SpriteSheet(rm.loadImage("graphics/pacman_spritesheet.png"));

        assets.store("pacman.flashing_maze",                   rm.loadImage("graphics/maze_flashing.png"));

        assets.store("pacman.startpage.image1",                rm.loadImage("graphics/f1.jpg"));
        assets.store("pacman.startpage.image2",                rm.loadImage("graphics/f2.jpg"));
        assets.store("pacman.startpage.image3",                rm.loadImage("graphics/f3.jpg"));

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
        assets.store("pacman.audio.bonus_eaten",               rm.loadAudioClip("sound/eat_fruit.mp3"));
        assets.store("pacman.audio.credit",                    rm.loadAudioClip("sound/credit.wav"));
        assets.store("pacman.audio.extra_life",                rm.loadAudioClip("sound/extend.mp3"));
        assets.store("pacman.audio.ghost_eaten",               rm.loadAudioClip("sound/eat_ghost.mp3"));
        assets.store("pacman.audio.sweep",                     rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player URL
        assets.store("pacman.audio.game_ready",                rm.url("sound/game_start.mp3"));
        assets.store("pacman.audio.game_over",                 rm.url("sound/common/game-over.mp3"));
        assets.store("pacman.audio.intermission",              rm.url("sound/intermission.mp3"));
        assets.store("pacman.audio.pacman_death",              rm.url("sound/pacman_death.wav"));
        assets.store("pacman.audio.pacman_munch",              rm.url("sound/munch.wav"));
        assets.store("pacman.audio.pacman_power",              rm.url("sound/ghost-turn-to-blue.mp3"));
        assets.store("pacman.audio.level_complete",            rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman.audio.siren.1",                   rm.url("sound/siren_1.mp3"));
        assets.store("pacman.audio.siren.2",                   rm.url("sound/siren_2.mp3"));
        assets.store("pacman.audio.siren.3",                   rm.url("sound/siren_3.mp3"));
        assets.store("pacman.audio.siren.4",                   rm.url("sound/siren_4.mp3"));
        assets.store("pacman.audio.ghost_returns",             rm.url("sound/retreating.mp3"));
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
        assertNotNull(gameScene);
        assertNotNull(sceneID);
        return scenesByID.get(sceneID) == gameScene;
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameScene2D createPiPScene(GameContext context, Canvas canvas) {
        var gameScene = new ArcadePlayScene2D();
        gameScene.setGameContext(context);
        gameScene.setGameRenderer(createRenderer(context.assets(), canvas));
        return gameScene;
    }

    @Override
    public ArcadePacMan_GameRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new ArcadePacMan_GameRenderer(assets, spriteSheet, canvas);
    }

    @Override
    public WorldMapColorScheme worldMapColoring(WorldMap worldMap) {
        return MAP_COLORING;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> "CutScene" + context.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case GameState.TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default -> GlobalProperties3d.PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
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