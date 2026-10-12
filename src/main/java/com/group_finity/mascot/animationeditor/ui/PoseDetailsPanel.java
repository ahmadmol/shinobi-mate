package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animationeditor.model.AnimationPose;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PoseDetailsPanel extends JPanel {
   private AnimationPose currentPose;
   private List<Runnable> changeListeners = new ArrayList<>();
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();
   private File currentProjectDirectory;
   private JTextField imageField;
   private JButton browseImageButton;
   private JTextField imageAnchorField;
   private JTextField velocityField;
   private JSpinner durationSpinner;
   private JTextField soundField;
   private JButton browseSoundButton;
   private JSlider volumeSlider;
   private JLabel volumeLabel;

   public PoseDetailsPanel() {
      this.initializeComponents();
      this.setupLayout();
      this.setupEventHandlers();
      this.setPose((AnimationPose)null);
   }

   private void initializeComponents() {
      this.imageField = new JTextField();
      this.imageField.setFont(new Font("SansSerif", 0, 15));
      this.browseImageButton = new JButton(this.languageBundle.getString("editor.button.browse"));
      this.browseImageButton.setFont(new Font("SansSerif", 0, 15));
      this.imageAnchorField = new JTextField();
      this.imageAnchorField.setFont(new Font("SansSerif", 0, 15));
      this.imageAnchorField.setToolTipText("Format: x,y (e.g., 64,128) - defines the anchor point of the image");
      this.velocityField = new JTextField();
      this.velocityField.setFont(new Font("SansSerif", 0, 15));
      this.velocityField.setToolTipText("Format: x,y (e.g., 5,0) - defines the movement velocity in pixels");
      this.durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
      this.durationSpinner.setFont(new Font("SansSerif", 0, 15));
      this.soundField = new JTextField();
      this.soundField.setFont(new Font("SansSerif", 0, 15));
      this.browseSoundButton = new JButton(this.languageBundle.getString("editor.button.browse"));
      this.browseSoundButton.setFont(new Font("SansSerif", 0, 15));
      this.volumeSlider = new JSlider(0, 100, 100);
      this.volumeLabel = new JLabel("100%");
      this.volumeLabel.setFont(new Font("SansSerif", 0, 15));
      this.volumeSlider.addChangeListener((e) -> {
         int value = this.volumeSlider.getValue();
         this.volumeLabel.setText(value + "%");
      });
   }

   private void setupLayout() {
      this.setLayout(new GridBagLayout());
      this.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("editor.panel.poseDetails")));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = 17;
      Font labelFont = new Font("SansSerif", 0, 15);
      gbc.gridx = 0;
      gbc.gridy = 0;
      JLabel imageLabel = new JLabel(this.languageBundle.getString("editor.label.image"));
      imageLabel.setFont(labelFont);
      this.add(imageLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.imageField, gbc);
      gbc.gridx = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      this.add(this.browseImageButton, gbc);
      gbc.gridx = 0;
      gbc.gridy = 1;
      JLabel anchorLabel = new JLabel(this.languageBundle.getString("editor.label.imageAnchor"));
      anchorLabel.setFont(labelFont);
      this.add(anchorLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.imageAnchorField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.gridwidth = 1;
      JLabel velocityLabel = new JLabel(this.languageBundle.getString("editor.label.velocity"));
      velocityLabel.setFont(labelFont);
      this.add(velocityLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.velocityField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      JLabel durationLabel = new JLabel(this.languageBundle.getString("editor.label.durationFrames"));
      durationLabel.setFont(labelFont);
      this.add(durationLabel, gbc);
      gbc.gridx = 1;
      gbc.gridwidth = 2;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.durationSpinner, gbc);
      gbc.gridx = 0;
      gbc.gridy = 4;
      gbc.gridwidth = 3;
      gbc.fill = 2;
      JLabel durationExplanation = new JLabel(this.languageBundle.getString("editor.label.durationTip"));
      durationExplanation.setFont(new Font("SansSerif", 2, 11));
      durationExplanation.setForeground(Color.GRAY);
      this.add(durationExplanation, gbc);
      gbc.gridx = 0;
      gbc.gridy = 5;
      gbc.gridwidth = 1;
      JLabel soundLabel = new JLabel(this.languageBundle.getString("editor.label.sound"));
      soundLabel.setFont(labelFont);
      this.add(soundLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.soundField, gbc);
      gbc.gridx = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      this.add(this.browseSoundButton, gbc);
      gbc.gridx = 0;
      gbc.gridy = 6;
      gbc.gridwidth = 1;
      JLabel volumeSliderLabel = new JLabel(this.languageBundle.getString("editor.label.volume"));
      volumeSliderLabel.setFont(labelFont);
      this.add(volumeSliderLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      this.add(this.volumeSlider, gbc);
      gbc.gridx = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      this.add(this.volumeLabel, gbc);
   }

   private void setupEventHandlers() {
      this.imageField.addActionListener((e) -> this.updatePoseFromUI());
      this.imageField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            PoseDetailsPanel.this.updatePoseFromUI();
         }
      });
      this.imageAnchorField.addActionListener((e) -> this.updatePoseFromUI());
      this.imageAnchorField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            PoseDetailsPanel.this.updatePoseFromUI();
         }
      });
      this.velocityField.addActionListener((e) -> this.updatePoseFromUI());
      this.velocityField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            PoseDetailsPanel.this.updatePoseFromUI();
         }
      });
      this.durationSpinner.addChangeListener((e) -> this.updatePoseFromUI());
      this.soundField.addActionListener((e) -> this.updatePoseFromUI());
      this.soundField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            PoseDetailsPanel.this.updatePoseFromUI();
         }
      });
      this.volumeSlider.addChangeListener((e) -> this.updatePoseFromUI());
      this.browseImageButton.addActionListener((e) -> this.browseForImage());
      this.browseSoundButton.addActionListener((e) -> this.browseForSound());
   }

   public void setPose(AnimationPose pose) {
      this.currentPose = pose;
      this.updateUIFromPose();
   }

   private void updateUIFromPose() {
      if (this.currentPose == null) {
         this.imageField.setText("");
         this.imageAnchorField.setText("");
         this.velocityField.setText("");
         this.durationSpinner.setValue(1);
         this.soundField.setText("");
         this.volumeSlider.setValue(100);
         this.volumeLabel.setText("100%");
         this.setEnabled(false);
      } else {
         this.imageField.setText(this.currentPose.getImage() != null ? this.currentPose.getImage() : "");
         this.imageAnchorField.setText(this.currentPose.getImageAnchor() != null ? this.currentPose.getImageAnchor() : "");
         this.velocityField.setText(this.currentPose.getVelocity() != null ? this.currentPose.getVelocity() : "");
         this.durationSpinner.setValue(this.currentPose.getDuration());
         this.soundField.setText(this.currentPose.getSound() != null ? this.currentPose.getSound() : "");
         int volumePercent = (int)(this.currentPose.getVolume() * (double)100.0F);
         this.volumeSlider.setValue(volumePercent);
         this.volumeLabel.setText(volumePercent + "%");
         this.setEnabled(true);
      }

   }

   private void updatePoseFromUI() {
      if (this.currentPose != null) {
         this.currentPose.setImage(this.imageField.getText().trim());
         String anchorText = this.imageAnchorField.getText().trim();
         if (anchorText.isEmpty()) {
            this.currentPose.setImageAnchor("0,0");
         } else {
            if (!this.isValidCoordinate(anchorText)) {
               this.imageAnchorField.setText(this.currentPose.getImageAnchor() != null ? this.currentPose.getImageAnchor() : "0,0");
               this.showValidationMessage("Image Anchor should be in format 'x,y' (e.g., '64,128')");
               return;
            }

            this.currentPose.setImageAnchor(anchorText);
         }

         String velocityText = this.velocityField.getText().trim();
         if (velocityText.isEmpty()) {
            this.currentPose.setVelocity("0,0");
         } else {
            if (!this.isValidCoordinate(velocityText)) {
               this.velocityField.setText(this.currentPose.getVelocity() != null ? this.currentPose.getVelocity() : "0,0");
               this.showValidationMessage("Velocity should be in format 'x,y' (e.g., '5,0')");
               return;
            }

            this.currentPose.setVelocity(velocityText);
         }

         this.currentPose.setDuration((Integer)this.durationSpinner.getValue());
         this.currentPose.setSound(this.soundField.getText().trim());
         this.currentPose.setVolume((double)this.volumeSlider.getValue() / (double)100.0F);

         for(Runnable listener : this.changeListeners) {
            listener.run();
         }

      }
   }

   private boolean isValidCoordinate(String coordinate) {
      if (coordinate != null && !coordinate.trim().isEmpty()) {
         String[] parts = coordinate.split(",");
         if (parts.length != 2) {
            return false;
         } else {
            try {
               Double.parseDouble(parts[0].trim());
               Double.parseDouble(parts[1].trim());
               return true;
            } catch (NumberFormatException var4) {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private void showValidationMessage(String message) {
      JOptionPane.showMessageDialog(this, message, "Input Validation", 2);
   }

   private void browseForImage() {
      String initialDir = this.currentProjectDirectory != null ? this.currentProjectDirectory.getAbsolutePath() : "./img";
      JFileChooser chooser = new JFileChooser(initialDir);
      chooser.setFileFilter(new FileNameExtensionFilter(this.languageBundle.getString("editor.filter.imageFiles"), new String[]{"png", "jpg", "jpeg", "gif", "bmp"}));
      if (chooser.showOpenDialog(this) == 0) {
         String imagePath = "/" + chooser.getSelectedFile().getName();
         this.imageField.setText(imagePath);
         this.updatePoseFromUI();
      }

   }

   private void browseForSound() {
      String initialDir = this.currentProjectDirectory != null ? this.currentProjectDirectory.getAbsolutePath() + "/sound" : "./sound";
      JFileChooser chooser = new JFileChooser(initialDir);
      chooser.setFileFilter(new FileNameExtensionFilter(this.languageBundle.getString("editor.filter.audioFiles"), new String[]{"wav", "mp3", "ogg"}));
      if (chooser.showOpenDialog(this) == 0) {
         String soundPath = "/" + chooser.getSelectedFile().getName();
         this.soundField.setText(soundPath);
         this.updatePoseFromUI();
      }

   }

   public void addChangeListener(Runnable listener) {
      this.changeListeners.add(listener);
   }

   public void removeChangeListener(Runnable listener) {
      this.changeListeners.remove(listener);
   }

   public void setProjectDirectory(File projectDirectory) {
      this.currentProjectDirectory = projectDirectory;
   }

   public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      this.imageField.setEnabled(enabled);
      this.browseImageButton.setEnabled(enabled);
      this.imageAnchorField.setEnabled(enabled);
      this.velocityField.setEnabled(enabled);
      this.durationSpinner.setEnabled(enabled);
      this.soundField.setEnabled(enabled);
      this.browseSoundButton.setEnabled(enabled);
      this.volumeSlider.setEnabled(enabled);
   }
}
