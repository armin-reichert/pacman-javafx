package de.amr.games.pacman.ui.fx.app;

import static javafx.scene.layout.BackgroundSize.AUTO;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
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
public class StartPage extends StackPane {

	private final BorderPane content = new BorderPane();
	private final Pane button;
	private PacManGames2dUI ui;

	private static Pane createButton(String text, Theme theme) {
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

		return button;
	}

	public StartPage(PacManGames2dUI ui) {
		this.ui = ui;
		setBackground(ResourceManager.coloredBackground(Color.BLACK));
		getChildren().add(content);
		button = createButton("Play!", ui.theme());
		button.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				ui.play();
			}
		});
		content.setBottom(button);
		BorderPane.setAlignment(button, Pos.CENTER);
		button.setTranslateY(-10);
	}

	public void setGameVariant(GameVariant gameVariant) {
		var image = gameVariant == GameVariant.MS_PACMAN ? ui.theme().image("mspacman.startpage.image")
				: ui.theme().image("pacman.startpage.image");
		var bgImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, new BackgroundSize(AUTO, AUTO, false, false, true, false));
		content.setBackground(new Background(bgImage));
	}

	public void handleKeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
			ui.play();
		}
		if (e.getCode() == KeyCode.V) {
			ui.selectGameVariant(ui.gameVariant().next());
		}
		if (PacManGames2d.KEY_FULLSCREEN.match(e)) {
			ui.stage.setFullScreen(true);
		}
	}
}