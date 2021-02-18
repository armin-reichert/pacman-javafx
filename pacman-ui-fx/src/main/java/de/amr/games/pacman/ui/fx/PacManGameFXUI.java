package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacManSceneRendering;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacManSceneRendering;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	static final int MS_PACMAN = 0, PACMAN = 1;

	public static final SoundManager pacManSounds = new PacManGameSoundManager(PacManGameSounds::getPacManSoundURL);
	public static final SoundManager msPacManSounds = new PacManGameSoundManager(PacManGameSounds::getMsPacManSoundURL);

	public static final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();

	private final PacManGameScene[/* Game Type */][/* SceneID */] scenes = new PacManGameScene[2][5];

	private final Stage stage;
	private final double scaling;
	private final double width;
	private final double height;

	private PacManGameModel game;
	private PacManGameScene currentScene;

	private boolean muted;

	public PacManGameFXUI(Stage stage, PacManGameController controller, double scaling) {
		this.stage = stage;
		this.scaling = scaling;
		width = 28 * TS * scaling;
		height = 36 * TS * scaling;
		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			controller.endGame();
			Platform.exit();
		});
		setGame(controller.getGame());
		log("Pac-Man game JavaFX UI created");
	}

	private void createScenes(int gameType) {
		switch (gameType) {
		case MS_PACMAN:
			scenes[MS_PACMAN][0] = new MsPacMan_IntroScene(game, width, height, scaling);
			scenes[MS_PACMAN][1] = new MsPacMan_IntermissionScene1(game, width, height, scaling);
			scenes[MS_PACMAN][2] = new MsPacMan_IntermissionScene2(game, width, height, scaling);
			scenes[MS_PACMAN][3] = new MsPacMan_IntermissionScene3(game, width, height, scaling);
			scenes[MS_PACMAN][4] = new PlayScene<>(width, height, scaling, game, MsPacManSceneRendering.IT, msPacManSounds);
			break;
		case PACMAN:
			scenes[PACMAN][0] = new PacMan_IntroScene(game, width, height, scaling);
			scenes[PACMAN][1] = new PacMan_IntermissionScene1(game, width, height, scaling);
			scenes[PACMAN][2] = new PacMan_IntermissionScene2(game, width, height, scaling);
			scenes[PACMAN][3] = new PacMan_IntermissionScene3(game, width, height, scaling);
			scenes[PACMAN][4] = new PlayScene<>(width, height, scaling, game, PacManSceneRendering.IT, pacManSounds);
			break;
		default:
			break;
		}
	}

	private PacManGameScene getScene() {
		int gameType = game instanceof MsPacManGame ? 0 : 1;
		switch (game.state) {
		case INTRO:
			return scenes[gameType][0];
		case INTERMISSION:
			return scenes[gameType][game.intermissionNumber];
		default:
			return scenes[gameType][4];
		}
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = Objects.requireNonNull(game);
		if (game instanceof MsPacManGame) {
			createScenes(MS_PACMAN);
		} else {
			createScenes(PACMAN);
		}
		currentScene = getScene();
		currentScene.start();
	}

	@Override
	public void show() {
		stage.setScene(currentScene.getFXScene());
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void update() {
		PacManGameScene newScene = getScene();
		if (currentScene != newScene) {
			log("%s: Scene changes from %s to %s", this, currentScene, newScene);
			if (currentScene != null) {
				currentScene.end();
			}
			newScene.start();
			currentScene = newScene;
		}
		currentScene.update();

		FlashMessage message = flashMessageQ.peek();
		if (message != null) {
			message.timer.run();
			if (message.timer.expired()) {
				flashMessageQ.remove();
			}
		}
	}

	@Override
	public void render() {
		Platform.runLater(() -> {
			if (stage.getScene() != currentScene.getFXScene()) {
				stage.setScene(currentScene.getFXScene());
			}
			try {
				currentScene.render();
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentScene);
				x.printStackTrace();
			}
		});
	}

	@Override
	public void reset() {
		currentScene.end();
		setGame(game);
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = currentScene.keyboard().keyPressed(keySpec);
		currentScene.keyboard().clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty();
		}
		if (game instanceof PacManGame) {
			return Optional.of(pacManSounds);
		}
		if (game instanceof MsPacManGame) {
			return Optional.of(msPacManSounds);
		}
		return Optional.empty();
	}

	@Override
	public void mute(boolean b) {
		muted = b;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return currentScene.animation();
	}
}