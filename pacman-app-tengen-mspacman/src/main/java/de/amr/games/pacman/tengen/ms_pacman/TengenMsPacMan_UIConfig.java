/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_Palette;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapRepository;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_GhostAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_PacAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet;
import de.amr.games.pacman.tengen.ms_pacman.scene.*;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.GameUIConfiguration;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.WorldMapColorScheme;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
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

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class TengenMsPacMan_UIConfig implements GameUIConfiguration {

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

    public TengenMsPacMan_UIConfig() {
        ResourceManager rm = () -> TengenMsPacMan_UIConfig.class;

        appIcon = rm.loadImage("graphics/icons/mspacman.png");
        spriteSheet = new TengenMsPacMan_SpriteSheet(rm.loadImage("graphics/spritesheet.png"));
        mapRepository = new MapRepository(
            rm.loadImage("graphics/arcade_mazes.png"),
            rm.loadImage("graphics/non_arcade_mazes.png")
        );

        THE_UI.assets().store("tengen.startpage.image1",                 rm.loadImage("graphics/f1.png"));
        THE_UI.assets().store("tengen.startpage.image2",                 rm.loadImage("graphics/f2.png"));

        THE_UI.assets().store("tengen.color.game_over_message",          nesPaletteColor(0x11));
        THE_UI.assets().store("tengen.color.ready_message",              nesPaletteColor(0x28));

        THE_UI.assets().store("tengen.pac.color.head",                   nesPaletteColor(0x28));
        THE_UI.assets().store("tengen.pac.color.eyes",                   nesPaletteColor(0x02));
        THE_UI.assets().store("tengen.pac.color.palate",                 nesPaletteColor(0x2d));
        THE_UI.assets().store("tengen.pac.color.boobs",                  nesPaletteColor(0x28).deriveColor(0, 1.0, 0.96, 1.0));
        THE_UI.assets().store("tengen.pac.color.hairbow",                nesPaletteColor(0x05));
        THE_UI.assets().store("tengen.pac.color.hairbow.pearls",         nesPaletteColor(0x02));

        THE_UI.assets().store("tengen.ghost.0.color.normal.dress",       nesPaletteColor(0x05));
        THE_UI.assets().store("tengen.ghost.0.color.normal.eyeballs",    nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.0.color.normal.pupils",      nesPaletteColor(0x16));

        THE_UI.assets().store("tengen.ghost.1.color.normal.dress",       nesPaletteColor(0x25));
        THE_UI.assets().store("tengen.ghost.1.color.normal.eyeballs",    nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.1.color.normal.pupils",      nesPaletteColor(0x11));

        THE_UI.assets().store("tengen.ghost.2.color.normal.dress",       nesPaletteColor(0x11));
        THE_UI.assets().store("tengen.ghost.2.color.normal.eyeballs",    nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.2.color.normal.pupils",      nesPaletteColor(0x11));

        THE_UI.assets().store("tengen.ghost.3.color.normal.dress",       nesPaletteColor(0x16));
        THE_UI.assets().store("tengen.ghost.3.color.normal.eyeballs",    nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.3.color.normal.pupils",      nesPaletteColor(0x05));

        THE_UI.assets().store("tengen.ghost.color.frightened.dress",     nesPaletteColor(0x01));
        THE_UI.assets().store("tengen.ghost.color.frightened.eyeballs",  nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.color.frightened.pupils",    nesPaletteColor(0x20));

        //TODO has two flashing colors, when to use which?
        THE_UI.assets().store("tengen.ghost.color.flashing.dress",       nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.color.flashing.eyeballs",    nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.ghost.color.flashing.pupils",      nesPaletteColor(0x20));
        THE_UI.assets().store("tengen.audio.option.selection_changed",   rm.loadAudioClip("sound/ms-select1.wav"));
        THE_UI.assets().store("tengen.audio.option.value_changed",       rm.loadAudioClip("sound/ms-select2.wav"));

        THE_UI.assets().store("tengen.audio.bonus_eaten",                rm.loadAudioClip("sound/ms-fruit.wav"));
        THE_UI.assets().store("tengen.audio.extra_life",                 rm.loadAudioClip("sound/ms-extralife.wav"));
        THE_UI.assets().store("tengen.audio.ghost_eaten",                rm.loadAudioClip("sound/ms-ghosteat.wav"));

        THE_UI.assets().store("tengen.audio.intermission.4.junior.1",    rm.loadAudioClip("sound/ms-theend1.wav"));
        THE_UI.assets().store("tengen.audio.intermission.4.junior.2",    rm.loadAudioClip("sound/ms-theend2.wav"));


        // used only in 3D scene when level is completed:
        THE_UI.assets().store("tengen.audio.level_complete",             rm.url("sound/common/level-complete.mp3"));
        THE_UI.assets().store("tengen.audio.sweep",                      rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        THE_UI.assets().store("tengen.audio.game_ready",                 rm.url("sound/ms-start.wav"));
        THE_UI.assets().store("tengen.audio.intermission.1",             rm.url("sound/theymeet.wav"));
        THE_UI.assets().store("tengen.audio.intermission.2",             rm.url("sound/thechase.wav"));
        THE_UI.assets().store("tengen.audio.intermission.3",             rm.url("sound/junior.wav"));
        THE_UI.assets().store("tengen.audio.intermission.4",             rm.url("sound/theend.wav"));
        THE_UI.assets().store("tengen.audio.pacman_death",               rm.url("sound/ms-death.wav"));
        THE_UI.assets().store("tengen.audio.pacman_munch",               rm.url("sound/ms-dot.wav"));
        THE_UI.assets().store("tengen.audio.pacman_power",               rm.url("sound/ms-power.wav"));
        THE_UI.assets().store("tengen.audio.siren.1",                    rm.url("sound/ms-siren1.wav"));
        THE_UI.assets().store("tengen.audio.siren.2",                    rm.url("sound/ms-siren2.wav"));
        THE_UI.assets().store("tengen.audio.siren.3",                    rm.url("sound/ms-siren2.wav"));
        THE_UI.assets().store("tengen.audio.siren.4",                    rm.url("sound/ms-siren2.wav"));
        THE_UI.assets().store("tengen.audio.ghost_returns",              rm.url("sound/ms-eyes.wav"));
        THE_UI.assets().store("tengen.audio.bonus_bouncing",             rm.url("sound/fruitbounce.wav"));

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
    public GameScene selectGameScene() {
        String sceneID = switch (THE_GAME_CONTROLLER.state()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + THE_GAME_CONTROLLER.game().level().map(GameLevel::cutSceneNumber).orElseThrow();
            case TESTING_CUT_SCENES -> "CutScene" + THE_GAME_CONTROLLER.state().<Integer>getProperty("intermissionTestNumber");
            default                 -> GlobalProperties3d.PY_3D_ENABLED.get() ? "PlayScene3D" : "PlayScene2D";
        };
        return scenesByID.get(sceneID);
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
    public GameScene2D createPiPScene(Canvas canvasNotUsed) {
        var gameScene = new TengenMsPacMan_PiPScene();
        gameScene.setGameRenderer(createRenderer(gameScene.canvas()));
        return gameScene;
    }

    @Override
    public boolean isGameCanvasDecorated() { return false; }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public TengenMsPacMan_Renderer2D createRenderer(Canvas canvas) {
        return new TengenMsPacMan_Renderer2D(spriteSheet, mapRepository, canvas);
    }

    @Override
    public WorldMapColorScheme worldMapColoring(WorldMap worldMap) {
        NES_ColorScheme colorScheme = worldMap.getConfigValue("nesColorScheme");
        return new WorldMapColorScheme(
            colorScheme.fillColor(), colorScheme.strokeColor(), colorScheme.strokeColor(), colorScheme.pelletColor());
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new TengenMsPacMan_GhostAnimations(spriteSheet, ghost.id())));
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets, double size) {
        String namespace = assetNamespace();
        return new Group(
            PacModel3D.createPacShape(
                THE_UI.assets().get("model3D.pacman"), size,
                THE_UI.assets().color(namespace + ".pac.color.head"),
                THE_UI.assets().color(namespace + ".pac.color.eyes"),
                THE_UI.assets().color(namespace + ".pac.color.palate")
            ),
            PacModel3D.createFemaleParts(size,
                THE_UI.assets().color(namespace + ".pac.color.hairbow"),
                THE_UI.assets().color(namespace + ".pac.color.hairbow.pearls"),
                THE_UI.assets().color(namespace + ".pac.color.boobs")
            )
        );
    }
}