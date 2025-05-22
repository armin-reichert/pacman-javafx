/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.GameAssets;
import de.amr.pacmanfx.ui.GameUIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.PacManGamesEnv.PY_3D_ENABLED;
import static de.amr.pacmanfx.ui.PacManGamesEnv.PY_CANVAS_BG_COLOR;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_UIConfig implements GameUIConfig, ResourceManager {

    public static final BooleanProperty PY_TENGEN_JOYPAD_BINDINGS_DISPLAYED = new SimpleBooleanProperty(false);
    public static final ObjectProperty<SceneDisplayMode> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    public static Color nesPaletteColor(int index) {
        return Color.web(NES_Palette.color(index));
    }

    public static final Vector2i NES_TILES = new Vector2i(32, 30);
    public static final Vector2i NES_SIZE = NES_TILES.scaled(TS);

    private final Image appIcon;
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final MapRepository mapRepository;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    @Override
    public Class<?> resourceRootClass() {
        return TengenMsPacMan_UIConfig.class;
    }

    public TengenMsPacMan_UIConfig(GameAssets assets) {
        appIcon = loadImage("graphics/icons/mspacman.png");
        spriteSheet = new TengenMsPacMan_SpriteSheet(loadImage("graphics/spritesheet.png"));
        mapRepository = new MapRepository(
            loadImage("graphics/arcade_mazes.png"),
            loadImage("graphics/non_arcade_mazes.png")
        );

        assets.store("tengen.startpage.image1",                 loadImage("graphics/f1.png"));
        assets.store("tengen.startpage.image2",                 loadImage("graphics/f2.png"));

        assets.store("tengen.color.score",                      nesPaletteColor(0x20));
        assets.store("tengen.color.game_over_message",          nesPaletteColor(0x11));
        assets.store("tengen.color.ready_message",              nesPaletteColor(0x28));

        assets.store("tengen.pac.color.head",                   nesPaletteColor(0x28));
        assets.store("tengen.pac.color.eyes",                   nesPaletteColor(0x02));
        assets.store("tengen.pac.color.palate",                 nesPaletteColor(0x2d));
        assets.store("tengen.pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        assets.store("tengen.pac.color.hairbow",                nesPaletteColor(0x05));
        assets.store("tengen.pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        assets.store("tengen.ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        assets.store("tengen.ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store("tengen.ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        assets.store("tengen.ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        assets.store("tengen.ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store("tengen.ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store("tengen.ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        assets.store("tengen.ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store("tengen.ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        assets.store("tengen.ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        assets.store("tengen.ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        assets.store("tengen.ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        assets.store("tengen.ghost.color.frightened.dress",     nesPaletteColor(0x01));
        assets.store("tengen.ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        assets.store("tengen.ghost.color.flashing.dress",       nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        assets.store("tengen.ghost.color.flashing.pupils",      nesPaletteColor(0x20));
        assets.store("tengen.audio.option.selection_changed",   loadAudioClip("sound/ms-select1.wav"));
        assets.store("tengen.audio.option.value_changed",       loadAudioClip("sound/ms-select2.wav"));

        assets.store("tengen.audio.bonus_eaten",                loadAudioClip("sound/ms-fruit.wav"));
        assets.store("tengen.audio.extra_life",                 loadAudioClip("sound/ms-extralife.wav"));
        assets.store("tengen.audio.ghost_eaten",                loadAudioClip("sound/ms-ghosteat.wav"));

        assets.store("tengen.audio.intermission.4.junior.1",    loadAudioClip("sound/ms-theend1.wav"));
        assets.store("tengen.audio.intermission.4.junior.2",    loadAudioClip("sound/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        assets.store("tengen.audio.level_complete",             url("sound/common/level-complete.mp3"));
        assets.store("tengen.audio.sweep",                      loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store("tengen.audio.game_ready",                 url("sound/ms-start.wav"));
        assets.store("tengen.audio.intermission.1",             url("sound/theymeet.wav"));
        assets.store("tengen.audio.intermission.2",             url("sound/thechase.wav"));
        assets.store("tengen.audio.intermission.3",             url("sound/junior.wav"));
        assets.store("tengen.audio.intermission.4",             url("sound/theend.wav"));
        assets.store("tengen.audio.pacman_death",               url("sound/ms-death.wav"));
        assets.store("tengen.audio.pacman_munch",               url("sound/ms-dot.wav"));
        assets.store("tengen.audio.pacman_power",               url("sound/ms-power.wav"));
        assets.store("tengen.audio.siren.1",                    url("sound/ms-siren1.wav"));
        assets.store("tengen.audio.siren.2",                    url("sound/ms-siren2.wav"));
        assets.store("tengen.audio.siren.3",                    url("sound/ms-siren2.wav"));
        assets.store("tengen.audio.siren.4",                    url("sound/ms-siren2.wav"));
        assets.store("tengen.audio.ghost_returns",              url("sound/ms-eyes.wav"));
        assets.store("tengen.audio.bonus_bouncing",             url("sound/fruitbounce.wav"));

        scenesByID.put("BootScene",      new TengenMsPacMan_BootScene());
        scenesByID.put("IntroScene",     new TengenMsPacMan_IntroScene());
        scenesByID.put("StartScene",     new TengenMsPacMan_OptionsScene());
        scenesByID.put("ShowingCredits", new TengenMsPacMan_CreditsScene());
        scenesByID.put("PlayScene2D",    new TengenMsPacMan_PlayScene2D());
        scenesByID.put("PlayScene3D",    new TengenMsPacMan_PlayScene3D());
        scenesByID.put("CutScene1",      new TengenMsPacMan_CutScene1());
        scenesByID.put("CutScene2",      new TengenMsPacMan_CutScene2());
        scenesByID.put("CutScene3",      new TengenMsPacMan_CutScene3());
        scenesByID.put("CutScene4",      new TengenMsPacMan_CutScene4());

        //TODO where is the best place to do that?
        var playScene2D = (TengenMsPacMan_PlayScene2D) scenesByID.get("PlayScene2D");
        playScene2D.displayModeProperty().bind(PY_TENGEN_PLAY_SCENE_DISPLAY_MODE);
    }

    @Override
    public Image appIcon() {
        return appIcon;
    }

    @Override
    public String assetNamespace() {
        return "tengen";
    }

    @Override
    public GameScene selectGameScene(GameModel game, GameState gameState) {
        String sceneID = switch (gameState) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + game.level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + gameState.<Integer>getProperty("intermissionTestNumber");
            default                 -> PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
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
    public GameScene2D createPiPScene(Canvas canvasNotUsed) {
        var gameScene = new TengenMsPacMan_PiPScene();
        gameScene.setGameRenderer(createRenderer(gameScene.canvas()));
        return gameScene;
    }

    @Override
    public boolean isGameCanvasDecorated() { return false; }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameSpriteSheet> T spriteSheet() {
        return (T) spriteSheet;
    }

    @Override
    public TengenMsPacMan_Renderer2D createRenderer(Canvas canvas) {
        var renderer = new TengenMsPacMan_Renderer2D(spriteSheet, mapRepository, canvas);
        renderer.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
        return renderer;
    }

    @Override
    public WorldMapColorScheme worldMapColorScheme(WorldMap worldMap) {
        NES_ColorScheme colorScheme = worldMap.getConfigValue("nesColorScheme");
        return new WorldMapColorScheme(
            colorScheme.fillColor(), colorScheme.strokeColor(), colorScheme.strokeColor(), colorScheme.pelletColor());
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new TengenMsPacMan_PacAnimationMap(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new TengenMsPacMan_GhostAnimationMap(spriteSheet, ghost.personality())));
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets, double size) {
        String namespace = assetNamespace();
        return new Group(
            Model3DRepository.get().createPacShape(
                size,
                assets.color(namespace + ".pac.color.head"),
                assets.color(namespace + ".pac.color.eyes"),
                assets.color(namespace + ".pac.color.palate")
            ),
            Model3DRepository.get().createFemaleBodyParts(size,
                assets.color(namespace + ".pac.color.hairbow"),
                assets.color(namespace + ".pac.color.hairbow.pearls"),
                assets.color(namespace + ".pac.color.boobs")
            )
        );
    }
}