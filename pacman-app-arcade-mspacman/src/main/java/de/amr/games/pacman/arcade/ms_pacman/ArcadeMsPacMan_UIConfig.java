/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameAssets;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfig;
import de.amr.games.pacman.ui._2d.ArcadeBootScene2D;
import de.amr.games.pacman.ui._2d.ArcadePlayScene2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._3d.PlayScene3D;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.WorldMapColorScheme;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_UIConfig implements GameUIConfig, ResourceManager {

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

    public ArcadeMsPacMan_UIConfig(GameAssets assets) {
        appIcon = loadImage("graphics/icons/mspacman.png");
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(loadImage("graphics/mspacman_spritesheet.png"));

        assets.store("ms_pacman.flashing_mazes",                  loadImage("graphics/mazes_flashing.png"));
        assets.store("ms_pacman.logo.midway",                     loadImage("graphics/midway_logo.png"));
        assets.store("ms_pacman.startpage.image1",                loadImage("graphics/f1.jpg"));
        assets.store("ms_pacman.startpage.image2",                loadImage("graphics/f2.jpg"));

        assets.store("ms_pacman.color.game_over_message",         Color.web(Arcade.Palette.RED));

        assets.store("ms_pacman.pac.color.head",                  Color.web(Arcade.Palette.YELLOW));
        assets.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman.pac.color.boobs",                 Color.web(Arcade.Palette.YELLOW).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman.pac.color.hairbow",               Color.web(Arcade.Palette.RED));
        assets.store("ms_pacman.pac.color.hairbow.pearls",        Color.web(Arcade.Palette.BLUE));

        assets.store("ms_pacman.ghost.0.color.normal.dress",      Color.web(Arcade.Palette.RED));
        assets.store("ms_pacman.ghost.0.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.0.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.1.color.normal.dress",      Color.web(Arcade.Palette.PINK));
        assets.store("ms_pacman.ghost.1.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.1.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.2.color.normal.dress",      Color.web(Arcade.Palette.CYAN));
        assets.store("ms_pacman.ghost.2.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.2.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.3.color.normal.dress",      Color.web(Arcade.Palette.ORANGE));
        assets.store("ms_pacman.ghost.3.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.3.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.dress",    Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman.ghost.color.frightened.eyeballs", Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.frightened.pupils",   Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.dress",      Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman.ghost.color.flashing.eyeballs",   Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman.ghost.color.flashing.pupils",     Color.web(Arcade.Palette.RED));

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

        scenesByID.put("BootScene",   new ArcadeBootScene2D());
        scenesByID.put("IntroScene",  new ArcadeMsPacMan_IntroScene());
        scenesByID.put("StartScene",  new ArcadeMsPacMan_StartScene());
        scenesByID.put("PlayScene2D", new ArcadePlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new ArcadeMsPacMan_CutScene1());
        scenesByID.put("CutScene2",   new ArcadeMsPacMan_CutScene2());
        scenesByID.put("CutScene3",   new ArcadeMsPacMan_CutScene3());
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
        var gameScene = new ArcadePlayScene2D();
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
    public Node createLivesCounterShape(AssetStorage assets, double size) {
        String namespace = assetNamespace();
        return new Group(
            PacModel3D.createPacShape(
                assets.get("model3D.pacman"), size,
                assets.color(namespace + ".pac.color.head"),
                assets.color(namespace + ".pac.color.eyes"),
                assets.color(namespace + ".pac.color.palate")
            ),
            PacModel3D.createFemaleParts(size,
                assets.color(namespace + ".pac.color.hairbow"),
                assets.color(namespace + ".pac.color.hairbow.pearls"),
                assets.color(namespace + ".pac.color.boobs")
            )
        );
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameController gameController) {
        String sceneID = switch (gameController.state()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + gameController.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + gameController.state().<Integer>getProperty("intermissionTestNumber");
            default                 -> PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}