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
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._2d.ArcadeBootScene;
import de.amr.games.pacman.ui._2d.ArcadePlayScene2D;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.ui._3d.scene3d.GameUIConfiguration3D;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.WorldMapColoring;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ArcadePacMan_GameUIConfig3D implements GameUIConfiguration3D {

    private static final WorldMapColoring MAP_COLORING = new WorldMapColoring("#000000", "#2121ff", "#fcb5ff", "#febdb4");

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public ArcadePacMan_GameUIConfig3D(AssetStorage assets) {
        loadAssets(() -> ResourceRoot.class, assets);
        spriteSheet = new ArcadePacMan_SpriteSheet(assets.get("pacman.spritesheet"));

        setGameScene("BootScene",   new ArcadeBootScene());
        setGameScene("IntroScene",  new IntroScene());
        setGameScene("StartScene",  new StartScene());
        setGameScene("PlayScene2D", new ArcadePlayScene2D());
        setGameScene("PlayScene3D", new PlayScene3D());
        setGameScene("CutScene1",   new CutScene1());
        setGameScene("CutScene2",   new CutScene2());
        setGameScene("CutScene3",   new CutScene3());
    }

    @Override
    public GameVariant gameVariant() {
        return GameVariant.PACMAN;
    }

    @Override
    public String assetNamespace() {
        return "pacman";
    }

    @Override
    public void setGameScene(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene getGameScene(String id) {
        return scenesByID.get(id);
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
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
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
            case GameState.INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case GameState.TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default -> GlobalProperties3d.PY_3D_ENABLED.get() ?  "PlayScene3D" : "PlayScene2D";
        };
        return getGameScene(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    @Override
    public TerrainRenderer3D createTerrainRenderer3D() {
        return new TerrainRenderer3D();
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets) {
        String akp = assetNamespace();
        return PacModel3D.createPacShape(
                assets.get("model3D.pacman"), 10,
                assets.color(akp + ".pac.color.head"),
                assets.color(akp + ".pac.color.eyes"),
                assets.color(akp + ".pac.color.palate")
        );
    }

    private void loadAssets(ResourceManager rm, AssetStorage assets) {
        assets.store("pacman.spritesheet",              rm.loadImage("graphics/pacman_spritesheet.png"));
        assets.store("pacman.flashing_maze",            rm.loadImage("graphics/maze_flashing.png"));

        assets.store("pacman.startpage.image1",         rm.loadImage("graphics/f1.jpg"));
        assets.store("pacman.startpage.image2",         rm.loadImage("graphics/f2.jpg"));
        assets.store("pacman.startpage.image3",         rm.loadImage("graphics/f3.jpg"));

        assets.store("pacman.icon",                     rm.loadImage("graphics/icons/pacman.png"));

        assets.store("pacman.color.game_over_message",         Color.valueOf(Arcade.Palette.RED));

        assets.store("pacman.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("pacman.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("pacman.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("pacman.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("pacman.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

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
}