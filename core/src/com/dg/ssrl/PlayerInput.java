package com.dg.ssrl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * Created by magnus.ornebring on 31/05/15.
 */
public class PlayerInput extends InputAdapter implements GestureDetector.GestureListener {
    private static final String tag = "PlayerInput";

    public enum Action {
        FIRE,
        BOMB
    }

    private static final double ONE_PI_EIGHTS = (Math.PI) / 8;
    private static final double THREE_PI_EIGHTS = (3 * Math.PI) / 8;
    private static final double FIVE_PI_EIGHTS = (5 * Math.PI) / 8;
    private static final double SEVEN_PI_EIGHTS = (7 * Math.PI) / 8;
    private static final double NINE_PI_EIGHTS = (9 * Math.PI) / 8;
    private static final double ELEVEN_PI_EIGHTS = (11 * Math.PI) / 8;
    private static final double THIRTEEN_PI_EIGHTS = (13 * Math.PI) / 8;
    private static final double FIFTEEN_PI_EIGHTS = (15 * Math.PI) / 8;


    private int touchSlop = 8;
    private int touchSlopSquare = touchSlop * touchSlop;

    private Vector2 pointer = new Vector2();
    private Vector2 pointerDown = new Vector2();
    private boolean isWithinTapSquare;
    private int pointerId = -1;


    private boolean swiped;

    private Direction movementDirection = Direction.NONE;

    private ArrayList<Action> actionQueue = new ArrayList<Action>();


    public Direction getMovementDirection() {
        return movementDirection;
    }

    public Action popAction() {
        if (actionQueue.size() > 0) {
            return actionQueue.remove(0);
        }
        return null;
    }

    @Override
    public boolean keyDown (int keycode) {

        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.LEFT:
                setMovementDirection(Direction.WEST);
                break;
            case com.badlogic.gdx.Input.Keys.RIGHT:
                setMovementDirection(Direction.EAST);
                break;
            case com.badlogic.gdx.Input.Keys.UP:
                setMovementDirection(Direction.NORTH);
                break;
            case com.badlogic.gdx.Input.Keys.DOWN:
                setMovementDirection(Direction.SOUTH);
                break;
        }
        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp (int keycode) {
        switch (keycode) {
            case com.badlogic.gdx.Input.Keys.LEFT:
            case com.badlogic.gdx.Input.Keys.RIGHT:
            case com.badlogic.gdx.Input.Keys.UP:
            case com.badlogic.gdx.Input.Keys.DOWN:
                movementDirection = Direction.NONE;
                break;
            case Input.Keys.SPACE:
                actionQueue.add(Action.FIRE);
                break;
        }
        return super.keyUp(keycode);
    }


    private void setMovementDirection(Direction direction) {
        movementDirection = direction;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointerId == -1) {
            pointerId = pointer;
            this.pointer.set(x, y);
            this.pointerDown.set(x,y);
            isWithinTapSquare = true;
        }

        return super.touchDown(x,y,pointer,button);
    }

    private boolean isWithinTapSquare(int x, int y) {
        float dx = x - this.pointerDown.x;
        float dy = y - this.pointerDown.y;
        float distanceSquared = (dx * dx) + (dy * dy);

        return distanceSquared < touchSlopSquare;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        if(pointer == pointerId) {
            swiped = false;
            pointerId = -1;
            movementDirection = Direction.NONE;
        }
        return super.touchUp(x, y, pointer, button);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (pointerId == pointer) {
            float dx = x - this.pointer.x;
            float dy = y - this.pointer.y;
            float distanceSquared = (dx * dx) + (dy * dy);

            isWithinTapSquare = isWithinTapSquare(x,y);

            if (distanceSquared > touchSlopSquare) {
                // considered moved finger
                double angle = Math.atan2(dy, -dx);
                if (angle < 0) {
                    angle = 2d * Math.PI + angle;
                }

                Direction swipeDirection = directionFromAngle(angle);
                if(swipeDirection != Direction.NONE && !swiped) {
                    swiped = true;
                    setMovementDirection(swipeDirection);
                }

            }
            this.pointer.set(x, y);
        }
        return super.touchDragged(x, y, pointer);
    }

    private Direction directionFromAngle(double angle) {
        if (angle > FIFTEEN_PI_EIGHTS || angle < ONE_PI_EIGHTS) {
            return Direction.WEST;
        } else if (angle > ONE_PI_EIGHTS && angle < THREE_PI_EIGHTS) {
            // return Direction.NORTHWEST;
        } else if (angle > THREE_PI_EIGHTS && angle < FIVE_PI_EIGHTS) {
            return Direction.SOUTH;
        } else if (angle > FIVE_PI_EIGHTS && angle < SEVEN_PI_EIGHTS) {
            //return Direction.NORTHEAST;
        } else if (angle > SEVEN_PI_EIGHTS && angle < NINE_PI_EIGHTS) {
            return Direction.EAST;
        } else if (angle > NINE_PI_EIGHTS && angle < ELEVEN_PI_EIGHTS) {
            //return Direction.SOUTHEAST;
        } else if (angle > ELEVEN_PI_EIGHTS && angle < THIRTEEN_PI_EIGHTS) {
            return Direction.NORTH;
        } else if (angle > THIRTEEN_PI_EIGHTS && angle < FIFTEEN_PI_EIGHTS) {
            //return Direction.SOUTHWEST;
        }

        return Direction.NONE;
    }


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if(count == 1) {
            actionQueue.add(Action.FIRE);
            return true;
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        actionQueue.add(Action.BOMB);
        return true;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
