package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.Arrays;
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
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_SceneRendering;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_SceneRendering;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	public static final int MS_PACMAN = 0, PACMAN = 1;

	public static final MsPacMan_SceneRendering msPacManRendering = new MsPacMan_SceneRendering();
	public static final PacMan_SceneRendering pacManRendering = new PacMan_SceneRendering();

	public static final SoundManager[] sounds = { new PacManGameSoundManager(PacManGameSounds::getMsPacManSoundURL),
			new PacManGameSoundManager(PacManGameSounds::getPacManSoundURL) };

	private static final Deque<FlashMessage> flashMessageQ = new ArrayDeque<>();

	public static Optional<FlashMessage> flashMessage() {
		return Optional.ofNullable(flashMessageQ.peek());
	}

	private final GameScene<?>[/* gameType */][/* sceneIndex */] scenes = new GameScene[2][5];

	private final Stage stage;
	private final double width;
	private final double height;

	private PacManGameModel game;
	private GameScene<?> currentScene;
	private boolean muted;

	public PacManGameFXUI(Stage stage, PacManGameController controller, double scaling) {
		this.stage = stage;
		width = 28 * TS * scaling;
		height = 36 * TS * scaling;

		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			controller.endGame();
			Platform.exit();
		});

		scenes[MS_PACMAN][0] = new MsPacMan_IntroScene(width, height, scaling);
		scenes[MS_PACMAN][1] = new MsPacMan_IntermissionScene1(width, height, scaling);
		scenes[MS_PACMAN][2] = new MsPacMan_IntermissionScene2(width, height, scaling);
		scenes[MS_PACMAN][3] = new MsPacMan_IntermissionScene3(width, height, scaling);
		scenes[MS_PACMAN][4] = new PlayScene<MsPacMan_SceneRendering>(width, height, scaling, MS_PACMAN);

		scenes[PACMAN][0] = new PacMan_IntroScene(width, height, scaling);
		scenes[PACMAN][1] = new PacMan_IntermissionScene1(width, height, scaling);
		scenes[PACMAN][2] = new PacMan_IntermissionScene2(width, height, scaling);
		scenes[PACMAN][3] = new PacMan_IntermissionScene3(width, height, scaling);
		scenes[PACMAN][4] = new PlayScene<PacMan_SceneRendering>(width, height, scaling, PACMAN);

		onGameChanged(controller.getGame());
	}

	private int currentGameType() {
		if (game instanceof MsPacManGame) {
			return MS_PACMAN;
		}
		if (game instanceof PacManGame) {
			return PACMAN;
		}
		throw new IllegalStateException("Illegal game type " + game);
	}

	private GameScene<?> getScene() {
		int gameType = currentGameType();
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
	public void onGameChanged(PacManGameModel newGame) {
		this.game = Objects.requireNonNull(newGame);
		Arrays.stream(scenes[currentGameType()]).forEach(scene -> scene.setGame(newGame));
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
		GameScene<?> newScene = getScene();
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
		// TODO Should the game loop also run on the JavaFX application thread?
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
		onGameChanged(game);
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
		return muted ? Optional.empty() : Optional.of(sounds[currentGameType()]);
	}

	@Override
	public void mute(boolean muteState) {
		muted = muteState;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return currentScene.animation();
	}
}