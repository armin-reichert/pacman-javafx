package de.amr.games.pacman.ui.fx.app;

import static javafx.scene.layout.BackgroundSize.AUTO;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class StartPage {

	private final Scene scene;
	private final StackPane root = new StackPane();
	private final BorderPane content = new BorderPane();
	private final Pane button;
	private PacManGames2dUI ui;

	// This should be a Button but it seems WebFX/GWT has issues with graphic buttons
	private static Pane createButton(String text, Theme theme, Runnable action) {
		var textView = new Text(text);
		textView.setFill(theme.color("startpage.button.color"));
		textView.setFont(theme.font("startpage.button.font"));
		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));
		textView.setEffect(ds);

		var button = new StackPane(textView);
		button.setMaxSize(200, 100);
		button.setPadding(new Insets(10));
		button.setCursor(Cursor.HAND);
		button.setBackground(ResourceManager.coloredRoundedBackground(theme.color("startpage.button.bgColor"), 20));

		button.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				action.run();
			}
		});

		return button;
	}

	public StartPage(PacManGames2dUI ui, double width, double height) {
		this.ui = ui;
		scene = new Scene(root, width, height);
		scene.setOnKeyPressed(this::handleKeyPressed);

		root.setBackground(ResourceManager.coloredBackground(Color.BLACK));
		root.getChildren().add(content);

		button = createButton("Play!", ui.theme(), this::startSelectedGame);
		content.setBottom(button);

		BorderPane.setAlignment(button, Pos.CENTER);
		button.setTranslateY(-10);
	}

	public Scene scene() {
		return scene;
	}

	public StackPane root() {
		return root;
	}

	public void setGameVariant(GameVariant gameVariant) {
		var image = gameVariant == GameVariant.MS_PACMAN ? ui.theme().image("mspacman.startpage.image")
				: ui.theme().image("pacman.startpage.image");
		var bgImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, new BackgroundSize(AUTO, AUTO, false, false, true, false));
		content.setBackground(new Background(bgImage));
	}

	public void handleKeyPressed(KeyEvent e) {
		switch (e.getCode()) {
		case ENTER:
		case SPACE:
			startSelectedGame();
			break;
		case V:
			ui.selectGameVariant(ui.gameVariant().next());
			break;
		case F11:
			ui.stage.setFullScreen(true);
			break;
		default:
			break;
		}
	}

	private void startSelectedGame() {
		ui.reboot();
		ui.stage.setScene(ui.mainScene);
		ui.clock.start();
		ui.soundHandler().playVoice("voice.explain", 1.5f);
	}
}