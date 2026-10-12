package com.group_finity.mascot.ui;
import com.group_finity.mascot.settings.RuntimeSettings;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/** Shared spacing, typography and rounded surfaces for all new product windows. */
public final class Theme {
 public static final int GAP=12,CARD_RADIUS=18;
 public static final Color ACCENT=new Color(119,103,238);
 private Theme() { }
 public static void defaults(){
  UIManager.put("defaultFont",new Font("Segoe UI",Font.PLAIN,16));
  UIManager.put("Button.arc",14);UIManager.put("Component.arc",12);
  UIManager.put("TabbedPane.tabHeight",42);
 }
 public static String t(String ar,String en){return RuntimeSettings.label(ar,en);}
 public static JPanel card() {
  JPanel p=new CardPanel();
  p.setOpaque(false);p.setBackground(UIManager.getColor("TextField.background"));
  p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBorder(new EmptyBorder(18,20,18,20));return p;
 }
 private static final class CardPanel extends JPanel implements Scrollable {
   @Override protected void paintComponent(Graphics g){
    Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());g2.fillRoundRect(0,0,getWidth(),getHeight(),CARD_RADIUS,CARD_RADIUS);g2.dispose();
   }
   public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
   public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return 24;}
   public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return r.height-24;}
   public boolean getScrollableTracksViewportWidth(){return true;}
   public boolean getScrollableTracksViewportHeight(){return false;}
 }
 public static void orient(Component component){
  component.applyComponentOrientation(RuntimeSettings.arabic()?ComponentOrientation.RIGHT_TO_LEFT:ComponentOrientation.LEFT_TO_RIGHT);
 }
 public static JLabel title(String value){JLabel label=new JLabel(value);label.setFont(label.getFont().deriveFont(Font.BOLD,22f));return label;}
 public static JLabel text(String value){JLabel label=new JLabel(value);label.setAlignmentX(RuntimeSettings.arabic()?Component.RIGHT_ALIGNMENT:Component.LEFT_ALIGNMENT);label.setMaximumSize(new Dimension(Integer.MAX_VALUE,label.getPreferredSize().height));return label;}
 public static JButton button(String value){JButton b=new JButton(value);b.putClientProperty("JButton.buttonType","roundRect");b.addActionListener(e->com.group_finity.mascot.audio.AudioManager.play("ui.click"));return b;}
 public static void fit(Window window,int width,int height) {
  AppIcon.apply(window);
  var gc=window.getGraphicsConfiguration();Rectangle r=new Rectangle(gc.getBounds());
  Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(gc);
  r.x+=insets.left;r.y+=insets.top;r.width-=insets.left+insets.right;r.height-=insets.top+insets.bottom;
  window.setSize(Math.max(1,Math.min(width,r.width-40)),Math.max(1,Math.min(height,r.height-40)));window.setLocationRelativeTo(window.getOwner());
  window.setLocation(Math.max(r.x,Math.min(window.getX(),r.x+r.width-window.getWidth())),Math.max(r.y,Math.min(window.getY(),r.y+r.height-window.getHeight())));
 }
}
