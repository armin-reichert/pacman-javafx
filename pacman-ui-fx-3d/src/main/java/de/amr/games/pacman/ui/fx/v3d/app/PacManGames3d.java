/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

import java.util.ResourceBundle;

import static de.amr.games.pacman.ui.fx.input.Keyboard.alt;
import static de.amr.games.pacman.ui.fx.input.Keyboard.just;

/**
 * @author Armin Reichert
 */
public class PacManGames3d {

	public static final ResourceManager MGR = new ResourceManager("/de/amr/games/pacman/ui/fx/v3d/", PacManGames3d.class);

	public static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.v3d.texts.messages");

	private static final Picker<String> PICKER_READY_PAC_MAN    = Picker.fromBundle(TEXTS, "pacman.ready");
	private static final Picker<String> PICKER_READY_MS_PAC_MAN = Picker.fromBundle(TEXTS, "mspacman.ready");
	private static final Picker<String> PICKER_CHEATING         = Picker.fromBundle(TEXTS, "cheating");
	private static final Picker<String> PICKER_LEVEL_COMPLETE   = Picker.fromBundle(TEXTS, "level.complete");
	private static final Picker<String> PICKER_GAME_OVER        = Picker.fromBundle(TEXTS, "game.over");

	public static final String KEY_NO_TEXTURE = "No Texture";

	public static Theme createTheme() {
		var theme = new ArcadeTheme(PacManGames2d.MGR);

		theme.set("model3D.pacman",     new Model3D(MGR.url("model3D/pacman.obj")));
		theme.set("model3D.ghost",      new Model3D(MGR.url("model3D/ghost.obj")));
		theme.set("model3D.pellet",     new Model3D(MGR.url("model3D/12206_Fruit_v1_L3.obj")));
		theme.set("model3D.wallpaper",  MGR.imageBackground("graphics/sky.png",
				BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER,
				new BackgroundSize(1, 1, true, true, false, true)
		));
		theme.set("image.armin1970",    MGR.image("graphics/armin.jpg"));
		theme.set("icon.play",          MGR.image("graphics/icons/play.png"));
		theme.set("icon.stop",          MGR.image("graphics/icons/stop.png"));
		theme.set("icon.step",          MGR.image("graphics/icons/step.png"));
		
		theme.set("texture.hexagon",    createFloorTexture("hexagon", "jpg"));
		theme.set("texture.knobs",      createFloorTexture("knobs", "jpg"));
		theme.set("texture.plastic",    createFloorTexture("plastic", "jpg"));
		theme.set("texture.wood",       createFloorTexture("wood", "jpg"));

		return theme;
	}

	public static String pickFunnyReadyMessage(GameVariant gameVariant) {
		return switch (gameVariant) {
		case MS_PACMAN -> PICKER_READY_MS_PAC_MAN.next();
		case PACMAN -> PICKER_READY_PAC_MAN.next();
		default -> throw new IllegalGameVariantException(gameVariant);
		};
	}

	public static String pickCheatingMessage() {
		return PICKER_CHEATING.next();
	}

	public static String pickGameOverMessage() {
		return PICKER_GAME_OVER.next();
	}

	public static String pickLevelCompleteMessage(int levelNumber) {
		return "%s%n%n%s".formatted(PICKER_LEVEL_COMPLETE.next(),
				ResourceManager.message(TEXTS, "level_complete", levelNumber));
	}

	private static PhongMaterial createFloorTexture(String textureBase, String ext) {
		var material = textureMaterial(textureBase, ext, null, null);
		material.diffuseColorProperty().bind(PacManGames3d.PY_3D_FLOOR_COLOR);
		return material;
	}

	public static PhongMaterial textureMaterial(String textureBase, String ext, Color diffuseColor, Color specularColor) {
		var texture = new PhongMaterial();
		texture.setBumpMap(MGR.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
		texture.setDiffuseMap(MGR.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
		texture.setDiffuseColor(diffuseColor);
		texture.setSpecularColor(specularColor);
		return texture;
	}

	public static final float                       PIP_MIN_HEIGHT           = 36 * 8;
	public static final float                       PIP_MAX_HEIGHT           = 2.5f * PIP_MIN_HEIGHT;

	public static final DoubleProperty              PY_PIP_OPACITY           = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty              PY_PIP_HEIGHT            = new SimpleDoubleProperty(World.TILES_Y * Globals.TS);
	public static final BooleanProperty             PY_PIP_ON                = new SimpleBooleanProperty(false);

	public static final IntegerProperty             PY_SIMULATION_STEPS      = new SimpleIntegerProperty(1);

	public static final BooleanProperty             PY_3D_AXES_VISIBLE       = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode>    PY_3D_DRAW_MODE          = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty             PY_3D_ENABLED            = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color>       PY_3D_FLOOR_COLOR        = new SimpleObjectProperty<>(Color.grayRgb(0x60));
	public static final StringProperty              PY_3D_FLOOR_TEXTURE      = new SimpleStringProperty("knobs");
	public static final BooleanProperty             PY_3D_FLOOR_TEXTURE_RND  = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color>       PY_3D_LIGHT_COLOR        = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty              PY_3D_WALL_HEIGHT        = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty              PY_3D_WALL_THICKNESS     = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty             PY_3D_PAC_LIGHT_ENABLED  = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> PY_3D_PERSPECTIVE        = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty             PY_3D_ENERGIZER_EXPLODES = new SimpleBooleanProperty(true);

	public static final BooleanProperty             PY_WOKE_PUSSY            = new SimpleBooleanProperty(false); 

	public static final KeyCodeCombination          KEY_TOGGLE_DASHBOARD     = just(KeyCode.F1);
	public static final KeyCodeCombination          KEY_TOGGLE_DASHBOARD_2   = alt(KeyCode.B);
	public static final KeyCodeCombination          KEY_TOGGLE_PIP_VIEW      = just(KeyCode.F2);
	public static final KeyCodeCombination          KEY_TOGGLE_2D_3D         = alt(KeyCode.DIGIT3);
	public static final KeyCodeCombination          KEY_PREV_PERSPECTIVE     = alt(KeyCode.LEFT);
	public static final KeyCodeCombination          KEY_NEXT_PERSPECTIVE     = alt(KeyCode.RIGHT);
}