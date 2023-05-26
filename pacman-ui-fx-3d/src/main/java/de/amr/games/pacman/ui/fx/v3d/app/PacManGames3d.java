/*
MIT License

Copyright (c) 2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx.v3d.app;

import static de.amr.games.pacman.ui.fx.util.Ufx.alt;
import static de.amr.games.pacman.ui.fx.util.Ufx.just;

import java.util.ResourceBundle;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.Theme;
import de.amr.games.pacman.ui.fx.util.Picker;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public class PacManGames3d {

	public static final ResourceManager MGR = new ResourceManager("/de/amr/games/pacman/ui/fx/v3d/", PacManGames3d.class);

	public static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.v3d.texts.messages");

	private static final Picker<String> pickerReadyPacMan = Picker.fromBundle(TEXTS, "pacman.ready");
	private static final Picker<String> pickerReadyMsPacMan = Picker.fromBundle(TEXTS, "mspacman.ready");
	private static final Picker<String> pickerCheating = Picker.fromBundle(TEXTS, "cheating");
	private static final Picker<String> pickerLevelComplete = Picker.fromBundle(TEXTS, "level.complete");
	private static final Picker<String> pickerGameOver = Picker.fromBundle(TEXTS, "game.over");

	public static final String KEY_NO_TEXTURE = "No Texture";

	//@formatter:off
	public static void addThings(Theme theme) {
		theme.set("model3D.pacman",            new Model3D(MGR.url("model3D/pacman.obj")));
		theme.set("model3D.ghost",             new Model3D(MGR.url("model3D/ghost.obj")));
		theme.set("model3D.pellet",            new Model3D(MGR.url("model3D/12206_Fruit_v1_L3.obj")));
		theme.set("model3D.wallpaper",         MGR.imageBackground("graphics/sky.png", 
				                                     BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				                                     BackgroundPosition.CENTER, 
				                                     new BackgroundSize(1, 1, true, true, false, true)
				                                   ));
		theme.set("image.armin1970",           MGR.image("graphics/armin.jpg"));
		theme.set("icon.play",                 MGR.image("graphics/icons/play.png"));
		theme.set("icon.stop",                 MGR.image("graphics/icons/stop.png"));
		theme.set("icon.step",                 MGR.image("graphics/icons/step.png"));
		
		theme.set("texture.hexagon",           createFloorTexture("hexagon", "jpg"));
		theme.set("texture.knobs",             createFloorTexture("knobs", "jpg"));
		theme.set("texture.plastic",           createFloorTexture("plastic", "jpg"));
		theme.set("texture.wood",              createFloorTexture("wood", "jpg"));
	}

	public static String pickFunnyReadyMessage(GameVariant gameVariant) {
		return switch (gameVariant) {
		case MS_PACMAN -> pickerReadyMsPacMan.next();
		case PACMAN -> pickerReadyPacMan.next();
		default -> throw new IllegalGameVariantException(gameVariant);
		};
	}

	public static String pickCheatingMessage() {
		return pickerCheating.next();
	}

	public static String pickGameOverMessage() {
		return pickerGameOver.next();
	}

	public static String pickLevelCompleteMessage(int levelNumber) {
		return "%s%n%n%s".formatted(pickerLevelComplete.next(),
				ResourceManager.fmtMessage(TEXTS, "level_complete", levelNumber));
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

	//@formatter:off
	public static final float                       PIP_MIN_HEIGHT           = 36 * 8;
	public static final float                       PIP_MAX_HEIGHT           = 2.5f * PIP_MIN_HEIGHT;

	public static final DoubleProperty              PY_PIP_OPACITY           = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty              PY_PIP_HEIGHT            = new SimpleDoubleProperty(World.TILES_Y * Globals.TS);
	public static final BooleanProperty             PY_PIP_ON                = new SimpleBooleanProperty(false);

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

	public static PacManGames3dUI     ui;
	//@formatter:on
}