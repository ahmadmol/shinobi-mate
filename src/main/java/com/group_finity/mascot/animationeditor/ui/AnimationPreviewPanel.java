package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.AnimationPose;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class AnimationPreviewPanel extends JPanel {
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();
   private AnimationAction currentAction;
   private Timer animationTimer;
   private int currentPoseIndex = 0;
   private int currentFrameCount = 0;
   private boolean isPlaying = false;
   private boolean isSlowMotion = false;
   private File currentProjectDirectory;
   private JPanel previewArea;
   private JLabel imageLabel;
   private JPanel controlPanel;
   private JButton playButton;
   private JButton stopButton;
   private JButton slowMotionButton;
   private JLabel frameInfoLabel;
   private JProgressBar progressBar;

   public AnimationPreviewPanel() {
      this.initializeComponents();
      this.setupLayout();
      this.setupEventHandlers();
      this.setupAnimation();
   }

   private void initializeComponents() {
      this.previewArea = new JPanel(new BorderLayout());
      this.previewArea.setBackground(Color.LIGHT_GRAY);
      this.previewArea.setPreferredSize(new Dimension(300, 200));
      this.previewArea.setBorder(BorderFactory.createLoweredBevelBorder());
      this.imageLabel = new JLabel();
      this.imageLabel.setHorizontalAlignment(0);
      this.imageLabel.setVerticalAlignment(0);
      this.imageLabel.setText(this.languageBundle.getString("editor.preview.selectAnimation"));
      this.controlPanel = new JPanel(new FlowLayout());
      this.playButton = new JButton(this.languageBundle.getString("editor.preview.play"));
      this.playButton.setFont(new Font("SansSerif", 0, 13));
      this.stopButton = new JButton(this.languageBundle.getString("editor.preview.stop"));
      this.stopButton.setFont(new Font("SansSerif", 0, 13));
      this.slowMotionButton = new JButton(this.languageBundle.getString("editor.preview.slowPlay"));
      this.slowMotionButton.setFont(new Font("SansSerif", 0, 13));
      this.frameInfoLabel = new JLabel(this.languageBundle.getString("editor.preview.frameInfo") + " 0/0");
      this.frameInfoLabel.setFont(new Font("SansSerif", 0, 13));
      this.progressBar = new JProgressBar(0, 100);
      this.progressBar.setStringPainted(true);
      this.progressBar.setString("0%");
   }

   private void setupLayout() {
      this.setLayout(new BorderLayout());
      this.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.previewArea.add(this.imageLabel, "Center");
      this.add(this.previewArea, "Center");
      this.controlPanel.add(this.playButton);
      this.controlPanel.add(this.stopButton);
      this.controlPanel.add(this.slowMotionButton);
      this.controlPanel.add(Box.createHorizontalStrut(20));
      this.controlPanel.add(this.frameInfoLabel);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(this.controlPanel, "Center");
      bottomPanel.add(this.progressBar, "South");
      this.add(bottomPanel, "South");
   }

   private void setupEventHandlers() {
      this.playButton.addActionListener((e) -> {
         if (this.currentAction != null && !this.currentAction.getPoses().isEmpty()) {
            this.startAnimation(false);
         }

      });
      this.stopButton.addActionListener((e) -> this.stopAnimation());
      this.slowMotionButton.addActionListener((e) -> {
         if (this.currentAction != null && !this.currentAction.getPoses().isEmpty()) {
            this.startAnimation(true);
         }

      });
   }

   private void setupAnimation() {
      this.animationTimer = new Timer(40, (e) -> this.updateFrame());
   }

   public void setAction(AnimationAction action) {
      this.stopAnimation();
      this.currentAction = action;
      this.currentPoseIndex = 0;
      this.currentFrameCount = 0;
      this.updateDisplay();
      this.updateControls();
   }

   public void setProjectDirectory(File projectDirectory) {
      this.currentProjectDirectory = projectDirectory;
      if (this.currentAction != null) {
         this.updateDisplay();
      }

   }

   private void startAnimation(boolean slowMotion) {
      if (this.currentAction != null && !this.currentAction.getPoses().isEmpty()) {
         this.stopAnimation();
         this.isSlowMotion = slowMotion;
         this.isPlaying = true;
         this.currentPoseIndex = 0;
         this.currentFrameCount = 0;
         int delay = slowMotion ? 400 : 40;
         this.animationTimer.setDelay(delay);
         this.animationTimer.start();
         this.updateControls();
      }
   }

   private void stopAnimation() {
      if (this.animationTimer != null) {
         this.animationTimer.stop();
      }

      this.isPlaying = false;
      this.isSlowMotion = false;
      this.currentPoseIndex = 0;
      this.currentFrameCount = 0;
      this.updateDisplay();
      this.updateControls();
   }

   private void updateFrame() {
      if (this.currentAction != null && !this.currentAction.getPoses().isEmpty()) {
         List<AnimationPose> poses = this.currentAction.getPoses();
         AnimationPose currentPose = (AnimationPose)poses.get(this.currentPoseIndex);
         ++this.currentFrameCount;
         int frameDuration = this.isSlowMotion ? currentPose.getDuration() / 10 : currentPose.getDuration();
         if (frameDuration <= 0) {
            frameDuration = 1;
         }

         if (this.currentFrameCount >= frameDuration) {
            this.currentFrameCount = 0;
            this.currentPoseIndex = (this.currentPoseIndex + 1) % poses.size();
         }

         this.updateDisplay();
      } else {
         this.stopAnimation();
      }
   }

   private void updateDisplay() {
      if (this.currentAction != null && !this.currentAction.getPoses().isEmpty()) {
         List<AnimationPose> poses = this.currentAction.getPoses();
         AnimationPose currentPose = (AnimationPose)poses.get(this.currentPoseIndex);
         this.loadAndDisplayImage(currentPose);
         this.frameInfoLabel.setText(String.format(this.languageBundle.getString("editor.preview.frameInfo") + " %d/%d", this.currentPoseIndex + 1, poses.size()));
         int progress = !poses.isEmpty() ? this.currentPoseIndex * 100 / poses.size() : 0;
         this.progressBar.setValue(progress);
         this.progressBar.setString(progress + "%");
      } else {
         this.imageLabel.setIcon((Icon)null);
         this.imageLabel.setText(this.languageBundle.getString("editor.preview.selectAnimation"));
         this.frameInfoLabel.setText(this.languageBundle.getString("editor.preview.frameInfo") + " 0/0");
         this.progressBar.setValue(0);
         this.progressBar.setString("0%");
      }
   }

   private void loadAndDisplayImage(AnimationPose pose) {
      if (pose != null && pose.getImage() != null) {
         try {
            String imagePath = pose.getImage();
            if (imagePath.startsWith("/")) {
               imagePath = imagePath.substring(1);
            }

            File imageFile = this.findImageFile(imagePath);
            if (imageFile != null && imageFile.exists()) {
               BufferedImage image = ImageIO.read(imageFile);
               if (image != null) {
                  ImageIcon scaledIcon = this.scaleImageToFit(image, 280, 180);
                  this.imageLabel.setIcon(scaledIcon);
                  this.imageLabel.setText("");
               } else {
                  this.imageLabel.setIcon((Icon)null);
                  this.imageLabel.setText(this.languageBundle.getString("editor.preview.cannotLoadImage"));
               }
            } else {
               this.imageLabel.setIcon((Icon)null);
               JLabel var10000 = this.imageLabel;
               String var10001 = this.languageBundle.getString("editor.preview.imageNotFound");
               var10000.setText(var10001 + ": " + imagePath);
            }
         } catch (Exception var6) {
            this.imageLabel.setIcon((Icon)null);
            this.imageLabel.setText(this.languageBundle.getString("editor.preview.loadImageFailed"));
         }

      } else {
         this.imageLabel.setIcon((Icon)null);
         this.imageLabel.setText(this.languageBundle.getString("editor.pose.noImage"));
      }
   }

   private File findImageFile(String imageName) {
      File searchDir = this.currentProjectDirectory != null ? this.currentProjectDirectory : new File("./img");
      return !searchDir.exists() ? null : this.findFileRecursively(searchDir, imageName);
   }

   private File findFileRecursively(File directory, String fileName) {
      if (!directory.isDirectory()) {
         return null;
      } else {
         File[] files = directory.listFiles();
         if (files == null) {
            return null;
         } else {
            for(File file : files) {
               if (file.isFile() && file.getName().equals(fileName)) {
                  return file;
               }

               if (file.isDirectory()) {
                  File found = this.findFileRecursively(file, fileName);
                  if (found != null) {
                     return found;
                  }
               }
            }

            return null;
         }
      }
   }

   private ImageIcon scaleImageToFit(BufferedImage image, int maxWidth, int maxHeight) {
      int width = image.getWidth();
      int height = image.getHeight();
      double scaleX = (double)maxWidth / (double)width;
      double scaleY = (double)maxHeight / (double)height;
      double scale = Math.min(scaleX, scaleY);
      if (scale >= (double)1.0F) {
         return new ImageIcon(image);
      } else {
         int scaledWidth = (int)((double)width * scale);
         int scaledHeight = (int)((double)height * scale);
         Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, 4);
         return new ImageIcon(scaledImage);
      }
   }

   private void updateControls() {
      boolean hasAnimation = this.currentAction != null && !this.currentAction.getPoses().isEmpty();
      this.playButton.setEnabled(hasAnimation && !this.isPlaying);
      this.stopButton.setEnabled(hasAnimation && this.isPlaying);
      this.slowMotionButton.setEnabled(hasAnimation && !this.isPlaying);
      if (this.isPlaying) {
         if (this.isSlowMotion) {
            this.playButton.setText(this.languageBundle.getString("editor.preview.playingSlow"));
            this.slowMotionButton.setText(this.languageBundle.getString("editor.preview.slowPlaying"));
         } else {
            this.playButton.setText(this.languageBundle.getString("editor.preview.playing"));
            this.slowMotionButton.setText(this.languageBundle.getString("editor.preview.slowPlay"));
         }
      } else {
         this.playButton.setText(this.languageBundle.getString("editor.preview.play"));
         this.slowMotionButton.setText(this.languageBundle.getString("editor.preview.slowPlay"));
      }

   }

   public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      this.playButton.setEnabled(enabled);
      this.stopButton.setEnabled(enabled);
      this.slowMotionButton.setEnabled(enabled);
   }
}
