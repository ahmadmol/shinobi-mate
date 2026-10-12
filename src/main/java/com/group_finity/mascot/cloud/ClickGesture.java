package com.group_finity.mascot.cloud;

import java.awt.Point;

/** Screen-coordinate displacement avoids misclassifying a moving mascot as mouse movement. */
public final class ClickGesture {
    public static final int DRAG_THRESHOLD = 6;
    private Point pressedAt;
    private boolean moved;

    public void press(Point point) { pressedAt = new Point(point); moved = false; }
    public boolean isPressed() { return pressedAt != null; }
    public boolean move(Point point) {
        if (pressedAt != null && pressedAt.distanceSq(point) >= DRAG_THRESHOLD * DRAG_THRESHOLD) moved = true;
        return moved;
    }
    public boolean release(Point point) {
        move(point);
        boolean clicked = pressedAt != null && !moved;
        cancel();
        return clicked;
    }
    public void cancel() { pressedAt = null; moved = false; }
}
