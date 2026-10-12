package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.AnimationPose;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ActionDetailsPanel extends JPanel {
   private AnimationAction currentAction;
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();
   private File currentProjectDirectory;
   private List<Runnable> changeListeners = new ArrayList<>();
   private JTextField nameField;
   private JComboBox typeComboBox;
   private JTextField classField;
   private JComboBox borderTypeComboBox;
   private JList posesList;
   private DefaultListModel posesListModel;
   private JButton addPoseButton;
   private JButton removePoseButton;
   private JButton moveUpButton;
   private JButton moveDownButton;
   private PoseDetailsPanel poseDetailsPanel;

   public ActionDetailsPanel() {
      this.initializeComponents();
      this.setupLayout();
      this.setupEventHandlers();
      this.setAction((AnimationAction)null);
   }

   private void initializeComponents() {
      this.nameField = new JTextField();
      this.nameField.setFont(new Font("SansSerif", 0, 15));
      this.typeComboBox = new JComboBox(new String[]{"Stay", "Move", "Animate", "Sequence", "Select", "Embedded"});
      this.typeComboBox.setFont(new Font("SansSerif", 0, 15));
      this.classField = new JTextField();
      this.classField.setFont(new Font("SansSerif", 0, 15));
      this.borderTypeComboBox = new JComboBox(new String[]{"", "Floor", "Wall", "Ceiling"});
      this.borderTypeComboBox.setFont(new Font("SansSerif", 0, 15));
      this.posesListModel = new DefaultListModel();
      this.posesList = new JList(this.posesListModel);
      this.posesList.setSelectionMode(0);
      this.posesList.setFont(new Font("SansSerif", 0, 15));
      this.posesList.setCellRenderer(new SimplePoseListRenderer());
      this.poseDetailsPanel = new PoseDetailsPanel();
      this.addPoseButton = new JButton(this.languageBundle.getString("editor.button.addPose"));
      this.addPoseButton.setFont(new Font("SansSerif", 0, 15));
      this.addPoseButton.setToolTipText(this.languageBundle.getString("editor.button.addPose.tooltip"));
      this.removePoseButton = new JButton(this.languageBundle.getString("editor.button.removePose"));
      this.removePoseButton.setFont(new Font("SansSerif", 0, 15));
      this.removePoseButton.setToolTipText(this.languageBundle.getString("editor.button.removePose.tooltip"));
      this.moveUpButton = new JButton(this.languageBundle.getString("editor.button.moveUp"));
      this.moveUpButton.setFont(new Font("SansSerif", 1, 16));
      this.moveUpButton.setToolTipText(this.languageBundle.getString("editor.button.moveUp.tooltip"));
      this.moveDownButton = new JButton(this.languageBundle.getString("editor.button.moveDown"));
      this.moveDownButton.setFont(new Font("SansSerif", 1, 16));
      this.moveDownButton.setToolTipText(this.languageBundle.getString("editor.button.moveDown.tooltip"));
   }

   private void setupLayout() {
      this.setLayout(new BorderLayout());
      this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      JPanel topPanel = this.createBasicInfoPanel();
      JPanel centerPanel = this.createPosesPanel();
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.languageBundle.getString("editor.panel.poseDetails"), 0, 0, new Font("SansSerif", 1, 16)));
      bottomPanel.add(this.poseDetailsPanel, "Center");
      this.add(topPanel, "North");
      this.add(centerPanel, "Center");
      this.add(bottomPanel, "South");
   }

   private JPanel createBasicInfoPanel() {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.languageBundle.getString("editor.panel.basicInfo"), 0, 0, new Font("SansSerif", 1, 16)));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(4, 4, 4, 4);
      gbc.anchor = 17;
      Font labelFont = new Font("SansSerif", 0, 15);
      gbc.gridx = 0;
      gbc.gridy = 0;
      JLabel nameLabel = new JLabel(this.languageBundle.getString("editor.label.name"));
      nameLabel.setFont(labelFont);
      panel.add(nameLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      panel.add(this.nameField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 1;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      JLabel typeLabel = new JLabel(this.languageBundle.getString("editor.label.type"));
      typeLabel.setFont(labelFont);
      panel.add(typeLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      panel.add(this.typeComboBox, gbc);
      gbc.gridx = 0;
      gbc.gridy = 2;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      JLabel classLabel = new JLabel(this.languageBundle.getString("editor.label.class"));
      classLabel.setFont(labelFont);
      panel.add(classLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      panel.add(this.classField, gbc);
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.fill = 0;
      gbc.weightx = (double)0.0F;
      JLabel borderLabel = new JLabel(this.languageBundle.getString("editor.label.border"));
      borderLabel.setFont(labelFont);
      panel.add(borderLabel, gbc);
      gbc.gridx = 1;
      gbc.fill = 2;
      gbc.weightx = (double)1.0F;
      panel.add(this.borderTypeComboBox, gbc);
      return panel;
   }

   private JPanel createPosesPanel() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.languageBundle.getString("editor.panel.animationPoses"), 0, 0, new Font("SansSerif", 1, 16)));
      JScrollPane scrollPane = new JScrollPane(this.posesList);
      scrollPane.setPreferredSize(new Dimension(0, 100));
      JPanel buttonsPanel = new JPanel(new FlowLayout());
      buttonsPanel.add(this.addPoseButton);
      buttonsPanel.add(this.removePoseButton);
      buttonsPanel.add(this.moveUpButton);
      buttonsPanel.add(this.moveDownButton);
      panel.add(scrollPane, "Center");
      panel.add(buttonsPanel, "South");
      return panel;
   }

   private void setupEventHandlers() {
      this.nameField.addActionListener((e) -> this.updateActionFromUI());
      this.nameField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            ActionDetailsPanel.this.updateActionFromUI();
         }
      });
      this.typeComboBox.addActionListener((e) -> this.updateActionFromUI());
      this.classField.addActionListener((e) -> this.updateActionFromUI());
      this.classField.addFocusListener(new FocusAdapter() {
         public void focusLost(FocusEvent e) {
            ActionDetailsPanel.this.updateActionFromUI();
         }
      });
      this.borderTypeComboBox.addActionListener((e) -> this.updateActionFromUI());
      this.posesList.addListSelectionListener((e) -> {
         if (!e.getValueIsAdjusting()) {
            AnimationPose selectedPose = (AnimationPose)this.posesList.getSelectedValue();
            this.poseDetailsPanel.setPose(selectedPose);
            this.updateButtonStates();
         }

      });
      this.addPoseButton.addActionListener((e) -> this.addNewPose());
      this.removePoseButton.addActionListener((e) -> this.removeSelectedPose());
      this.moveUpButton.addActionListener((e) -> this.movePoseUp());
      this.moveDownButton.addActionListener((e) -> this.movePoseDown());
      this.poseDetailsPanel.addChangeListener(() -> {
         this.posesList.repaint();
         this.notifyChangeListeners();
      });
   }

   public void setAction(AnimationAction action) {
      this.currentAction = action;
      this.updateUIFromAction();
   }

   public void setProjectDirectory(File projectDirectory) {
      this.currentProjectDirectory = projectDirectory;
      if (this.poseDetailsPanel != null) {
         this.poseDetailsPanel.setProjectDirectory(projectDirectory);
      }

   }

   private void updateUIFromAction() {
      if (this.currentAction == null) {
         this.nameField.setText("");
         this.typeComboBox.setSelectedIndex(0);
         this.classField.setText("");
         this.borderTypeComboBox.setSelectedIndex(0);
         this.posesListModel.clear();
         this.poseDetailsPanel.setPose((AnimationPose)null);
         this.setControlsEnabled(false);
      } else {
         this.nameField.setText(this.currentAction.getName() != null ? this.currentAction.getName() : "");
         this.typeComboBox.setSelectedItem(this.currentAction.getType() != null ? this.currentAction.getType() : "Stay");
         this.classField.setText(this.currentAction.getClassName() != null ? this.currentAction.getClassName() : "");
         this.borderTypeComboBox.setSelectedItem(this.currentAction.getBorderType() != null ? this.currentAction.getBorderType() : "");
         this.posesListModel.clear();

         for(AnimationPose pose : this.currentAction.getPoses()) {
            this.posesListModel.addElement(pose);
         }

         this.setControlsEnabled(true);
      }

      this.updateButtonStates();
   }

   private void updateActionFromUI() {
      if (this.currentAction != null) {
         this.currentAction.setName(this.nameField.getText());
         this.currentAction.setType((String)this.typeComboBox.getSelectedItem());
         this.currentAction.setClassName(this.classField.getText());
         this.currentAction.setBorderType((String)this.borderTypeComboBox.getSelectedItem());
         this.notifyChangeListeners();
      }
   }

   private void addNewPose() {
      if (this.currentAction != null) {
         AnimationPose newPose = new AnimationPose();
         newPose.setImage("/Stand.png");
         newPose.setImageAnchor("64,128");
         newPose.setVelocity("0,0");
         newPose.setDuration(25);
         this.currentAction.addPose(newPose);
         this.posesListModel.addElement(newPose);
         this.posesList.setSelectedValue(newPose, true);
         this.notifyChangeListeners();
      }
   }

   private void removeSelectedPose() {
      AnimationPose selectedPose = (AnimationPose)this.posesList.getSelectedValue();
      if (selectedPose != null && this.currentAction != null) {
         this.currentAction.removePose(selectedPose);
         this.posesListModel.removeElement(selectedPose);
         this.poseDetailsPanel.setPose((AnimationPose)null);
         this.notifyChangeListeners();
      }

   }

   private void movePoseUp() {
      int selectedIndex = this.posesList.getSelectedIndex();
      if (selectedIndex > 0) {
         AnimationPose pose = (AnimationPose)this.posesListModel.remove(selectedIndex);
         this.posesListModel.add(selectedIndex - 1, pose);
         this.posesList.setSelectedIndex(selectedIndex - 1);
         this.currentAction.getPoses().remove(selectedIndex);
         this.currentAction.getPoses().add(selectedIndex - 1, pose);
         this.notifyChangeListeners();
      }

   }

   private void movePoseDown() {
      int selectedIndex = this.posesList.getSelectedIndex();
      if (selectedIndex >= 0 && selectedIndex < this.posesListModel.size() - 1) {
         AnimationPose pose = (AnimationPose)this.posesListModel.remove(selectedIndex);
         this.posesListModel.add(selectedIndex + 1, pose);
         this.posesList.setSelectedIndex(selectedIndex + 1);
         this.currentAction.getPoses().remove(selectedIndex);
         this.currentAction.getPoses().add(selectedIndex + 1, pose);
         this.notifyChangeListeners();
      }

   }

   private void updateButtonStates() {
      boolean hasAction = this.currentAction != null;
      boolean hasSelectedPose = this.posesList.getSelectedValue() != null;
      int selectedIndex = this.posesList.getSelectedIndex();
      this.addPoseButton.setEnabled(hasAction);
      this.removePoseButton.setEnabled(hasSelectedPose);
      this.moveUpButton.setEnabled(hasSelectedPose && selectedIndex > 0);
      this.moveDownButton.setEnabled(hasSelectedPose && selectedIndex < this.posesListModel.size() - 1);
   }

   private void setControlsEnabled(boolean enabled) {
      this.nameField.setEnabled(enabled);
      this.typeComboBox.setEnabled(enabled);
      this.classField.setEnabled(enabled);
      this.borderTypeComboBox.setEnabled(enabled);
      this.posesList.setEnabled(enabled);
      this.poseDetailsPanel.setEnabled(enabled);
      this.updateButtonStates();
   }

   public void setImageForSelectedPose(String imagePath) {
      AnimationPose selectedPose = (AnimationPose)this.posesList.getSelectedValue();
      if (selectedPose != null) {
         selectedPose.setImage(imagePath);
         this.poseDetailsPanel.setPose(selectedPose);
         this.posesList.repaint();
      }

   }

   public void addChangeListener(Runnable listener) {
      this.changeListeners.add(listener);
   }

   public void removeChangeListener(Runnable listener) {
      this.changeListeners.remove(listener);
   }

   private void notifyChangeListeners() {
      for(Runnable listener : this.changeListeners) {
         listener.run();
      }

   }

   private class SimplePoseListRenderer extends DefaultListCellRenderer {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         if (value instanceof AnimationPose pose) {
            String text = String.format(ActionDetailsPanel.this.languageBundle.getString("editor.pose.listFormat"), index + 1, pose.getImage() != null ? pose.getImage() : ActionDetailsPanel.this.languageBundle.getString("editor.pose.noImage"), pose.getDuration());
            this.setText(text);
         }

         return this;
      }
   }
}
