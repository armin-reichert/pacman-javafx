/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.ResourceRoot;
import de.amr.games.pacman.arcade.pacman.*;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfiguration;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui._3d.scene3d.PlayScene3D;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.WorldMapColoring;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;

public class PacManXXL_PacMan_UIConfig implements GameUIConfiguration {

    private final Image appIcon;
    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManXXL_PacMan_UIConfig(AssetStorage assets) {
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

        assets.store("pacman_xxl.color.game_over_message",         Color.valueOf(Arcade.Palette.RED));

        assets.store("pacman_xxl.pac.color.head",                  Color.valueOf(Arcade.Palette.YELLOW));
        assets.store("pacman_xxl.pac.color.eyes",                  Color.grayRgb(33));
        assets.store("pacman_xxl.pac.color.palate",                Color.rgb(240, 180, 160));

        assets.store("pacman_xxl.ghost.0.color.normal.dress",      Color.valueOf(Arcade.Palette.RED));
        assets.store("pacman_xxl.ghost.0.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.0.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.1.color.normal.dress",      Color.valueOf(Arcade.Palette.PINK));
        assets.store("pacman_xxl.ghost.1.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.1.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.2.color.normal.dress",      Color.valueOf(Arcade.Palette.CYAN));
        assets.store("pacman_xxl.ghost.2.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.2.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.3.color.normal.dress",      Color.valueOf(Arcade.Palette.ORANGE));
        assets.store("pacman_xxl.ghost.3.color.normal.eyeballs",   Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.3.color.normal.pupils",     Color.valueOf(Arcade.Palette.BLUE));

        assets.store("pacman_xxl.ghost.color.frightened.dress",    Color.valueOf(Arcade.Palette.BLUE));
        assets.store("pacman_xxl.ghost.color.frightened.eyeballs", Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman_xxl.ghost.color.frightened.pupils",   Color.valueOf(Arcade.Palette.ROSE));

        assets.store("pacman_xxl.ghost.color.flashing.dress",      Color.valueOf(Arcade.Palette.WHITE));
        assets.store("pacman_xxl.ghost.color.flashing.eyeballs",   Color.valueOf(Arcade.Palette.ROSE));
        assets.store("pacman_xxl.ghost.color.flashing.pupils",     Color.valueOf(Arcade.Palette.RED));

        // Clips
        assets.store("pacman_xxl.audio.bonus_eaten",    rm.loadAudioClip("sound/eat_fruit.mp3"));
        assets.store("pacman_xxl.audio.credit",         rm.loadAudioClip("sound/credit.wav"));
        assets.store("pacman_xxl.audio.extra_life",     rm.loadAudioClip("sound/extend.mp3"));
        assets.store("pacman_xxl.audio.ghost_eaten",    rm.loadAudioClip("sound/eat_ghost.mp3"));
        assets.store("pacman_xxl.audio.sweep",          rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store("pacman_xxl.audio.game_ready",     rm.url("sound/game_start.mp3"));
        assets.store("pacman_xxl.audio.game_over",      rm.url("sound/common/game-over.mp3"));
        assets.store("pacman_xxl.audio.intermission",   rm.url("sound/intermission.mp3"));
        assets.store("pacman_xxl.audio.pacman_death",   rm.url("sound/pacman_death.wav"));
        assets.store("pacman_xxl.audio.pacman_munch",   rm.url("sound/munch.wav"));
        assets.store("pacman_xxl.audio.pacman_power",   rm.url("sound/ghost-turn-to-blue.mp3"));
        assets.store("pacman_xxl.audio.level_complete", rm.url("sound/common/level-complete.mp3"));
        assets.store("pacman_xxl.audio.siren.1",        rm.url("sound/siren_1.mp3"));
        assets.store("pacman_xxl.audio.siren.2",        rm.url("sound/siren_2.mp3"));
        assets.store("pacman_xxl.audio.siren.3",        rm.url("sound/siren_3.mp3"));
        assets.store("pacman_xxl.audio.siren.4",        rm.url("sound/siren_4.mp3"));
        assets.store("pacman_xxl.audio.ghost_returns",  rm.url("sound/retreating.mp3"));

        rm = this::getClass;
        assets.store("pacman_xxl.audio.option.selection_changed",  rm.loadAudioClip("sound/ms-select1.wav"));
        assets.store("pacman_xxl.audio.option.value_changed",      rm.loadAudioClip("sound/ms-select2.wav"));

    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public GameVariant gameVariant() {
        return GameVariant.PACMAN;
    }

    @Override
    public String assetNamespace() {
        return "pacman_xxl";
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
    public VectorGraphicsGameRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new VectorGraphicsGameRenderer(assets, spriteSheet, canvas);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return new WorldMapColoring((Map<String, String>) worldMap.getConfigValue("colorMap"));
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT -> "BootScene";
            case SETTING_OPTIONS -> "StartScene";
            case INTRO -> "IntroScene";
            case INTERMISSION -> "CutScene" + context.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default -> PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
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
        String namespace = assetNamespace();
        return PacModel3D.createPacShape(
            assets.get("model3D.pacman"), 10,
            assets.color(namespace + ".pac.color.head"),
            assets.color(namespace + ".pac.color.eyes"),
            assets.color(namespace + ".pac.color.palate")
        );
    }
}