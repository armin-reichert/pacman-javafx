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

package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Globals.randomInt;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.Picker;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class AppRes {

	public static void load() {
		long start = System.nanoTime();
		load("3D models", Models3D::load);
		load("graphics", Graphics::load);
		load("sounds", Sounds::load);
		load("fonts", Fonts::load);
		load("texts", Texts::load);
		Logger.info("Loading application resources took {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	private static void load(String section, Runnable loadingCode) {
		long start = System.nanoTime();
		loadingCode.run();
		Logger.info("Loading {} done ({} seconds).", section, (System.nanoTime() - start) / 1e9f);
	}

	public static class Models3D {

		public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
		public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
		public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";
		public static final String MESH_ID_PELLET = "Fruit";

		public static PacModel3D pacModel3D;
		public static Model3D ghostModel3D;
		public static Model3D pelletModel3D;

		static void load() {
			pacModel3D = new PacModel3D("model3D/pacman.obj");
			ghostModel3D = new Model3D("model3D/ghost.obj");
			pelletModel3D = new Model3D("model3D/12206_Fruit_v1_L3.obj");
		}
	}

	public static class Fonts {

		public static Font arcadeFont;
		public static Font manuscriptFont;

		static void load() {
			arcadeFont = ResourceMgr.font("fonts/emulogic.ttf", 8);
			manuscriptFont = ResourceMgr.font("fonts/RockSalt-Regular.ttf", 8);
		}

		public static Font manuscriptFontSized(double size) {
			return Font.font(manuscriptFont.getFamily(), size);
		}

	}

	public static class Texts {

		private static ResourceBundle messageBundle;
		private static Picker<String> messagePickerCheating;
		private static Picker<String> messagePickerLevelComplete;
		private static Picker<String> messagePickerGameOver;
		private static Picker<String> readTextPickerPacMan;
		private static Picker<String> readyTextPickerMsPacman;

		static void load() {
			messageBundle = ResourceBundle.getBundle("assets.texts.messages");
			messagePickerCheating = ResourceMgr.createPicker(messageBundle, "cheating");
			messagePickerLevelComplete = ResourceMgr.createPicker(messageBundle, "level.complete");
			messagePickerGameOver = ResourceMgr.createPicker(messageBundle, "game.over");
			readTextPickerPacMan = new Picker<>("LET'S GO BRANDON!", "YELLOW MAN BAD!", "C'MON MAN!", "Asufutimaehaehfutbw");
			readyTextPickerMsPacman = new Picker<>("LET'S GO BRANDON!", "GHOST LIVES MATTER!", "(EAT) ME TOO!");
		}

		/**
		 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
		 * messages resource bundle.
		 * 
		 * @param keyPattern message key pattern
		 * @param args       arguments merged into key pattern
		 * @return message text for composed key or string indicating missing text
		 */
		public static String message(String keyPattern, Object... args) {
			try {
				var pattern = messageBundle.getString(keyPattern);
				return MessageFormat.format(pattern, args);
			} catch (Exception x) {
				Logger.error("No text resource found for key '{}'", keyPattern);
				return "missing{%s}".formatted(keyPattern);
			}
		}

		public static String pickCheatingMessage() {
			return messagePickerCheating.next();
		}

		public static String pickGameOverMessage() {
			return messagePickerGameOver.next();
		}

		public static String pickLevelCompleteMessage(int levelNumber) {
			return "%s%n%n%s".formatted(messagePickerLevelComplete.next(), message("level_complete", levelNumber));
		}

		public static String randomReadyText(GameVariant variant) {
			if (Env.wokePussyMode.get()) {
				return "READY!";
			} else {
				var picker = variant == GameVariant.MS_PACMAN ? readyTextPickerMsPacman : readTextPickerPacMan;
				return picker.next();
			}
		}
	}

	public static class Graphics {

		public static final String KEY_NO_TEXTURE = "No Texture";

		public static Image iconPacManGame;
		public static Image iconMsPacManGame;
		public static Background backgroundForScene3D;

		private static Map<String, PhongMaterial> floorTexturesByName = new LinkedHashMap<>();

		static void load() {
			loadFloorTexture("Hexagon", "hexagon", "jpg");
			loadFloorTexture("Knobs & Bumps", "knobs", "jpg");
			loadFloorTexture("Plastic", "plastic", "jpg");
			loadFloorTexture("Wood", "wood", "jpg");

			iconPacManGame = ResourceMgr.image("icons/pacman.png");
			iconMsPacManGame = ResourceMgr.image("icons/mspacman.png");
			backgroundForScene3D = new Background(
					new BackgroundImage(ResourceMgr.image("graphics/sky.png"), null, null, null, null));
		}

		private static void loadFloorTexture(String name, String textureBase, String ext) {
			var material = new PhongMaterial();
			material.setBumpMap(ResourceMgr.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			material.setDiffuseMap(ResourceMgr.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			material.diffuseColorProperty().bind(Env.d3_floorColorPy);
//			material.specularColorProperty()
//					.bind(Bindings.createObjectBinding(Env.d3_floorColorPy.get()::brighter, Env.d3_floorColorPy));
			floorTexturesByName.put(name, material);
		}

		public static PhongMaterial floorTexture(String name) {
			return floorTexturesByName.get(name);
		}

		public static String[] floorTextureNames() {
			return floorTexturesByName.keySet().toArray(String[]::new);
		}

		public static String randomFloorTextureName() {
			var names = floorTextureNames();
			return names[randomInt(0, names.length)];
		}

		public static PhongMaterial texture(String textureBase, String ext, Color diffuseColor, Color specularColor) {
			var texture = new PhongMaterial();
			texture.setBumpMap(ResourceMgr.image("graphics/textures/%s-bump.%s".formatted(textureBase, ext)));
			texture.setDiffuseMap(ResourceMgr.image("graphics/textures/%s-diffuse.%s".formatted(textureBase, ext)));
			texture.setDiffuseColor(diffuseColor);
			texture.setSpecularColor(specularColor);
			return texture;
		}
	}

	public static class Sounds {

		public static final String VOICE_HELP = "sound/voice/press-key.mp3";
		public static final String VOICE_AUTOPILOT_OFF = "sound/voice/autopilot-off.mp3";
		public static final String VOICE_AUTOPILOT_ON = "sound/voice/autopilot-on.mp3";
		public static final String VOICE_IMMUNITY_OFF = "sound/voice/immunity-off.mp3";
		public static final String VOICE_IMMUNITY_ON = "sound/voice/immunity-on.mp3";

		private static final Object[][] MS_PACMAN_AUDIO_CLIPS = { //
				{ AudioClipID.BONUS_EATEN, "sound/mspacman/Fruit.mp3", 1.0 }, //
				{ AudioClipID.CREDIT, "sound/mspacman/Credit.mp3", 1.0 }, //
				{ AudioClipID.EXTRA_LIFE, "sound/mspacman/ExtraLife.mp3", 1.0 }, //
				{ AudioClipID.GAME_READY, "sound/mspacman/Start.mp3", 1.0 }, //
				{ AudioClipID.GAME_OVER, "sound/common/game-over.mp3", 1.0 }, //
				{ AudioClipID.GHOST_EATEN, "sound/mspacman/Ghost.mp3", 1.0 }, //
				{ AudioClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, //
				{ AudioClipID.INTERMISSION_1, "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, //
				{ AudioClipID.INTERMISSION_2, "sound/mspacman/Act2TheChase.mp3", 1.0 }, //
				{ AudioClipID.INTERMISSION_3, "sound/mspacman/Act3Junior.mp3", 1.0 }, //
				{ AudioClipID.LEVEL_COMPLETE, "sound/common/level-complete.mp3", 1.0 }, //
				{ AudioClipID.PACMAN_DEATH, "sound/mspacman/Died.mp3", 1.0 }, //
				{ AudioClipID.PACMAN_MUNCH, "sound/mspacman/Pill.mp3", 1.0 }, //
				{ AudioClipID.PACMAN_POWER, "sound/mspacman/ScaredGhost.mp3", 1.0 }, //
				{ AudioClipID.SIREN_1, "sound/mspacman/GhostNoise1.mp3", 1.0 }, //
				{ AudioClipID.SIREN_2, "sound/mspacman/GhostNoise2.mp3", 1.0 }, //
				{ AudioClipID.SIREN_3, "sound/mspacman/GhostNoise3.mp3", 1.0 }, //
				{ AudioClipID.SIREN_4, "sound/mspacman/GhostNoise4.mp3", 1.0 }, //
				{ AudioClipID.SIREN_4, "sound/mspacman/GhostNoise4.mp3", 1.0 }, //
				{ AudioClipID.SWEEP, "sound/common/sweep.mp3", 1.0 }, //
		};

		private static final Object[][] PACMAN_AUDIO_CLIPS = { //
				{ AudioClipID.BONUS_EATEN, "sound/pacman/eat_fruit.mp3", 1.0 }, //
				{ AudioClipID.CREDIT, "sound/pacman/credit.wav", 1.0 }, //
				{ AudioClipID.EXTRA_LIFE, "sound/pacman/extend.mp3", 1.0 }, //
				{ AudioClipID.GAME_READY, "sound/pacman/game_start.mp3", 1.0 }, //
				{ AudioClipID.GAME_OVER, "sound/common/game-over.mp3", 1.0 }, //
				{ AudioClipID.GHOST_EATEN, "sound/pacman/eat_ghost.mp3", 1.0 }, //
				{ AudioClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, //
				{ AudioClipID.INTERMISSION_1, "sound/pacman/intermission.mp3", 1.0 }, //
				{ AudioClipID.LEVEL_COMPLETE, "sound/common/level-complete.mp3", 1.0 }, //
				{ AudioClipID.PACMAN_DEATH, "sound/pacman/pacman_death.wav", 0.5 }, //
				{ AudioClipID.PACMAN_MUNCH, "sound/pacman/doublemunch.wav", 1.0 }, //
				{ AudioClipID.PACMAN_POWER, "sound/pacman/power_pellet.mp3", 1.0 }, //
				{ AudioClipID.SIREN_1, "sound/pacman/siren_1.mp3", 0.4 }, //
				{ AudioClipID.SIREN_2, "sound/pacman/siren_2.mp3", 0.4 }, //
				{ AudioClipID.SIREN_3, "sound/pacman/siren_3.mp3", 0.4 }, //
				{ AudioClipID.SIREN_4, "sound/pacman/siren_4.mp3", 0.4 }, //
				{ AudioClipID.SWEEP, "sound/common/sweep.mp3", 1.0 }, //
		};

		private static GameSounds soundClipsMsPacMan;
		private static GameSounds soundClipsPacMan;

		static void load() {
			soundClipsMsPacMan = new GameSounds(MS_PACMAN_AUDIO_CLIPS);
			soundClipsPacMan = new GameSounds(PACMAN_AUDIO_CLIPS);
		}

		public static GameSounds soundClips(GameVariant variant) {
			return switch (variant) {
			case MS_PACMAN -> soundClipsMsPacMan;
			case PACMAN -> soundClipsPacMan;
			default -> throw new IllegalGameVariantException(variant);
			};
		}
	}
}