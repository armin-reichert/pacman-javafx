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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundClipID;
import de.amr.games.pacman.ui.fx.util.Picker;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 */
public class AppResources {

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static final String MSG_NOT_LOADED = "App resources not loaded";

	private static PacModel3D pacModel3D;

	public static PacModel3D pacModel3D() {
		if (pacModel3D != null) {
			return pacModel3D;
		}
		throw new IllegalStateException(MSG_NOT_LOADED);
	}

	private static Model3D ghostModel3D;

	public static Model3D ghostModel3D() {
		if (ghostModel3D != null) {
			return ghostModel3D;
		}
		throw new IllegalStateException(MSG_NOT_LOADED);
	}

	public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private static Model3D pelletModel3D;

	public static Model3D pelletModel3D() {
		if (pelletModel3D != null) {
			return pelletModel3D;
		}
		throw new IllegalStateException(MSG_NOT_LOADED);
	}

	public static final String MESH_ID_PELLET = "Fruit";

	public static final String KEY_NO_TEXTURE = "No Texture";

	private static final Picker<String> READY_TEXT_PICKER_PACMAN = new Picker<>(//
			"LET'S GO BRANDON!", "YELLOW MAN BAD!", "C'MON MAN!", "Asufutimaehaehfutbw");

	private static final Picker<String> READY_TEXT_PICKER_MS_PACMAN = new Picker<>(//
			"LET'S GO BRANDON!", "GHOST LIVES MATTER!", "(EAT) ME TOO!");

	private static ResourceBundle messageBundle;
	private static Picker<String> messagePickerCheating;
	private static Picker<String> messagePickerLevelComplete;
	private static Picker<String> messagePickerGameOver;
	private static Image iconPacManGame;
	private static Image iconMsPacManGame;
	private static Image skyImage;
	private static Map<String, PhongMaterial> textures = new LinkedHashMap<>();

	public static final String VOICE_HELP = "sound/common/press-key.mp3";
	public static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	public static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	public static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	public static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	public static final String SOUND_SWEEP = "sound/common/sweep.mp3";
	public static final String SOUND_LEVEL_COMPLETE = "sound/common/level-complete.mp3";
	public static final String SOUND_GAME_OVER = "sound/common/game-over.mp3";

	private static final Object[][] MS_PACMAN_SOUND_DATA = { //
			{ SoundClipID.BONUS_EATEN, "sound/mspacman/Fruit.mp3", 1.0 }, //
			{ SoundClipID.CREDIT, "sound/mspacman/Credit.mp3", 1.0 }, //
			{ SoundClipID.EXTRA_LIFE, "sound/mspacman/ExtraLife.mp3", 1.0 }, //
			{ SoundClipID.GAME_READY, "sound/mspacman/Start.mp3", 1.0 }, //
			{ SoundClipID.GHOST_EATEN, "sound/mspacman/Ghost.mp3", 1.0 }, //
			{ SoundClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_1, "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_2, "sound/mspacman/Act2TheChase.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_3, "sound/mspacman/Act3Junior.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_DEATH, "sound/mspacman/Died.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_MUNCH, "sound/mspacman/Pill.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_POWER, "sound/mspacman/ScaredGhost.mp3", 1.0 }, //
			{ SoundClipID.SIREN_1, "sound/mspacman/GhostNoise1.mp3", 1.0 }, //
			{ SoundClipID.SIREN_2, "sound/mspacman/GhostNoise2.mp3", 1.0 }, //
			{ SoundClipID.SIREN_3, "sound/mspacman/GhostNoise3.mp3", 1.0 }, //
			{ SoundClipID.SIREN_4, "sound/mspacman/GhostNoise4.mp3", 1.0 }, //
	};

	private static final Object[][] PACMAN_SOUND_DATA = { //
			{ SoundClipID.BONUS_EATEN, "sound/pacman/eat_fruit.mp3", 1.0 }, //
			{ SoundClipID.CREDIT, "sound/pacman/credit.wav", 1.0 }, //
			{ SoundClipID.EXTRA_LIFE, "sound/pacman/extend.mp3", 1.0 }, //
			{ SoundClipID.GAME_READY, "sound/pacman/game_start.mp3", 1.0 }, //
			{ SoundClipID.GHOST_EATEN, "sound/pacman/eat_ghost.mp3", 1.0 }, //
			{ SoundClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, //
			{ SoundClipID.INTERMISSION_1, "sound/pacman/intermission.mp3", 1.0 }, //
			{ SoundClipID.PACMAN_DEATH, "sound/pacman/pacman_death.wav", 0.5 }, //
			{ SoundClipID.PACMAN_MUNCH, "sound/pacman/doublemunch.wav", 1.0 }, //
			{ SoundClipID.PACMAN_POWER, "sound/pacman/power_pellet.mp3", 1.0 }, //
			{ SoundClipID.SIREN_1, "sound/pacman/siren_1.mp3", 0.4 }, //
			{ SoundClipID.SIREN_2, "sound/pacman/siren_2.mp3", 0.4 }, //
			{ SoundClipID.SIREN_3, "sound/pacman/siren_3.mp3", 0.4 }, //
			{ SoundClipID.SIREN_4, "sound/pacman/siren_4.mp3", 0.4 }, //
	};

	private static GameSounds soundsMsPacMan;
	private static GameSounds soundsPacMan;

	public static GameSounds sounds(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> soundsMsPacMan;
		case PACMAN -> soundsPacMan;
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	public static void load() {
		LOG.info("Loading application resources...");
		var start = System.nanoTime();

		// 3D models
		pacModel3D = new PacModel3D("model3D/pacman.obj");
		ghostModel3D = new Model3D("model3D/ghost.obj");
		pelletModel3D = new Model3D("model3D/12206_Fruit_v1_L3.obj");

		// graphics
		loadFloorTexture("Chrome", "chrome");
		loadFloorTexture("Grass", "grass");
		loadFloorTexture("Hexagon", "hexagon");
		loadFloorTexture("Knobs & Bumps", "knobs");
		loadFloorTexture("Plastic", "plastic");
		loadFloorTexture("Wood", "wood");

		iconPacManGame = ResourceMgr.image("icons/pacman.png");
		iconMsPacManGame = ResourceMgr.image("icons/mspacman.png");
		skyImage = ResourceMgr.image("graphics/sky.png");

		// sounds
		soundsMsPacMan = new GameSounds(MS_PACMAN_SOUND_DATA);
		soundsPacMan = new GameSounds(PACMAN_SOUND_DATA);

		// texts
		messageBundle = ResourceBundle.getBundle("assets.texts.messages");
		messagePickerCheating = ResourceMgr.createPicker(messageBundle, "cheating");
		messagePickerLevelComplete = ResourceMgr.createPicker(messageBundle, "level.complete");
		messagePickerGameOver = ResourceMgr.createPicker(messageBundle, "game.over");

		LOG.info("Loading application resources done (%.2f seconds).", (System.nanoTime() - start) / 1_000_000_000f);
	}

	private static void loadFloorTexture(String key, String textureName) {
		var material = new PhongMaterial();
		textures.put(key, material);
		material.setBumpMap(ResourceMgr.image("graphics/textures/%s-bump.jpg".formatted(textureName)));
		material.setDiffuseMap(ResourceMgr.image("graphics/textures/%s-diffuse.jpg".formatted(textureName)));
		material.diffuseColorProperty().bind(Env.d3_floorColorPy);
		material.specularColorProperty()
				.bind(Bindings.createObjectBinding(Env.d3_floorColorPy.get()::brighter, Env.d3_floorColorPy));
	}

	public static PhongMaterial texture(String key) {
		return textures.get(key);
	}

	public static String[] textureKeys() {
		return textures.keySet().toArray(String[]::new);
	}

	public static String randomTextureKey() {
		var keys = AppResources.textureKeys();
		return textureKeys()[randomInt(0, keys.length)];
	}

	public static Image appIcon(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> iconMsPacManGame;
		case PACMAN -> iconPacManGame;
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	public static BackgroundImage backgroundImage3D() {
		return new BackgroundImage(skyImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null);
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
			LOG.error("No text resource found for key '%s'", keyPattern);
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
		var picker = variant == GameVariant.MS_PACMAN ? READY_TEXT_PICKER_MS_PACMAN : READY_TEXT_PICKER_PACMAN;
		return picker.next();
	}
}