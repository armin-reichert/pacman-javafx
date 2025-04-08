/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.ms_pacman.*;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameAssets;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfig;
import de.amr.games.pacman.ui._2d.*;
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
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;

public class PacManXXL_MsPacMan_UIConfig implements GameUIConfig {

    private final Image appIcon;
    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManXXL_MsPacMan_UIConfig(GameAssets assets) {
        ResourceManager rm = () -> ArcadeMsPacMan_UIConfig.class;

        appIcon = rm.loadImage("graphics/icons/mspacman.png");
        spriteSheet = new ArcadeMsPacMan_SpriteSheet(rm.loadImage("graphics/mspacman_spritesheet.png"));

        assets.store("ms_pacman_xxl.flashing_mazes",                rm.loadImage("graphics/mazes_flashing.png"));

        assets.store("ms_pacman_xxl.startpage.image1",              rm.loadImage("graphics/f1.jpg"));
        assets.store("ms_pacman_xxl.startpage.image2",              rm.loadImage("graphics/f2.jpg"));

        assets.store("ms_pacman_xxl.logo.midway",                   rm.loadImage("graphics/midway_logo.png"));

        assets.store("ms_pacman_xxl.color.game_over_message",         Color.web(Arcade.Palette.RED));

        assets.store("ms_pacman_xxl.pac.color.head",                  Color.web(Arcade.Palette.YELLOW));
        assets.store("ms_pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("ms_pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));
        assets.store("ms_pacman_xxl.pac.color.boobs",                 Color.web(Arcade.Palette.YELLOW).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("ms_pacman_xxl.pac.color.hairbow",               Color.web(Arcade.Palette.RED));
        assets.store("ms_pacman_xxl.pac.color.hairbow.pearls",        Color.web(Arcade.Palette.BLUE));

        assets.store("ms_pacman_xxl.ghost.0.color.normal.dress",      Color.web(Arcade.Palette.RED));
        assets.store("ms_pacman_xxl.ghost.0.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman_xxl.ghost.0.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman_xxl.ghost.1.color.normal.dress",      Color.web(Arcade.Palette.PINK));
        assets.store("ms_pacman_xxl.ghost.1.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman_xxl.ghost.1.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman_xxl.ghost.2.color.normal.dress",      Color.web(Arcade.Palette.CYAN));
        assets.store("ms_pacman_xxl.ghost.2.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman_xxl.ghost.2.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman_xxl.ghost.3.color.normal.dress",      Color.web(Arcade.Palette.ORANGE));
        assets.store("ms_pacman_xxl.ghost.3.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman_xxl.ghost.3.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman_xxl.ghost.color.frightened.dress",    Color.web(Arcade.Palette.BLUE));
        assets.store("ms_pacman_xxl.ghost.color.frightened.eyeballs", Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman_xxl.ghost.color.frightened.pupils",   Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman_xxl.ghost.color.flashing.dress",      Color.web(Arcade.Palette.WHITE));
        assets.store("ms_pacman_xxl.ghost.color.flashing.eyeballs",   Color.web(Arcade.Palette.ROSE));
        assets.store("ms_pacman_xxl.ghost.color.flashing.pupils",     Color.web(Arcade.Palette.RED));

        // Clips
        assets.store("ms_pacman_xxl.audio.bonus_eaten",               rm.loadAudioClip("sound/Fruit.mp3"));
        assets.store("ms_pacman_xxl.audio.credit",                    rm.loadAudioClip("sound/credit.wav"));
        assets.store("ms_pacman_xxl.audio.extra_life",                rm.loadAudioClip("sound/ExtraLife.mp3"));
        assets.store("ms_pacman_xxl.audio.ghost_eaten",               rm.loadAudioClip("sound/Ghost.mp3"));
        assets.store("ms_pacman_xxl.audio.sweep",                     rm.loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("ms_pacman_xxl.audio.bonus_bouncing",          rm.url("sound/Fruit Bounce.mp3"));
        assets.store("ms_pacman_xxl.audio.game_ready",              rm.url("sound/Start.mp3"));
        assets.store("ms_pacman_xxl.audio.game_over",               rm.url("sound/game-over.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.1",          rm.url("sound/Act_1_They_Meet.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.2",          rm.url("sound/Act_2_The_Chase.mp3"));
        assets.store("ms_pacman_xxl.audio.intermission.3",          rm.url("sound/Act_3_Junior.mp3"));
        assets.store("ms_pacman_xxl.audio.level_complete",          rm.url("sound/level-complete.mp3"));
        assets.store("ms_pacman_xxl.audio.pacman_death",            rm.url("sound/Died.mp3"));
        assets.store("ms_pacman_xxl.audio.pacman_munch",            rm.url("sound/munch.wav"));
        assets.store("ms_pacman_xxl.audio.pacman_power",            rm.url("sound/ScaredGhost.mp3"));
        assets.store("ms_pacman_xxl.audio.siren.1",                 rm.url("sound/GhostNoise1.wav"));
        assets.store("ms_pacman_xxl.audio.siren.2",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.siren.3",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.siren.4",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store("ms_pacman_xxl.audio.ghost_returns",           rm.url("sound/GhostEyes.mp3"));

        rm = this::getClass;
        assets.store("ms_pacman_xxl.audio.option.selection_changed",  rm.loadAudioClip("sound/ms-select1.wav"));
        assets.store("ms_pacman_xxl.audio.option.value_changed",      rm.loadAudioClip("sound/ms-select2.wav"));

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
        return "ms_pacman_xxl";
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
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public GameRenderer createRenderer(Canvas canvas) {
        return new VectorGraphicsGameRenderer(spriteSheet, canvas);
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
    public GameScene selectGameScene() {
        String sceneID = switch (THE_GAME_CONTROLLER.state()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + THE_GAME_CONTROLLER.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + THE_GAME_CONTROLLER.state().<Integer>getProperty("intermissionTestNumber");
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
