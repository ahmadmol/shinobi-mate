package com.group_finity.mascot.cloud;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.accessibility.AccessibleContext;

/** Vector cloud silhouette with Java2D Arabic shaping, bidi, and centered word wrapping. */
public final class CloudPanel extends JComponent {
    public static final int MIN_WIDTH = 160;
    public static final int MAX_WIDTH = 340;
    private static final int HORIZONTAL_PADDING = 72;
    private static final int VERTICAL_PADDING = 54;
    private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(null, true, true);
    private List<TextLayout> lines = List.of();
    private float textHeight;
    private float opacity;
    private String message = "";
    private int widthLimit = MAX_WIDTH;
    private float textSize=16;
    public void setTextSize(float value){if(textSize!=value){textSize=value;message="";}}

    public CloudPanel() {
        setOpaque(false);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }

    @Override public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) accessibleContext = new AccessibleJComponent() { };
        return accessibleContext;
    }

    public void setMessage(String text, int availableWidth) {
        int limit = Math.min(MAX_WIDTH, Math.max(90, availableWidth));
        if (text.equals(message) && widthLimit == limit) return;
        message = text;
        widthLimit = limit;
        Font base = UIManager.getFont("Label.font");
        if (base == null) base = new Font(Font.DIALOG, Font.PLAIN, 16);
        Font font = base.deriveFont(Font.PLAIN, textSize);
        if (font.canDisplayUpTo(text) != -1) font = new Font(Font.DIALOG, Font.PLAIN, (int)textSize);
        List<TextLayout> wrapped;
        int width;
        do {
            AttributedString attributed = attributed(text, font);
            float advance = text.isEmpty() ? 0 : new TextLayout(attributed.getIterator(), FONT_CONTEXT).getAdvance();
            width = Math.min(limit, Math.max(Math.min(MIN_WIDTH, limit), (int) Math.ceil(advance) + HORIZONTAL_PADDING));
            wrapped = wrap(attributed, text.length(), Math.max(12, width - HORIZONTAL_PADDING));
            if (wrapped.size() <= 3 || font.getSize2D() <= 13f) break;
            font = font.deriveFont(font.getSize2D() - 1);
        } while (true);
        lines = List.copyOf(wrapped);
        textHeight = 0;
        for (TextLayout line : lines) textHeight += line.getAscent() + line.getDescent() + line.getLeading() + 3;
        setPreferredSize(new Dimension(width, Math.max(84, (int) Math.ceil(textHeight) + VERTICAL_PADDING)));
        if (getAccessibleContext() != null) getAccessibleContext().setAccessibleName(text);
        revalidate();
        repaint();
    }

    private static AttributedString attributed(String text, Font font) {
        AttributedString value = new AttributedString(text);
        if (!text.isEmpty()) {
            value.addAttribute(TextAttribute.FONT, font);
            value.addAttribute(TextAttribute.RUN_DIRECTION,new java.text.Bidi(text,java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).baseIsLeftToRight()?TextAttribute.RUN_DIRECTION_LTR:TextAttribute.RUN_DIRECTION_RTL);
            value.addAttribute(TextAttribute.LANGUAGE, Locale.forLanguageTag("ar"));
        }
        return value;
    }

    private static List<TextLayout> wrap(AttributedString text, int length, int width) {
        List<TextLayout> result = new ArrayList<>();
        LineBreakMeasurer measurer = new LineBreakMeasurer(text.getIterator(),
                BreakIterator.getLineInstance(Locale.forLanguageTag("ar")), FONT_CONTEXT);
        while (measurer.getPosition() < length) result.add(measurer.nextLayout(width));
        return result;
    }

    public void setOpacity(float value) {
        if (opacity != value) { opacity = value; repaint(); }
    }

    static Path2D cloudShape(int width, int height) {
        Path2D shape = new Path2D.Double();
        shape.moveTo(.12, .32);
        shape.curveTo(.08, .12, .20, .07, .28, .17);
        shape.curveTo(.32, -.01, .44, -.01, .50, .13);
        shape.curveTo(.57, -.01, .70, .01, .74, .18);
        shape.curveTo(.83, .07, .95, .17, .91, .35);
        shape.curveTo(1.02, .40, 1.01, .66, .91, .72);
        shape.curveTo(.95, .89, .83, 1.00, .75, .88);
        shape.curveTo(.69, 1.03, .56, 1.01, .50, .90);
        shape.curveTo(.43, 1.03, .30, 1.01, .25, .88);
        shape.curveTo(.14, 1.00, .04, .88, .08, .73);
        shape.curveTo(-.02, .66, -.01, .40, .12, .32);
        shape.closePath();
        shape.transform(new AffineTransform(width - 14, 0, 0, height - 14, 7, 5));
        return shape;
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Path2D cloud = cloudShape(getWidth(), getHeight());
            g.translate(0, 3);
            if(com.group_finity.mascot.performance.PerformanceProfile.current().shadows() && com.group_finity.mascot.settings.RuntimeSettings.flag("Shadows",true)) {
            g.setColor(new Color(65, 100, 125, 17));
            g.setStroke(new BasicStroke(4f));
            g.draw(cloud);
            g.fill(cloud);
            }
            g.translate(0, -3);
            g.setColor(new Color(255, 255, 255, 244));
            g.fill(cloud);
            g.setStroke(new BasicStroke(1.6f));
            g.setColor(new Color(162, 216, 249, 225));
            g.draw(cloud);
            g.setColor(new Color(48, 73, 94));
            float y = (getHeight() - textHeight) / 2f;
            for (TextLayout line : lines) {
                y += line.getAscent();
                java.awt.geom.Rectangle2D ink = line.getBounds();
                line.draw(g, (float) ((getWidth() - ink.getWidth()) / 2 - ink.getX()), y);
                y += line.getDescent() + line.getLeading() + 3;
            }
        } finally { g.dispose(); }
    }
}
