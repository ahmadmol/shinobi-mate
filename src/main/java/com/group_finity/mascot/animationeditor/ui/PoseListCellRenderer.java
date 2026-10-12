package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.animationeditor.model.AnimationPose;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class PoseListCellRenderer extends DefaultListCellRenderer {
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof AnimationPose pose) {
         String htmlText = "<html><b>帧 " + (index + 1) + " / Frame " + (index + 1) + "</b><br/>图片 / Image: " + (pose.getImage() != null ? pose.getImage() : "无 / None") + "<br/>锚点 / Anchor: " + (pose.getImageAnchor() != null ? pose.getImageAnchor() : "0,0") + "<br/>速度 / Velocity: " + (pose.getVelocity() != null ? pose.getVelocity() : "0,0") + "<br/>持续时间 / Duration: " + pose.getDuration() + (pose.getSound() != null && !pose.getSound().isEmpty() ? "<br/>音效 / Sound: " + pose.getSound() : "") + "</html>";
         this.setText(htmlText);
      } else {
         this.setText(value != null ? value.toString() : "");
      }

      if (isSelected) {
         this.setBackground(list.getSelectionBackground());
         this.setForeground(list.getSelectionForeground());
      } else {
         this.setBackground(list.getBackground());
         this.setForeground(list.getForeground());
      }

      this.setOpaque(true);
      this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      return this;
   }

   public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();
      size.height = Math.max(size.height, 80);
      return size;
   }
}
