/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.lib.WorldMap;
import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.util.Picker;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import de.amr.games.pacman.ui3d.model.Model3D;
import de.amr.games.pacman.ui3d.scene.Perspective;
import de.amr.games.pacman.ui3d.scene.PlayScene3D;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.time.LocalTime;
import java.util.ResourceBundle;

import static de.amr.games.pacman.ui2d.util.Keyboard.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.stream.IntStream.rangeClosed;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a 2D and a 3D version. All others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the 3D play scene. It is activated/deactivated by pressing key F2.
 * Size and transparency can be controlled using the dashboard.
 * <p>
 *
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI implements ActionHandler3D {

    static {
        System.setProperty("javafx.sg.warn", "true"); // WTF?
    }

    public static final IntegerProperty PY_PIP_HEIGHT                 = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_PX.y());
    public static final IntegerProperty PY_PIP_OPACITY_PERCENTAGE     = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS           = new SimpleIntegerProperty(1);
    public static final BooleanProperty PY_3D_AXES_VISIBLE            = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE      = new SimpleObjectProperty<>(DrawMode.FILL);
    public static final BooleanProperty PY_3D_ENABLED                 = new SimpleBooleanProperty(true);
    public static final BooleanProperty PY_3D_ENERGIZER_EXPLODES      = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Color> PY_3D_FLOOR_COLOR       = new SimpleObjectProperty<>(Color.grayRgb(0x33));
    public static final StringProperty  PY_3D_FLOOR_TEXTURE           = new SimpleStringProperty("knobs");
    public static final BooleanProperty PY_3D_FLOOR_TEXTURE_RND       = new SimpleBooleanProperty(false);
    public static final ObjectProperty<Color> PY_3D_LIGHT_COLOR       = new SimpleObjectProperty<>(Color.GHOSTWHITE);
    public static final BooleanProperty PY_3D_NIGHT_MODE              = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_3D_PAC_LIGHT_ENABLED       = new SimpleBooleanProperty(true);
    public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE = new SimpleObjectProperty<>(Perspective.FOLLOWING_PLAYER);
    public static final BooleanProperty PY_3D_PIP_ON                  = new SimpleBooleanProperty(false);
    public static final DoubleProperty  PY_3D_WALL_HEIGHT             = new SimpleDoubleProperty(4.5);
    public static final DoubleProperty  PY_3D_WALL_OPACITY            = new SimpleDoubleProperty(0.9);

    public static final KeyCodeCombination[] KEYS_TOGGLE_DASHBOARD    = {just(KeyCode.F1), alt(KeyCode.B)};
    public static final KeyCodeCombination KEY_TOGGLE_PIP_VIEW        = just(KeyCode.F2);
    public static final KeyCodeCombination KEY_TOGGLE_2D_3D           = alt(KeyCode.DIGIT3);
    public static final KeyCodeCombination KEY_PREV_PERSPECTIVE       = alt(KeyCode.LEFT);
    public static final KeyCodeCombination KEY_NEXT_PERSPECTIVE       = alt(KeyCode.RIGHT);
    public static final KeyCodeCombination KEY_SWITCH_EDITOR          = shift_alt(KeyCode.E);

    public static final String NO_TEXTURE                             = "No Texture";

    private static final BackgroundSize FILL_PAGE = new BackgroundSize(1, 1, true, true, false, true);

    public static Picker<String> PICKER_LEVEL_COMPLETE;
    public static Picker<String> PICKER_GAME_OVER;

    private TileMapEditor editor;

    @Override
    public void loadAssets() {
        super.loadAssets();

        var messages = ResourceBundle.getBundle("de.amr.games.pacman.ui3d.texts.messages", getClass().getModule());
        PICKER_LEVEL_COMPLETE = Picker.fromBundle(messages, "level.complete");
        PICKER_GAME_OVER      = Picker.fromBundle(messages, "game.over");
        bundles.add(messages);

        ResourceManager rm = () -> PacManGames3dUI.class;

        theme.set("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
        theme.set("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
        theme.set("model3D.pellet", new Model3D(rm.url("model3D/12206_Fruit_v1_L3.obj")));

        theme.set("model3D.wallpaper", rm.imageBackground("graphics/sea-wallpaper.jpg",
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, FILL_PAGE));

        theme.set("model3D.wallpaper.night", rm.imageBackground("graphics/sea-wallpaper-night.jpg",
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, FILL_PAGE));

        theme.set("image.armin1970", rm.loadImage("graphics/armin.jpg"));
        theme.set("icon.play",       rm.loadImage("graphics/icons/play.png"));
        theme.set("icon.stop",       rm.loadImage("graphics/icons/stop.png"));
        theme.set("icon.step",       rm.loadImage("graphics/icons/step.png"));

        theme.addAllToArray("texture.names", new String[] {"knobs", "plastic", "wood"});
        for (var name : theme.getArray("texture.names")) {
            var texture = new PhongMaterial();
            texture.setBumpMap(rm.loadImage("graphics/textures/%s-bump.jpg".formatted(name)));
            texture.setDiffuseMap(rm.loadImage("graphics/textures/%s-diffuse.jpg".formatted(name)));
            texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
            theme.set("texture." + name, texture);
        }

        theme.set("ghost.0.color.normal.dress",      theme.color("palette.red"));
        theme.set("ghost.0.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.0.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.1.color.normal.dress",      theme.color("palette.pink"));
        theme.set("ghost.1.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.1.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.2.color.normal.dress",      theme.color("palette.cyan"));
        theme.set("ghost.2.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.2.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.3.color.normal.dress",      theme.color("palette.orange"));
        theme.set("ghost.3.color.normal.eyeballs",   theme.color("palette.pale"));
        theme.set("ghost.3.color.normal.pupils",     theme.color("palette.blue"));

        theme.set("ghost.color.frightened.dress",    theme.color("palette.blue"));
        theme.set("ghost.color.frightened.eyeballs", theme.color("palette.rose"));
        theme.set("ghost.color.frightened.pupils",   theme.color("palette.rose"));

        theme.set("ghost.color.flashing.dress",      theme.color("palette.pale"));
        theme.set("ghost.color.flashing.eyeballs",   theme.color("palette.rose"));
        theme.set("ghost.color.flashing.pupils",     theme.color("palette.red"));

        theme.set("ms_pacman.color.head",           Color.rgb(255, 255, 0));
        theme.set("ms_pacman.color.palate",         Color.rgb(191, 79, 61));
        theme.set("ms_pacman.color.eyes",           Color.rgb(33, 33, 33));
        theme.set("ms_pacman.color.boobs",          Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
        theme.set("ms_pacman.color.hairbow",        Color.rgb(255, 0, 0));
        theme.set("ms_pacman.color.hairbow.pearls", Color.rgb(33, 33, 255));

        theme.set("pacman.color.head",             Color.rgb(255, 255, 0));
        theme.set("pacman.color.palate",           Color.rgb(191, 79, 61));
        theme.set("pacman.color.eyes",             Color.rgb(33, 33, 33));

        // lives counter
        theme.set("livescounter.entries",          5);
        theme.set("livescounter.pac.size",         10.0);
        theme.set("livescounter.pillar.color",     Color.grayRgb(120));
        theme.set("livescounter.pillar.height",    8.0);
        theme.set("livescounter.plate.color",      Color.grayRgb(180));
        theme.set("livescounter.plate.radius",     6.0);
        theme.set("livescounter.plate.thickness",  1.0);
        theme.set("livescounter.light.color",      Color.CORNFLOWERBLUE);

        // dashboard
        theme.set("infobox.min_col_width",         200);
        theme.set("infobox.min_label_width",       140);
        theme.set("infobox.text_color",            Color.WHITE);
        theme.set("infobox.label_font",            Font.font("Sans", 12));
        theme.set("infobox.text_font",             rm.loadFont("fonts/SplineSansMono-Regular.ttf", 12));
    }

    public void init(Stage stage, double width, double height) {
        super.init(stage, width, height);
        mainScene.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
        mainScene.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        stage.titleProperty().bind(stageTitleBinding(clock.pausedPy, gameVariantPy, PY_3D_DRAW_MODE, PY_3D_ENABLED));
        embedMapEditor(stage);

        int hour = LocalTime.now().getHour();
        PY_3D_NIGHT_MODE.set(hour >= 20 || hour <= 5);
    }

    @Override
    protected void createGameScenes() {
        super.createGameScenes();
        for (var variant : GameVariant.values()) {
            var playScene3D = new PlayScene3D();
            playScene3D.setContext(this);
            playScene3D.setParentScene(mainScene);
            gameScenesForVariant.get(variant).put("play3D", playScene3D);
            Logger.info("Added 3D play scene for variant " + variant);
        }
    }

    private void embedMapEditor(Window window) {
        editor = new TileMapEditor(GameModel.CUSTOM_MAP_DIR);
        editor.createUI(window);
        var miQuitEditor = new MenuItem("Back to Game");
        miQuitEditor.setOnAction(e -> quitMapEditor());
        editor.menuFile().getItems().add(miQuitEditor);
        editor.addPredefinedMap("Pac-Man",
            new WorldMap(GameModel.class.getResource("/de/amr/games/pacman/maps/pacman.world")));

        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(mapNumber -> editor.addPredefinedMap("Ms. Pac-Man " + mapNumber,
            new WorldMap(GameModel.class.getResource("/de/amr/games/pacman/maps/mspacman/mspacman_" + mapNumber + ".world")))
        );
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(mapNumber -> editor.addPredefinedMap("Pac-Man XXL " + mapNumber,
            new WorldMap(GameModel.class.getResource("/de/amr/games/pacman/maps/masonic/masonic_" + mapNumber + ".world")))
        );

        var editorLayout = new BorderPane();
        editorLayout.setCenter(editor.getLayout());
        editorLayout.setTop(editor.getMenuBar());
        pages.put("editorPage", () -> editorLayout); // fancy, isn't it?
    }

    private void enterMapEditor() {
        if (game().world() != null) {
            editor.setMap(game().world().map());
        }
        restartIntro();
        gameClock().stop();
        selectPage("editorPage");
        stage.titleProperty().bind(editor.titlePy);
        editor.start();
    }

    private void quitMapEditor() {
        editor.showConfirmation(
            editor::saveMapFileAs,
            () -> {
                editor.stop();
                selectPage("startPage");
                stage.titleProperty().bind(stageTitleBinding(clock.pausedPy, gameVariantPy, PY_3D_DRAW_MODE, PY_3D_ENABLED));
            }
        );
    }

    @Override
    protected GamePage3D createGamePage() {
        GamePage3D page = new GamePage3D(mainScene, this, mainScene.getWidth(), mainScene.getHeight());
        page.setUnscaledCanvasWidth(CANVAS_WIDTH_UNSCALED);
        page.setUnscaledCanvasHeight(CANVAS_HEIGHT_UNSCALED);
        page.setMinScaling(0.7);
        page.setDiscreteScaling(false);
        page.setCanvasBorderEnabled(true);
        page.setCanvasBorderColor(theme().color("palette.pale"));
        page.getCanvasLayer().setBackground(theme().background("wallpaper.background"));
        page.getCanvasContainer().setBackground(Ufx.coloredBackground(theme().color("canvas.background")));
        gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
        return page;
    }

    private void onMouseClicked(MouseEvent e) {
        currentPage().onMouseClicked(e);
    }

    private void onKeyPressed(KeyEvent e) {
        if (KEY_SWITCH_EDITOR.match(e)) {
            if (isPageSelected("editorPage")) {
                quitMapEditor();
            } else if (game().variant() == GameVariant.PACMAN_XXL) {
                enterMapEditor();
            }
        }
    }

    @Override
    public ActionHandler3D actionHandler() {
        return this;
    }

    @Override
    protected StringBinding stageTitleBinding(Observable... dependencies) {
        return Bindings.createStringBinding(() -> {
            var tk = "app.title." + game().variant().resourceKey() + (gameClock().isPaused() ? ".paused" : "");
            var dim = tt(PY_3D_ENABLED.get() ? "threeD" : "twoD");
            return tt(tk, dim);
        }, dependencies);
    }

    @Override
    protected GameScene sceneMatchingCurrentGameState() {
        var gameScene = super.sceneMatchingCurrentGameState();
        if (PY_3D_ENABLED.get() && gameScene == sceneConfig().get("play") && sceneConfig().containsKey("play3D")) {
            return sceneConfig().get("play3D");
        }
        return gameScene;
    }

    @Override
    public void selectNextPerspective() {
        var next = Perspective.next(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(next);
        showFlashMessage(tt("camera_perspective", tt(PY_3D_PERSPECTIVE.get().name())));
    }

    @Override
    public void selectPrevPerspective() {
        var prev = Perspective.previous(PY_3D_PERSPECTIVE.get());
        PY_3D_PERSPECTIVE.set(prev);
        showFlashMessage(tt("camera_perspective", tt(PY_3D_PERSPECTIVE.get().name())));
    }

    @Override
    public void toggleDrawMode() {
        PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
    }

    @Override
    public void toggle2D3D() {
        currentGameScene().ifPresent(gameScene -> {
            toggle(PY_3D_ENABLED);
            gameScene = sceneMatchingCurrentGameState();
            if (gameScene == sceneConfig().get("play") || gameScene == sceneConfig().get("play3D")) {
                updateOrReloadGameScene(true);
                gameScene.onSceneVariantSwitch();
            }
            gameController().update();
            if (!game().isPlaying()) {
                showFlashMessage(tt(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
            }
        });
    }

    @Override
    public void togglePipVisible() {
        toggle(PY_3D_PIP_ON);
        showFlashMessage(tt(PY_3D_PIP_ON.get() ? "pip_on" : "pip_off"));
    }
}