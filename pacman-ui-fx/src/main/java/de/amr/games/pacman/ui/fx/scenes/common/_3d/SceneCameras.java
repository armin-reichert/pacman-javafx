package de.amr.games.pacman.ui.fx.scenes.common._3d;

import java.util.EnumMap;

import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class SceneCameras {

	enum CameraType {
		STATIC, DYNAMIC, DYNAMIC_NEAR_PLAYER
	}

	private final SubScene scene;
	private EnumMap<CameraType, PerspectiveCamera> cams = new EnumMap<>(CameraType.class);
	private CameraType selection;

	public SceneCameras(SubScene scene) {
		this.scene = scene;
		for (CameraType cameraType : CameraType.values()) {
			cams.put(cameraType, new PerspectiveCamera(true));
			resetCam(cameraType);
			if (cameraType == CameraType.STATIC) {
				addCamController(cameraType);
			}
		}
		select(CameraType.STATIC);
	}

	private void resetCam(CameraType cameraType) {
		switch (cameraType) {
		case DYNAMIC:
			cams.get(CameraType.DYNAMIC).setNearClip(0.1);
			cams.get(CameraType.DYNAMIC).setFarClip(10000.0);
			cams.get(CameraType.DYNAMIC).setRotationAxis(Rotate.X_AXIS);
			cams.get(CameraType.DYNAMIC).setRotate(30);
			cams.get(CameraType.DYNAMIC).setTranslateZ(-250);

			break;
		case DYNAMIC_NEAR_PLAYER:
			cams.get(CameraType.DYNAMIC_NEAR_PLAYER).setNearClip(0.1);
			cams.get(CameraType.DYNAMIC_NEAR_PLAYER).setFarClip(10000.0);
			cams.get(CameraType.DYNAMIC_NEAR_PLAYER).setRotationAxis(Rotate.X_AXIS);
			cams.get(CameraType.DYNAMIC_NEAR_PLAYER).setRotate(60);
			cams.get(CameraType.DYNAMIC_NEAR_PLAYER).setTranslateZ(-60);
			break;
		case STATIC:
			cams.get(CameraType.STATIC).setNearClip(0.1);
			cams.get(CameraType.STATIC).setFarClip(10000.0);
			cams.get(CameraType.STATIC).setRotationAxis(Rotate.X_AXIS);
			cams.get(CameraType.STATIC).setRotate(30);
			cams.get(CameraType.STATIC).setTranslateX(0);
			cams.get(CameraType.STATIC).setTranslateY(270);
			cams.get(CameraType.STATIC).setTranslateZ(-460);
			break;
		default:
			break;
		}
	}

	public void select(CameraType cameraType) {
		selection = cameraType;
		resetCam(selection);
		scene.setCamera(selectedCamera());
	}

	public void selectNext() {
		CameraType[] types = CameraType.values();
		if (selection.ordinal() == types.length - 1) {
			select(types[0]);
		} else {
			select(types[selection.ordinal() + 1]);
		}
	}

	public CameraType selection() {
		return selection;
	}

	public PerspectiveCamera selectedCamera() {
		return cams.get(selection);
	}

	private void addCamController(CameraType cameraType) {
		CameraController cameraController = new CameraController(cams.get(cameraType));
		scene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
	}
}