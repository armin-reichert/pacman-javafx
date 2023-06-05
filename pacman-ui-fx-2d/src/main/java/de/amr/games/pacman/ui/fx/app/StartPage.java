package de.amr.games.pacman.ui.fx.app;

import static javafx.scene.layout.BackgroundSize.AUTO;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
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

	public ObjectProperty<Image> wallpaperPy = new SimpleObjectProperty<>();

	private final BorderPane content = new BorderPane();
	private final Pane button;
	private Runnable action;

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

	public StartPage(Theme theme) {
		setBackground(ResourceManager.coloredBackground(Color.BLACK));
		getChildren().add(content);
		button = createButton("Play!", theme);
		button.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				action.run();
			}
		});
		content.setBottom(button);
		BorderPane.setAlignment(button, Pos.CENTER);
		button.setTranslateY(-10);
	}

	public void setWallpaper(Image image) {
		wallpaperPy.set(image);
		var bgImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, new BackgroundSize(AUTO, AUTO, false, false, true, false));
		content.setBackground(new Background(bgImage));
	}

	public void setOnAction(Runnable action) {
		this.action = action;
	}

	public void handleKeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			action.run();
		}
	}
}