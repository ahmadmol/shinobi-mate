package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.Project;
import com.group_finity.mascot.animationeditor.util.ProjectScanner;
import com.group_finity.mascot.animationeditor.util.XMLConfigUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class MainEditorWindow extends JFrame {
   private static final Logger log = Logger.getLogger(MainEditorWindow.class.getName());
   private ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();
   private JSplitPane mainSplitPane;
   private JSplitPane centerSplitPane;
   private JPanel leftPanel;
   private JTree projectTree;
   private DefaultMutableTreeNode rootNode;
   private DefaultTreeModel treeModel;
   private JPanel centerTopPanel;
   private AnimationPreviewPanel previewPanel;
   private JPanel centerBottomPanel;
   private JPanel imageGridPanel;
   private JScrollPane imageScrollPane;
   private JPanel rightPanel;
   private ActionDetailsPanel actionDetailsPanel;
   private Project currentProject;
   private AnimationAction selectedAction;
   private int currentProjectIndex = 0;
   private List availableProjects;
   private boolean projectModified = false;
   private JLabel statusLabel;

   public MainEditorWindow() {
      this.initializeUI();
      this.setupLayout();
      this.setupEventHandlers();
      this.loadProjects();
   }

   private void initializeUI() {
      this.setTitle(this.languageBundle.getString("editor.title"));
      this.setDefaultCloseOperation(2);
      this.setSize(1400, 800);
      this.setLocationRelativeTo((Component)null);
      this.createLeftPanel();
      this.createCenterTopPanel();
      this.createCenterBottomPanel();
      this.createRightPanel();
   }

   private void createLeftPanel() {
      this.leftPanel = new JPanel(new BorderLayout());
      this.leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.leftPanel.setPreferredSize(new Dimension(250, 0));
      this.rootNode = new DefaultMutableTreeNode(this.languageBundle.getString("editor.projects.root"));
      this.treeModel = new DefaultTreeModel(this.rootNode);
      this.projectTree = new JTree(this.treeModel);
      this.projectTree.setRootVisible(true);
      this.projectTree.setCellRenderer(new ProjectTreeCellRenderer());
      JScrollPane treeScrollPane = new JScrollPane(this.projectTree);
      treeScrollPane.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("editor.projects.title")));
      this.leftPanel.add(treeScrollPane, "Center");
   }

   private void createCenterTopPanel() {
      this.centerTopPanel = new JPanel(new BorderLayout());
      this.centerTopPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.previewPanel = new AnimationPreviewPanel();
      JScrollPane previewScrollPane = new JScrollPane(this.previewPanel);
      previewScrollPane.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("editor.preview.title")));
      this.centerTopPanel.add(previewScrollPane, "Center");
   }

   private void createCenterBottomPanel() {
      this.centerBottomPanel = new JPanel(new BorderLayout());
      this.centerBottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.imageGridPanel = new JPanel();
      this.imageGridPanel.setLayout(new GridLayout(0, 3, 15, 15));
      this.imageGridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
      this.imageGridPanel.setBackground(new Color(248, 249, 250));
      this.imageScrollPane = new JScrollPane(this.imageGridPanel);
      this.imageScrollPane.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("editor.images.title")));
      this.imageScrollPane.getVerticalScrollBar().setUnitIncrement(16);
      this.centerBottomPanel.add(this.imageScrollPane, "Center");
   }

   private void createRightPanel() {
      this.rightPanel = new JPanel(new BorderLayout());
      this.rightPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.actionDetailsPanel = new ActionDetailsPanel();
      this.actionDetailsPanel.addChangeListener(() -> this.markProjectAsModified());
      JScrollPane rightScrollPane = new JScrollPane(this.actionDetailsPanel);
      rightScrollPane.setBorder(BorderFactory.createTitledBorder(this.languageBundle.getString("editor.details.title")));
      this.rightPanel.add(rightScrollPane, "Center");
   }

   private void setupLayout() {
      this.mainSplitPane = new JSplitPane(1);
      this.mainSplitPane.setResizeWeight((double)0.25F);
      this.centerSplitPane = new JSplitPane(0);
      this.centerSplitPane.setTopComponent(this.centerTopPanel);
      this.centerSplitPane.setBottomComponent(this.centerBottomPanel);
      this.centerSplitPane.setDividerLocation(300);
      this.centerSplitPane.setResizeWeight(0.6);
      JSplitPane rightSplitPane = new JSplitPane(1);
      rightSplitPane.setLeftComponent(this.centerSplitPane);
      rightSplitPane.setRightComponent(this.rightPanel);
      rightSplitPane.setDividerLocation(600);
      rightSplitPane.setResizeWeight(0.67);
      this.mainSplitPane.setLeftComponent(this.leftPanel);
      this.mainSplitPane.setRightComponent(rightSplitPane);
      this.mainSplitPane.setDividerLocation(350);
      this.add(this.mainSplitPane, "Center");
      this.createStatusBar();
      this.createMenuBar();
   }

   private void createMenuBar() {
      JMenuBar menuBar = new JMenuBar();
      JMenu fileMenu = new JMenu(this.languageBundle.getString("editor.menu.file"));
      JMenuItem openProjectItem = new JMenuItem(this.languageBundle.getString("editor.menu.file.open"));
      openProjectItem.addActionListener((e) -> this.openProject());
      fileMenu.add(openProjectItem);
      JMenuItem saveProjectItem = new JMenuItem(this.languageBundle.getString("editor.menu.file.save"));
      saveProjectItem.addActionListener((e) -> this.saveProject());
      fileMenu.add(saveProjectItem);
      fileMenu.addSeparator();
      JMenuItem nextProjectItem = new JMenuItem(this.languageBundle.getString("editor.menu.file.nextProject"));
      nextProjectItem.addActionListener((e) -> this.switchToNextProject());
      fileMenu.add(nextProjectItem);
      JMenuItem previousProjectItem = new JMenuItem(this.languageBundle.getString("editor.menu.file.previousProject"));
      previousProjectItem.addActionListener((e) -> this.switchToPreviousProject());
      fileMenu.add(previousProjectItem);
      fileMenu.addSeparator();
      JMenuItem exitItem = new JMenuItem(this.languageBundle.getString("editor.menu.file.exit"));
      exitItem.addActionListener((e) -> this.dispose());
      fileMenu.add(exitItem);
      menuBar.add(fileMenu);
      JMenu editMenu = new JMenu(this.languageBundle.getString("editor.menu.edit"));
      JMenuItem newActionItem = new JMenuItem(this.languageBundle.getString("editor.menu.edit.newAction"));
      newActionItem.addActionListener((e) -> this.createNewAction());
      editMenu.add(newActionItem);
      JMenuItem deleteActionItem = new JMenuItem(this.languageBundle.getString("editor.menu.edit.deleteAction"));
      deleteActionItem.addActionListener((e) -> this.deleteSelectedAction());
      editMenu.add(deleteActionItem);
      menuBar.add(editMenu);
      this.setJMenuBar(menuBar);
   }

   private void setupEventHandlers() {
      this.projectTree.addTreeSelectionListener((e) -> {
         DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)this.projectTree.getLastSelectedPathComponent();
         if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof Project) {
               this.loadProject((Project)userObject);
            } else if (userObject instanceof AnimationAction) {
               this.selectAction((AnimationAction)userObject);
            }

         }
      });
   }

   private void loadProjects() {
      try {
         File imgDirectory = new File("./img");
         this.availableProjects = ProjectScanner.scanProjects(imgDirectory);
         if (this.availableProjects.isEmpty()) {
            this.rootNode.removeAllChildren();
            this.treeModel.reload();
            JOptionPane.showMessageDialog(this, "No valid projects found in ./img directory", "No Projects", 1);
            return;
         }

         this.loadSingleProject(this.currentProjectIndex);
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to load projects", e);
         JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.loadProjects") + ": " + e.getMessage(), this.languageBundle.getString("editor.error.title"), 0);
      }

   }

   private void loadSingleProject(int projectIndex) {
      if (this.availableProjects != null && projectIndex < this.availableProjects.size()) {
         File projectDir = (File)this.availableProjects.get(projectIndex);
         Project project = new Project(projectDir.getName(), projectDir);

         try {
            if (ProjectScanner.isValidProject(projectDir)) {
               List<AnimationAction> actions = XMLConfigUtil.loadActionsFromXML(ProjectScanner.getConfigFile(projectDir));
               project.setActions(actions);
            }

            this.rootNode.removeAllChildren();
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);

            for(AnimationAction action : project.getActions()) {
               DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);
               projectNode.add(actionNode);
            }

            this.rootNode.add(projectNode);
            this.treeModel.reload();
            this.expandTree();
            this.currentProject = project;
            this.projectModified = false;
            this.updateProjectDirectoryForAllPanels();
            this.updateWindowTitle();
            this.updateStatusLabel("Project loaded: " + project.getName());
         } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load project: " + projectDir.getName(), e);
            this.updateStatusLabel("Failed to load project: " + projectDir.getName());
         }

      }
   }

   private void switchToNextProject() {
      if (this.availableProjects != null && !this.availableProjects.isEmpty()) {
         this.clearPreview();
         this.currentProjectIndex = (this.currentProjectIndex + 1) % this.availableProjects.size();
         this.loadSingleProject(this.currentProjectIndex);
      }

   }

   private void switchToPreviousProject() {
      if (this.availableProjects != null && !this.availableProjects.isEmpty()) {
         this.clearPreview();
         this.currentProjectIndex = (this.currentProjectIndex - 1 + this.availableProjects.size()) % this.availableProjects.size();
         this.loadSingleProject(this.currentProjectIndex);
      }

   }

   private void updateProjectDirectoryForAllPanels() {
      if (this.currentProject != null) {
         if (this.previewPanel != null) {
            this.previewPanel.setProjectDirectory(this.currentProject.getRootDirectory());
         }

         if (this.actionDetailsPanel != null) {
            this.actionDetailsPanel.setProjectDirectory(this.currentProject.getRootDirectory());
         }
      }

   }

   private void clearPreview() {
      this.selectedAction = null;
      if (this.previewPanel != null) {
         this.previewPanel.setAction((AnimationAction)null);
      }

      if (this.actionDetailsPanel != null) {
         this.actionDetailsPanel.setAction((AnimationAction)null);
      }

   }

   private void expandTree() {
      for(int i = 0; i < this.projectTree.getRowCount(); ++i) {
         this.projectTree.expandRow(i);
      }

   }

   private void loadProject(Project project) {
      this.currentProject = project;
      if (this.availableProjects != null) {
         for(int i = 0; i < this.availableProjects.size(); ++i) {
            if (((File)this.availableProjects.get(i)).getName().equals(project.getName())) {
               this.currentProjectIndex = i;
               break;
            }
         }
      }

      this.updateProjectDirectoryForAllPanels();
      this.imageGridPanel.removeAll();

      for(File imageFile : ProjectScanner.scanImageFiles(project.getRootDirectory())) {
         this.addImageToGrid(imageFile);
      }

      this.imageGridPanel.revalidate();
      this.imageGridPanel.repaint();
      this.clearPreview();
      this.updateWindowTitle();
   }

   private void selectAction(AnimationAction action) {
      this.selectedAction = action;
      this.updateProjectDirectoryForAllPanels();
      this.actionDetailsPanel.setAction(action);
      this.previewPanel.setAction(action);
   }

   private void openProject() {
      JFileChooser chooser = new JFileChooser("./img");
      chooser.setFileSelectionMode(1);
      chooser.setDialogTitle(this.languageBundle.getString("editor.dialog.selectProject"));
      if (chooser.showOpenDialog(this) == 0) {
         File selectedDir = chooser.getSelectedFile();
         if (ProjectScanner.isValidProject(selectedDir)) {
            this.loadProjects();
         } else {
            JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.invalidProject"), this.languageBundle.getString("editor.error.invalidProjectTitle"), 2);
         }
      }

   }

   private void saveProject() {
      if (this.currentProject == null) {
         JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.noProjectSelected"), this.languageBundle.getString("editor.error.noProjectTitle"), 2);
      } else {
         boolean success = XMLConfigUtil.saveActionsToXML(this.currentProject.getActions(), this.currentProject.getConfigFile());
         if (success) {
            JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.success.projectSaved"), this.languageBundle.getString("editor.success.saveTitle"), 1);
            this.markProjectAsSaved();
         } else {
            JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.saveFailed"), this.languageBundle.getString("editor.error.saveFailedTitle"), 0);
         }

      }
   }

   private void createNewAction() {
      if (this.currentProject == null) {
         JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.noProjectSelected"), this.languageBundle.getString("editor.error.noProjectTitle"), 2);
      } else {
         String actionName = JOptionPane.showInputDialog(this, this.languageBundle.getString("editor.dialog.enterActionName"), this.languageBundle.getString("editor.dialog.newActionTitle"), 3);
         if (actionName != null && !actionName.trim().isEmpty()) {
            AnimationAction newAction = new AnimationAction(actionName.trim(), "Stay");
            this.currentProject.getActions().add(newAction);
            this.loadProjects();
            this.markProjectAsModified();
         }

      }
   }

   private void deleteSelectedAction() {
      if (this.selectedAction == null) {
         JOptionPane.showMessageDialog(this, this.languageBundle.getString("editor.error.noActionSelected"), this.languageBundle.getString("editor.error.noSelectionTitle"), 2);
      } else {
         int result = JOptionPane.showConfirmDialog(this, String.format(this.languageBundle.getString("editor.dialog.confirmDeleteAction"), this.selectedAction.getName()), this.languageBundle.getString("editor.dialog.confirmDeleteTitle"), 0);
         if (result == 0) {
            this.currentProject.getActions().remove(this.selectedAction);
            this.selectAction((AnimationAction)null);
            this.loadProjects();
            this.markProjectAsModified();
         }

      }
   }

   private void createStatusBar() {
      this.statusLabel = new JLabel("Ready");
      this.statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      this.statusLabel.setFont(new Font("SansSerif", 0, 12));
      JPanel statusPanel = new JPanel(new BorderLayout());
      statusPanel.setBorder(BorderFactory.createEtchedBorder());
      statusPanel.add(this.statusLabel, "West");
      this.add(statusPanel, "South");
   }

   private void markProjectAsModified() {
      if (!this.projectModified) {
         this.projectModified = true;
         this.updateWindowTitle();
         this.updateStatusLabel("Project modified - remember to save changes");
      }

   }

   private void markProjectAsSaved() {
      if (this.projectModified) {
         this.projectModified = false;
         this.updateWindowTitle();
         this.updateStatusLabel("Project saved successfully");
      }

   }

   private void updateWindowTitle() {
      String title = this.languageBundle.getString("editor.title");
      if (this.currentProject != null) {
         title = title + " - " + this.currentProject.getName();
         if (this.projectModified) {
            title = title + " *";
         }
      }

      this.setTitle(title);
   }

   private void updateStatusLabel(String message) {
      if (this.statusLabel != null) {
         this.statusLabel.setText(message);
      }

   }

   private void addImageToGrid(File imageFile) {
      JPanel imagePanel = ImageGridPanelRenderer.createImagePanel(imageFile, () -> {
         String var10001 = imageFile.getName();
         this.updateStatusLabel("选择了图片: " + var10001 + " / Selected image: " + imageFile.getName());

         for(Component comp : this.imageGridPanel.getComponents()) {
            if (comp instanceof JPanel) {
               comp.setBackground(Color.WHITE);
               ((JPanel)comp).setBorder(BorderFactory.createRaisedBevelBorder());
            }
         }

      }, () -> {
         if (this.selectedAction != null) {
            this.actionDetailsPanel.setImageForSelectedPose(imageFile.getAbsolutePath());
            String var10001 = imageFile.getName();
            this.updateStatusLabel("设置图片到选中姿势: " + var10001 + " / Set image to selected pose: " + imageFile.getName());
         }

      });
      this.imageGridPanel.add(imagePanel);
   }
}
