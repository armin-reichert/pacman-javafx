package de.amr.games.pacman.ui.fx;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

/**
 * Layered container containing a canvas in the center of the lowest layer.
 *
 * <p>TODO: needs testing</p>
 */
public class CanvasContainer {

	protected final StackPane layers = new StackPane();
	protected final BorderPane canvasLayer = new BorderPane();
	protected final BorderPane canvasContainer = new BorderPane();
	protected final Canvas canvas = new Canvas();

	protected double minScaling = 1.0;
	protected double scaling = 1.0;
	protected double unscaledCanvasWidth = 300;
	protected double unscaledCanvasHeight = 400;
	protected Color canvasBorderColor = Color.WHITE;
	protected boolean canvasBorderEnabled = false;
	protected boolean discreteScaling = false;

	public CanvasContainer() {
		canvasLayer.setCenter(canvasContainer);
		canvasContainer.setCenter(canvas);
		layers.getChildren().add(canvasLayer);
		canvasContainer.widthProperty().addListener((py, ov, nv) -> scalePage(scaling, false));
		canvasContainer.heightProperty().addListener((py, ov, nv) -> scalePage(scaling, false));
	}

	public Pane root() {
		return layers;
	}

	public BorderPane getCanvasLayer() {
		return canvasLayer;
	}

	public BorderPane getCanvasContainer() {
		return canvasContainer;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setUnscaledCanvasWidth(double unscaledCanvasWidth) {
		this.unscaledCanvasWidth = unscaledCanvasWidth;
	}

	public void setUnscaledCanvasHeight(double unscaledCanvasHeight) {
		this.unscaledCanvasHeight = unscaledCanvasHeight;
	}

	public void setMinScaling(double minScaling) {
		this.minScaling = minScaling;
	}

	public void setDiscreteScaling(boolean discreteScaling) {
		this.discreteScaling = discreteScaling;
	}

	public void setCanvasBorderEnabled(boolean canvasBorderEnabled) {
		this.canvasBorderEnabled = canvasBorderEnabled;
	}

	public void setCanvasBorderColor(Color canvasBorderColor) {
		this.canvasBorderColor = canvasBorderColor;
	}

	protected void setSizes(Region region, double width, double height) {
		region.setMinSize(width, height);
		region.setMaxSize(width, height);
		region.setPrefSize(width, height);
	}

	public void setSize(double width, double height) {
		double shrink_width  = canvasBorderEnabled ? 0.8 : 1.0;
		double shrink_height = canvasBorderEnabled ? 0.9 : 1.0;

		double s = shrink_height * height / unscaledCanvasHeight;
		if (s * unscaledCanvasWidth > shrink_width * width) {
			s = shrink_width * width / unscaledCanvasWidth;
		}

		if (discreteScaling) {
			s = Math.floor(s * 10) / 10; // round scaling factor to first decimal digit
		}

		scalePage(s, false);
	}

	protected void scalePage(double newScaling, boolean always) {
		if (newScaling < minScaling) {
			Logger.error("Cannot scale to {}, minimum scaling is {}", newScaling, minScaling);
			return;
		}
		if (scaling == newScaling && !always) {
			// avoid useless scaling
			return;
		}
		scaling = newScaling;

		canvas.setWidth(unscaledCanvasWidth * scaling);
		canvas.setHeight(unscaledCanvasHeight * scaling);

		if (canvasBorderEnabled) {
			double w = Math.round((unscaledCanvasWidth + 25) * scaling);
			double h = Math.round((unscaledCanvasHeight + 15) * scaling);
			var roundedRect = new Rectangle(w, h);
			roundedRect.setArcWidth(26 * scaling);
			roundedRect.setArcHeight(26 * scaling);
			canvasContainer.setClip(roundedRect);

			double borderWidth = Math.max(5, Math.ceil(h / 55));
			double cornerRadius = Math.ceil(10 * scaling);
			var roundedBorder = new Border(
				new BorderStroke(canvasBorderColor,
					BorderStrokeStyle.SOLID,
					new CornerRadii(cornerRadius),
					new BorderWidths(borderWidth)));
			canvasContainer.setBorder(roundedBorder);
			setSizes(canvasContainer, w, h);
			Logger.trace("Canvas container resized: scaling: {}, canvas size: {000} x {000} px, border: {0} px", scaling,
				canvas.getWidth(), canvas.getHeight(), borderWidth);
		} else {
			canvasContainer.setBorder(null);
			setSizes(canvasContainer, canvas.getWidth(), canvas.getHeight());
			Logger.trace("Canvas container resized: scaling: {}, canvas size: {000} x {000} px, no border", scaling,
				canvas.getWidth(), canvas.getHeight());
		}
	}
}