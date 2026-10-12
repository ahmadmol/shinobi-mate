package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.Project;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if (value instanceof DefaultMutableTreeNode node) {
         Object userObject = node.getUserObject();
         if (userObject instanceof Project project) {
            this.setText(project.getName());
            this.setIcon(this.createProjectIcon());
         } else if (userObject instanceof AnimationAction action) {
            String var10001 = action.getName();
            this.setText(var10001 + " (" + action.getType() + ")");
            this.setIcon(this.createActionIcon(action.getType()));
         } else {
            this.setText(userObject.toString());
         }
      }

      return this;
   }

   private Icon createProjectIcon() {
      return new ColorIcon(new Color(100, 149, 237), 16, 16);
   }

   private Icon createActionIcon(String actionType) {
      Color color;
      switch (actionType) {
         case "Stay" -> color = new Color(34, 139, 34);
         case "Move" -> color = new Color(255, 165, 0);
         case "Animate" -> color = new Color(220, 20, 60);
         case "Sequence" -> color = new Color(138, 43, 226);
         case "Select" -> color = new Color(255, 215, 0);
         case "Embedded" -> color = new Color(169, 169, 169);
         default -> color = new Color(105, 105, 105);
      }

      return new ColorIcon(color, 12, 12);
   }

   private static class ColorIcon implements Icon {
      private Color color;
      private int width;
      private int height;

      public ColorIcon(Color color, int width, int height) {
         this.color = color;
         this.width = width;
         this.height = height;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         Graphics2D g2d = (Graphics2D)g.create();
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setColor(this.color);
         g2d.fillOval(x, y, this.width, this.height);
         g2d.setColor(this.color.darker());
         g2d.drawOval(x, y, this.width, this.height);
         g2d.dispose();
      }

      public int getIconWidth() {
         return this.width;
      }

      public int getIconHeight() {
         return this.height;
      }
   }
}
