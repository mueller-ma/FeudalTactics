package com.sesu8642.feudaltactics.input;

import javax.inject.Inject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.eventbus.EventBus;
import com.sesu8642.feudaltactics.dagger.qualifierannotations.IngameCamera;
import com.sesu8642.feudaltactics.events.BackInputEvent;
import com.sesu8642.feudaltactics.events.EscInputEvent;
import com.sesu8642.feudaltactics.events.TapInputEvent;

/** Class that handles touch as well as gesture inputs. **/
public class CombinedInputProcessor implements GestureListener, InputProcessor {

	private EventBus eventBus;
	private OrthographicCamera camera;

	public static final float MIN_ZOOM = 0.01F;
	public static final float MAX_ZOOM = 1;

	@Inject
	public CombinedInputProcessor(EventBus eventBus, @IngameCamera OrthographicCamera camera) {
		this.eventBus = eventBus;
		this.camera = camera;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		float adjAmount = (amountY * camera.zoom) / 3;
		if ((adjAmount < 0 && camera.zoom + adjAmount > MIN_ZOOM)
				|| (adjAmount > 0 && camera.zoom + adjAmount < MAX_ZOOM)) {
			Vector3 oldMousePosition = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			camera.zoom += adjAmount;
			camera.update();
			Vector3 newMousePosition = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			camera.translate(oldMousePosition.sub(newMousePosition));
			camera.update();
		}
		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		Vector3 worldDistance = camera.unproject(new Vector3(x, y, 0))
				.sub(camera.unproject(new Vector3(x + deltaX, y + deltaY, 0)));
		camera.translate(worldDistance);
		camera.update();
		return true;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}

	// TODO: move this logic somwhere else
	@Override
	public boolean tap(float x, float y, int count, int button) {
		Vector3 fullWorldCoords = camera.unproject(new Vector3(x, y, 0));
		Vector2 worldCoords = new Vector2(fullWorldCoords.x, fullWorldCoords.y);
		eventBus.post(new TapInputEvent(worldCoords));
		return true;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	Float cameraZoomBeforePinch = null;

	// TODO: move this logic somwhere else
	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		if (cameraZoomBeforePinch == null) {
			cameraZoomBeforePinch = camera.zoom;
		}
		float initialDistance = initialPointer1.dst(initialPointer2);
		float currentDistance = pointer1.dst(pointer2);
		float zoomAmount = initialDistance / currentDistance;
		float newZoom = cameraZoomBeforePinch * zoomAmount;
		if ((zoomAmount < 1 && newZoom > MIN_ZOOM) || (zoomAmount > 1 && newZoom < MAX_ZOOM)) {
			Vector2 oldPointerCenter = new Vector2((pointer1.x + pointer2.x) / 2, (pointer1.y + pointer2.y) / 2);
			Vector3 oldPointerCenterInWorld = camera.unproject(new Vector3(oldPointerCenter, 0));
			camera.zoom = newZoom;
			camera.update();
			Vector3 newPointerCenterInWorld = camera.unproject(new Vector3(oldPointerCenter, 0));
			camera.translate(oldPointerCenterInWorld.sub(newPointerCenterInWorld));
			camera.update();
		}
		return true;
	}

	@Override
	public void pinchStop() {
		cameraZoomBeforePinch = null;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.ESCAPE:
			eventBus.post(new EscInputEvent());
			break;
		case Keys.BACK:
			eventBus.post(new BackInputEvent());
			break;
		default:
			// noop: ignore all other keys
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
}
