package com.group_finity.mascot.imagesetchooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class ImageSetChooserPanel extends JPanel {
   String imageSet;
   private JLabel actionsFile;
   private JLabel behaviorsFile;
   private JLabel caption;
   private JCheckBox checkbox;
   private JLabel image;

   public ImageSetChooserPanel() {
      this.initComponents();
   }

   public ImageSetChooserPanel(String var1, String var2, String var3, String var4, String var5) {
      this.initComponents();
      this.imageSet = var1;
      this.setName(var1);
      this.getAccessibleContext().setAccessibleName(var1);
      this.caption.setText(var5);
      this.caption.setFont(new Font("Segoe UI", 1, 14));
      this.caption.setToolTipText(var1);
      this.actionsFile.setText(var2);
      this.behaviorsFile.setText(var3);
      this.actionsFile.setVisible(false);
      this.behaviorsFile.setVisible(false);

      try {
         BufferedImage var6 = ImageIO.read(new File(var4));
         this.image.setIcon(new ImageIcon(var6.getScaledInstance(60, 60, 1)));
      } catch (Exception var7) {
      }

   }

   public void setCheckbox(boolean var1) {
      this.checkbox.setSelected(var1);
   }

   public String getImageSetName() {
      return this.imageSet;
   }

   private void initComponents() {
      this.checkbox = new JCheckBox();
      this.caption = new JLabel();
      this.actionsFile = new JLabel();
      this.behaviorsFile = new JLabel();
      this.image = new JLabel();
      this.setBorder(BorderFactory.createEtchedBorder());
      this.setMinimumSize(new Dimension(340, 80));
      this.setPreferredSize(new Dimension(340, 80));
      this.setLayout(new AbsoluteLayout());
      this.checkbox.setOpaque(false);
      this.add(this.checkbox, new AbsoluteConstraints(10, 30, -1, -1));
      this.caption.setText("Builder");
      this.add(this.caption, new AbsoluteConstraints(110, 10, -1, -1));
      this.actionsFile.setText("img/Builder/conf/actionsxml");
      this.add(this.actionsFile, new AbsoluteConstraints(110, 30, -1, -1));
      this.behaviorsFile.setText("img/Builder/conf/behaviors.xml");
      this.add(this.behaviorsFile, new AbsoluteConstraints(110, 50, -1, -1));
      this.image.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
      this.add(this.image, new AbsoluteConstraints(40, 10, 60, 60));
   }
}
