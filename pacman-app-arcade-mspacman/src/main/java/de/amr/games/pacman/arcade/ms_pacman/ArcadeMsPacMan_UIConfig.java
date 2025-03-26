/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfiguration;
import de.amr.games.pacman.ui._2d.ArcadeBootScene;
import de.amr.games.pacman.ui._2d.ArcadePlayScene2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
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

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.GameUI.THE_ASSETS;

public class ArcadeMsPacMan_UIConfig implements GameUIConfiguration {

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

    public ArcadeMsPacMan_UIConfig() {
        scenesByID.put("BootScene",   new ArcadeBootScene());
        scenesByID.put("IntroScene",  new IntroScene());
        scenesByID.put("StartScene",  new StartScene());
        scenesByID.put("PlayScene2D", new ArcadePlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new CutScene1());
        scenesByID.put("CutScene2",   new CutScene2());
        scenesByID.put("CutScene3",   new CutScene3());

        ResourceManager rm = () -> ArcadeMsPacMan_UIConfig.class;

        appIcon = rm.loadImage("graphics/icons/mspacman.png");
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(rm.loadImage("graphics/mspacman_spritesheet.png"));

        THE_ASSETS.store("ms_pacman.flashing_mazes",                  rm.loadImage("graphics/mazes_flashing.png"));
        THE_ASSETS.store("ms_pacman.logo.midway",                     rm.loadImage("graphics/midway_logo.png"));
        THE_ASSETS.store("ms_pacman.startpage.image1",                rm.loadImage("graphics/f1.jpg"));
        THE_ASSETS.store("ms_pacman.startpage.image2",                rm.loadImage("graphics/f2.jpg"));

        THE_ASSETS.store("ms_pacman.color.game_over_message",         Color.web(Arcade.Palette.RED));

        THE_ASSETS.store("ms_pacman.pac.color.head",                  Color.web(Arcade.Palette.YELLOW));
        THE_ASSETS.store("ms_pacman.pac.color.eyes",                  Color.grayRgb(33));
        THE_ASSETS.store("ms_pacman.pac.color.palate",                Color.rgb(240, 180, 160));
        THE_ASSETS.store("ms_pacman.pac.color.boobs",                 Color.web(Arcade.Palette.YELLOW).deriveColor(0, 1.0, 0.96, 1.0));
        THE_ASSETS.store("ms_pacman.pac.color.hairbow",               Color.web(Arcade.Palette.RED));
        THE_ASSETS.store("ms_pacman.pac.color.hairbow.pearls",        Color.web(Arcade.Palette.BLUE));

        THE_ASSETS.store("ms_pacman.ghost.0.color.normal.dress",      Color.web(Arcade.Palette.RED));
        THE_ASSETS.store("ms_pacman.ghost.0.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_ASSETS.store("ms_pacman.ghost.0.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        THE_ASSETS.store("ms_pacman.ghost.1.color.normal.dress",      Color.web(Arcade.Palette.PINK));
        THE_ASSETS.store("ms_pacman.ghost.1.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_ASSETS.store("ms_pacman.ghost.1.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        THE_ASSETS.store("ms_pacman.ghost.2.color.normal.dress",      Color.web(Arcade.Palette.CYAN));
        THE_ASSETS.store("ms_pacman.ghost.2.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_ASSETS.store("ms_pacman.ghost.2.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        THE_ASSETS.store("ms_pacman.ghost.3.color.normal.dress",      Color.web(Arcade.Palette.ORANGE));
        THE_ASSETS.store("ms_pacman.ghost.3.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_ASSETS.store("ms_pacman.ghost.3.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        THE_ASSETS.store("ms_pacman.ghost.color.frightened.dress",    Color.web(Arcade.Palette.BLUE));
        THE_ASSETS.store("ms_pacman.ghost.color.frightened.eyeballs", Color.web(Arcade.Palette.ROSE));
        THE_ASSETS.store("ms_pacman.ghost.color.frightened.pupils",   Color.web(Arcade.Palette.ROSE));
        THE_ASSETS.store("ms_pacman.ghost.color.flashing.dress",      Color.web(Arcade.Palette.WHITE));
        THE_ASSETS.store("ms_pacman.ghost.color.flashing.eyeballs",   Color.web(Arcade.Palette.ROSE));
        THE_ASSETS.store("ms_pacman.ghost.color.flashing.pupils",     Color.web(Arcade.Palette.RED));

        // Clips
        THE_ASSETS.store("ms_pacman.audio.bonus_eaten",             rm.loadAudioClip("sound/Fruit.mp3"));
        THE_ASSETS.store("ms_pacman.audio.credit",                  rm.loadAudioClip("sound/credit.wav"));
        THE_ASSETS.store("ms_pacman.audio.extra_life",              rm.loadAudioClip("sound/ExtraLife.mp3"));
        THE_ASSETS.store("ms_pacman.audio.ghost_eaten",             rm.loadAudioClip("sound/Ghost.mp3"));
        THE_ASSETS.store("ms_pacman.audio.sweep",                   rm.loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        THE_ASSETS.store("ms_pacman.audio.bonus_bouncing",          rm.url("sound/Fruit Bounce.mp3"));
        THE_ASSETS.store("ms_pacman.audio.game_ready",              rm.url("sound/Start.mp3"));
        THE_ASSETS.store("ms_pacman.audio.game_over",               rm.url("sound/game-over.mp3"));
        THE_ASSETS.store("ms_pacman.audio.intermission.1",          rm.url("sound/Act_1_They_Meet.mp3"));
        THE_ASSETS.store("ms_pacman.audio.intermission.2",          rm.url("sound/Act_2_The_Chase.mp3"));
        THE_ASSETS.store("ms_pacman.audio.intermission.3",          rm.url("sound/Act_3_Junior.mp3"));
        THE_ASSETS.store("ms_pacman.audio.level_complete",          rm.url("sound/level-complete.mp3"));
        THE_ASSETS.store("ms_pacman.audio.pacman_death",            rm.url("sound/Died.mp3"));
        THE_ASSETS.store("ms_pacman.audio.pacman_munch",            rm.url("sound/munch.wav"));
        THE_ASSETS.store("ms_pacman.audio.pacman_power",            rm.url("sound/ScaredGhost.mp3"));
        THE_ASSETS.store("ms_pacman.audio.siren.1",                 rm.url("sound/GhostNoise1.wav"));
        THE_ASSETS.store("ms_pacman.audio.siren.2",                 rm.url("sound/GhostNoise1.wav"));// TODO
        THE_ASSETS.store("ms_pacman.audio.siren.3",                 rm.url("sound/GhostNoise1.wav"));// TODO
        THE_ASSETS.store("ms_pacman.audio.siren.4",                 rm.url("sound/GhostNoise1.wav"));// TODO
        THE_ASSETS.store("ms_pacman.audio.ghost_returns",           rm.url("sound/GhostEyes.mp3"));

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
        assertNotNull(gameScene);
        assertNotNull(sceneID);
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
    public WorldMapColorScheme worldMapColoring(WorldMap worldMap) {
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
                THE_ASSETS.get("model3D.pacman"), size,
                THE_ASSETS.color(namespace + ".pac.color.head"),
                THE_ASSETS.color(namespace + ".pac.color.eyes"),
                THE_ASSETS.color(namespace + ".pac.color.palate")
            ),
            PacModel3D.createFemaleParts(size,
                THE_ASSETS.color(namespace + ".pac.color.hairbow"),
                THE_ASSETS.color(namespace + ".pac.color.hairbow.pearls"),
                THE_ASSETS.color(namespace + ".pac.color.boobs")
            )
        );
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene() {
        String sceneID = switch (THE_GAME_CONTROLLER.state()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + THE_GAME_CONTROLLER.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + THE_GAME_CONTROLLER.state().<Integer>getProperty("intermissionTestNumber");
            default                 -> GlobalProperties3d.PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}