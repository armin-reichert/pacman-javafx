/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.*;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfig;
import de.amr.games.pacman.ui._2d.*;
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

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class PacManXXL_PacMan_UIConfig implements GameUIConfig {

    private final Image appIcon;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManXXL_PacMan_UIConfig() {
        ResourceManager rm = () -> ArcadePacMan_UIConfig.class;

        appIcon = rm.loadImage("graphics/icons/pacman.png");
        spriteSheet = new ArcadePacMan_SpriteSheet(rm.loadImage("graphics/pacman_spritesheet.png"));

        THE_UI.assets().store("pacman_xxl.color.game_over_message",         Color.web(Arcade.Palette.RED));

        THE_UI.assets().store("pacman_xxl.pac.color.head",                  Color.web(Arcade.Palette.YELLOW));
        THE_UI.assets().store("pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        THE_UI.assets().store("pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));

        THE_UI.assets().store("pacman_xxl.ghost.0.color.normal.dress",      Color.web(Arcade.Palette.RED));
        THE_UI.assets().store("pacman_xxl.ghost.0.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_UI.assets().store("pacman_xxl.ghost.0.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));

        THE_UI.assets().store("pacman_xxl.ghost.1.color.normal.dress",      Color.web(Arcade.Palette.PINK));
        THE_UI.assets().store("pacman_xxl.ghost.1.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_UI.assets().store("pacman_xxl.ghost.1.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));

        THE_UI.assets().store("pacman_xxl.ghost.2.color.normal.dress",      Color.web(Arcade.Palette.CYAN));
        THE_UI.assets().store("pacman_xxl.ghost.2.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_UI.assets().store("pacman_xxl.ghost.2.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));

        THE_UI.assets().store("pacman_xxl.ghost.3.color.normal.dress",      Color.web(Arcade.Palette.ORANGE));
        THE_UI.assets().store("pacman_xxl.ghost.3.color.normal.eyeballs",   Color.web(Arcade.Palette.WHITE));
        THE_UI.assets().store("pacman_xxl.ghost.3.color.normal.pupils",     Color.web(Arcade.Palette.BLUE));

        THE_UI.assets().store("pacman_xxl.ghost.color.frightened.dress",    Color.web(Arcade.Palette.BLUE));
        THE_UI.assets().store("pacman_xxl.ghost.color.frightened.eyeballs", Color.web(Arcade.Palette.ROSE));
        THE_UI.assets().store("pacman_xxl.ghost.color.frightened.pupils",   Color.web(Arcade.Palette.ROSE));

        THE_UI.assets().store("pacman_xxl.ghost.color.flashing.dress",      Color.web(Arcade.Palette.WHITE));
        THE_UI.assets().store("pacman_xxl.ghost.color.flashing.eyeballs",   Color.web(Arcade.Palette.ROSE));
        THE_UI.assets().store("pacman_xxl.ghost.color.flashing.pupils",     Color.web(Arcade.Palette.RED));

        // Clips
        THE_UI.assets().store("pacman_xxl.audio.bonus_eaten",    rm.loadAudioClip("sound/eat_fruit.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.credit",         rm.loadAudioClip("sound/credit.wav"));
        THE_UI.assets().store("pacman_xxl.audio.extra_life",     rm.loadAudioClip("sound/extend.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.ghost_eaten",    rm.loadAudioClip("sound/eat_ghost.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.sweep",          rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        THE_UI.assets().store("pacman_xxl.audio.game_ready",     rm.url("sound/game_start.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.game_over",      rm.url("sound/common/game-over.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.intermission",   rm.url("sound/intermission.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.pacman_death",   rm.url("sound/pacman_death.wav"));
        THE_UI.assets().store("pacman_xxl.audio.pacman_munch",   rm.url("sound/munch.wav"));
        THE_UI.assets().store("pacman_xxl.audio.pacman_power",   rm.url("sound/ghost-turn-to-blue.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.level_complete", rm.url("sound/common/level-complete.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.siren.1",        rm.url("sound/siren_1.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.siren.2",        rm.url("sound/siren_2.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.siren.3",        rm.url("sound/siren_3.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.siren.4",        rm.url("sound/siren_4.mp3"));
        THE_UI.assets().store("pacman_xxl.audio.ghost_returns",  rm.url("sound/retreating.mp3"));

        rm = this::getClass;
        THE_UI.assets().store("pacman_xxl.audio.option.selection_changed",  rm.loadAudioClip("sound/ms-select1.wav"));
        THE_UI.assets().store("pacman_xxl.audio.option.value_changed",      rm.loadAudioClip("sound/ms-select2.wav"));

        scenesByID.put("BootScene",   new ArcadeBootScene2D());
        scenesByID.put("IntroScene",  new IntroScene());
        scenesByID.put("StartScene",  new StartScene());
        scenesByID.put("PlayScene2D", new ArcadePlayScene2D());
        scenesByID.put("PlayScene3D", new PlayScene3D());
        scenesByID.put("CutScene1",   new CutScene1());
        scenesByID.put("CutScene2",   new CutScene2());
        scenesByID.put("CutScene3",   new CutScene3());
    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public String assetNamespace() {
        return "pacman_xxl";
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
    public VectorGraphicsGameRenderer createRenderer(Canvas canvas) {
        return new VectorGraphicsGameRenderer(spriteSheet, canvas);
    }

    @Override
    public WorldMapColorScheme worldMapColoring(WorldMap worldMap) {
        Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
        return new WorldMapColorScheme(
            colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("door"), colorMap.get("pellet"));
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene() {
        String sceneID = switch (THE_GAME_CONTROLLER.state()) {
            case BOOT -> "BootScene";
            case SETTING_OPTIONS -> "StartScene";
            case INTRO -> "IntroScene";
            case INTERMISSION -> "CutScene" + THE_GAME_CONTROLLER.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + THE_GAME_CONTROLLER.state().<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
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
            THE_UI.assets().get("model3D.pacman"), size,
            THE_UI.assets().color(namespace + ".pac.color.head"),
            THE_UI.assets().color(namespace + ".pac.color.eyes"),
            THE_UI.assets().color(namespace + ".pac.color.palate")
        );
    }
}