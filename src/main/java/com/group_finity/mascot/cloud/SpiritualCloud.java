package com.group_finity.mascot.cloud;

import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.random.RandomGenerator;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/** One reusable window per mascot, updated on the EDT from the existing engine tick. */
public final class SpiritualCloud {
    private final Component mascotComponent;
    private final AtomicBoolean updateQueued = new AtomicBoolean();
    private volatile boolean disposed;
    private volatile boolean enabled;
    private volatile boolean active;
    private static List<Rectangle> cachedMonitors = List.of();
    private static long refreshMonitorsAt;
    private CloudState state = new CloudState(List.of(), RandomGenerator.getDefault());
    private JWindow window;
    private CloudPanel panel;

    public SpiritualCloud(Component mascotComponent, String imageSet) {
        this.mascotComponent = mascotComponent;
        reload(imageSet);
    }

    public boolean isEnabled() { return enabled && !disposed && com.group_finity.mascot.settings.RuntimeSettings.flag("CloudEnabled",true); }
    private static long now() { return System.nanoTime() / 1_000_000; }
    private static void onEdt(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) task.run();
        else SwingUtilities.invokeLater(task);
    }

    public void reload(String imageSet) {
        List<String> messages = CloudMessages.load(imageSet);
        enabled = messages.size() >= 2;
        com.group_finity.mascot.RuntimeDiagnostics.event(enabled?"CLOUD_MESSAGES_LOADED":"CLOUD_MESSAGES_UNAVAILABLE",messages.size());
        onEdt(() -> {
            if (disposed) return;
            state = new CloudState(messages, RandomGenerator.getDefault());
            active = false;
            if (window != null) window.setVisible(false);
        });
    }

    public void mouseEnter() { onEdt(() -> { if (!disposed) { state.mouseEnter(now()); render(); } }); }
    public void mouseExit() { onEdt(() -> { if (!disposed) { state.mouseExit(); render(); } }); }
    public void click() { onEdt(() -> { if (!disposed) { state.click(now()); render(); } }); }

    /** Coalesce slow EDT updates; do not accumulate an event per engine frame. */
    public void update() {
        if (!enabled || disposed || !active || !updateQueued.compareAndSet(false, true)) return;
        SwingUtilities.invokeLater(() -> {
            updateQueued.set(false);
            if (!disposed) render();
        });
    }

    private void render() {
        if(!isEnabled()) {if(window!=null)window.setVisible(false);return;}
        state.setIntervalMillis((long)(com.group_finity.mascot.settings.RuntimeSettings.number("CloudIntervalSeconds",6,3,120)*1000),now());
        state.update(now());
        active = state.visibility() != CloudState.Visibility.HIDDEN || state.opacity() > 0;
        if (!state.isEnabled() || (state.visibility() == CloudState.Visibility.HIDDEN && state.opacity() == 0)
                || !mascotComponent.isShowing()) {
            if (window != null && window.isVisible()) window.setVisible(false);
            return;
        }
        Rectangle mascot;
        try {
            mascot = new Rectangle(mascotComponent.getLocationOnScreen(), mascotComponent.getSize());
        } catch (IllegalComponentStateException ex) {
            // The engine can dispose/hide the native mascot between the two observations.
            if (window != null) window.setVisible(false);
            return;
        }
        Rectangle monitor = CloudPlacement.monitorFor(mascot, monitors());
        if (window == null) createWindow();
        boolean onTop=com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true);
        if(window.isAlwaysOnTop()!=onTop)window.setAlwaysOnTop(onTop);
        panel.setTextSize((float)com.group_finity.mascot.settings.RuntimeSettings.number("CloudTextSize",16,12,26));
        panel.setMessage(state.message(), monitor.width);
        Rectangle bounds = CloudPlacement.place(mascot, panel.getPreferredSize(), monitor);
        if (!window.getBounds().equals(bounds)) window.setBounds(bounds);
        panel.setOpacity(state.opacity()*(float)com.group_finity.mascot.settings.RuntimeSettings.number("CloudOpacity",1,.2,1));
        if (!window.isVisible()) window.setVisible(true);
    }

    /** Shared EDT-only cache, refreshed for monitor hot-plug/taskbar changes. */
    private static List<Rectangle> monitors() {
        long time = now();
        if (!cachedMonitors.isEmpty() && time < refreshMonitorsAt) return cachedMonitors;
        List<Rectangle> monitors = new ArrayList<>();
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            GraphicsConfiguration configuration = device.getDefaultConfiguration();
            Rectangle bounds = new Rectangle(configuration.getBounds());
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
            monitors.add(bounds);
        }
        cachedMonitors = List.copyOf(monitors);
        refreshMonitorsAt = time + 1_000;
        return cachedMonitors;
    }

    private void createWindow() {
        window = new JWindow();
        window.setName("SpiritualCloud");
        window.setFocusableWindowState(false);
        window.setAutoRequestFocus(false);
        window.setAlwaysOnTop(com.group_finity.mascot.settings.RuntimeSettings.flag("AlwaysOnTop",true));
        window.setBackground(new Color(0, 0, 0, 0));
        panel = new CloudPanel();
        window.setContentPane(panel);
        window.pack();
        if (com.sun.jna.Platform.isWindows()) {
            // Ignore mouse hit-testing, including when clamping overlaps a mascot.
            Pointer handle = Native.getComponentPointer(window);
            int style = User32.INSTANCE.GetWindowLongW(handle, -20);
            User32.INSTANCE.SetWindowLongW(handle, -20, style | 0x20 | 0x08000000);
        }
    }

    public void dispose() {
        disposed = true;
        enabled = false;
        active = false;
        onEdt(() -> {
            if (window != null) { window.dispose(); window = null; panel = null; }
            state = new CloudState(List.of(), RandomGenerator.getDefault());
        });
    }
}
