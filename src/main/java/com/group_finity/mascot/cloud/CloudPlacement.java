package com.group_finity.mascot.cloud;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/** Select an actual monitor, not the bounding rectangle spanning gaps between monitors. */
public final class CloudPlacement {
    public static final int MARGIN = 8;
    private CloudPlacement() { }

    public static Rectangle monitorFor(Rectangle mascot, List<Rectangle> monitors) {
        Point center = new Point(mascot.x + mascot.width / 2, mascot.y + mascot.height / 2);
        Rectangle best = monitors.getFirst();
        long bestArea = -1;
        double bestDistance = Double.MAX_VALUE;
        for (Rectangle monitor : monitors) {
            Rectangle overlap = monitor.intersection(mascot);
            long area = overlap.isEmpty() ? 0 : (long) overlap.width * overlap.height;
            double dx = Math.max(monitor.x - center.x, Math.max(0, center.x - monitor.getMaxX()));
            double dy = Math.max(monitor.y - center.y, Math.max(0, center.y - monitor.getMaxY()));
            double distance = dx * dx + dy * dy;
            if (area > bestArea || (area == bestArea && distance < bestDistance)) {
                best = monitor;
                bestArea = area;
                bestDistance = distance;
            }
        }
        return new Rectangle(best);
    }

    public static Rectangle place(Rectangle mascot, Dimension size, Rectangle monitor) {
        int width = Math.min(size.width, monitor.width);
        int height = Math.min(size.height, monitor.height);
        int x = mascot.x + (mascot.width - width) / 2;
        int y = mascot.y - height - MARGIN;
        if (y < monitor.y) {
            y = mascot.y + mascot.height + MARGIN;
            if (y + height > monitor.y + monitor.height) {
                x = mascot.x + mascot.width + MARGIN;
                if (x + width > monitor.x + monitor.width) x = mascot.x - width - MARGIN;
                y = mascot.y + (mascot.height - height) / 2;
            }
        }
        x = Math.max(monitor.x, Math.min(x, monitor.x + monitor.width - width));
        y = Math.max(monitor.y, Math.min(y, monitor.y + monitor.height - height));
        return new Rectangle(x, y, width, height);
    }
}
