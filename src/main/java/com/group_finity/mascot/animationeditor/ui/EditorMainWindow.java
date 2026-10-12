package com.group_finity.mascot.animationeditor.ui;

import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.Project;
import com.group_finity.mascot.animationeditor.util.ProjectScanner;
import com.group_finity.mascot.animationeditor.util.XMLConfigUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class EditorMainWindow extends JFrame {
   private static final Logger log = Logger.getLogger(EditorMainWindow.class.getName());
   private JSplitPane mainSplitPane;
   private JSplitPane rightSplitPane;
   private JSplitPane centerSplitPane;
   private JPanel leftPanel;
   private JTree projectTree;
   private DefaultMutableTreeNode rootNode;
   private DefaultTreeModel treeModel;
   private JPanel centerTopPanel;
   private AnimationPreviewPanel previewPanel;
   private JPanel centerBottomPanel;
   private JLabel currentDirectoryLabel;
   private JButton browseButton;
   private JPanel imageGridPanel;
   private JScrollPane imageScrollPane;
   private JPanel rightPanel;
   private ActionDetailsPanel actionDetailsPanel;
   private Project currentProject;
   private AnimationAction selectedAction;
   private File currentImageDirectory;

   public EditorMainWindow() {
      this.initializeUI();
      this.setupLayout();
      this.setupEventHandlers();
   }

   private void initializeUI() {
      this.setTitle("Shimeji 动画编辑器 / Animation Editor");
      this.setDefaultCloseOperation(2);
      this.setSize(1400, 900);
      this.setLocationRelativeTo((Component)null);

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to set system look and feel", e);
      }

      this.createLeftPanel();
      this.createCenterTopPanel();
      this.createCenterBottomPanel();
      this.createRightPanel();
   }

   private void createLeftPanel() {
      this.leftPanel = new JPanel(new BorderLayout());
      this.leftPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
      this.leftPanel.setPreferredSize(new Dimension(280, 0));
      this.rootNode = new DefaultMutableTreeNode("动画项目 / Animation Projects");
      this.treeModel = new DefaultTreeModel(this.rootNode);
      this.projectTree = new JTree(this.treeModel);
      this.projectTree.setRootVisible(true);
      this.projectTree.setCellRenderer(new ProjectTreeCellRenderer());
      this.projectTree.setFont(new Font("SansSerif", 0, 14));
      this.projectTree.setRowHeight(26);
      JScrollPane treeScrollPane = new JScrollPane(this.projectTree);
      treeScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "项目和动作 / Projects & Actions", 0, 0, new Font("SansSerif", 1, 14)));
      this.leftPanel.add(treeScrollPane, "Center");
      JButton refreshButton = new JButton("刷新项目 / Refresh");
      refreshButton.setFont(new Font("SansSerif", 0, 13));
      refreshButton.addActionListener((e) -> this.loadProjects());
      this.leftPanel.add(refreshButton, "South");
   }

   private void createCenterTopPanel() {
      this.centerTopPanel = new JPanel(new BorderLayout());
      this.centerTopPanel.setBorder(new EmptyBorder(8, 4, 4, 4));
      this.centerTopPanel.setPreferredSize(new Dimension(0, 350));
      this.previewPanel = new AnimationPreviewPanel();
      this.centerTopPanel.add(this.previewPanel, "Center");
   }

   private void createCenterBottomPanel() {
      this.centerBottomPanel = new JPanel(new BorderLayout());
      this.centerBottomPanel.setBorder(new EmptyBorder(4, 4, 8, 4));
      JPanel toolbarPanel = new JPanel(new BorderLayout());
      toolbarPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.currentDirectoryLabel = new JLabel("选择项目以浏览图片 / Select project to browse images");
      this.currentDirectoryLabel.setFont(new Font("SansSerif", 0, 13));
      this.browseButton = new JButton("浏览文件夹 / Browse Folder");
      this.browseButton.setFont(new Font("SansSerif", 0, 13));
      this.browseButton.addActionListener((e) -> this.browseForImageDirectory());
      this.browseButton.setEnabled(false);
      toolbarPanel.add(this.currentDirectoryLabel, "Center");
      toolbarPanel.add(this.browseButton, "East");
      this.imageGridPanel = new JPanel();
      this.imageGridPanel.setLayout(new GridLayout(0, 3, 15, 15));
      this.imageGridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
      this.imageGridPanel.setBackground(new Color(248, 249, 250));
      this.imageScrollPane = new JScrollPane(this.imageGridPanel);
      this.imageScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "图片资源 / Image Resources", 0, 0, new Font("SansSerif", 1, 14)));
      this.imageScrollPane.getVerticalScrollBar().setUnitIncrement(16);
      this.centerBottomPanel.add(toolbarPanel, "North");
      this.centerBottomPanel.add(this.imageScrollPane, "Center");
   }

   private void createRightPanel() {
      this.rightPanel = new JPanel(new BorderLayout());
      this.rightPanel.setBorder(new EmptyBorder(8, 4, 8, 8));
      this.rightPanel.setPreferredSize(new Dimension(350, 0));
      this.actionDetailsPanel = new ActionDetailsPanel();
      JScrollPane rightScrollPane = new JScrollPane(this.actionDetailsPanel);
      rightScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "动作详情 / Action Details", 0, 0, new Font("SansSerif", 1, 14)));
      this.rightPanel.add(rightScrollPane, "Center");
   }

   private void setupLayout() {
      this.centerSplitPane = new JSplitPane(0);
      this.centerSplitPane.setTopComponent(this.centerTopPanel);
      this.centerSplitPane.setBottomComponent(this.centerBottomPanel);
      this.centerSplitPane.setDividerLocation(350);
      this.centerSplitPane.setResizeWeight(0.4);
      this.centerSplitPane.setOneTouchExpandable(true);
      this.mainSplitPane = new JSplitPane(1);
      this.rightSplitPane = new JSplitPane(1);
      this.mainSplitPane.setLeftComponent(this.leftPanel);
      this.mainSplitPane.setRightComponent(this.rightSplitPane);
      this.mainSplitPane.setDividerLocation(280);
      this.mainSplitPane.setResizeWeight(0.2);
      this.mainSplitPane.setOneTouchExpandable(true);
      this.rightSplitPane.setLeftComponent(this.centerSplitPane);
      this.rightSplitPane.setRightComponent(this.rightPanel);
      this.rightSplitPane.setDividerLocation(700);
      this.rightSplitPane.setResizeWeight(0.67);
      this.rightSplitPane.setOneTouchExpandable(true);
      this.add(this.mainSplitPane, "Center");
      this.createMenuBar();
      this.createStatusBar();
   }

   private void createMenuBar() {
      JMenuBar menuBar = new JMenuBar();
      JMenu fileMenu = new JMenu("文件 / File");
      JMenuItem openProjectItem = new JMenuItem("打开项目 / Open Project");
      openProjectItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
      openProjectItem.addActionListener((e) -> this.openProject());
      fileMenu.add(openProjectItem);
      JMenuItem saveProjectItem = new JMenuItem("保存项目 / Save Project");
      saveProjectItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
      saveProjectItem.addActionListener((e) -> this.saveProject());
      fileMenu.add(saveProjectItem);
      fileMenu.addSeparator();
      JMenuItem exitItem = new JMenuItem("退出 / Exit");
      exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
      exitItem.addActionListener((e) -> this.dispose());
      fileMenu.add(exitItem);
      menuBar.add(fileMenu);
      JMenu editMenu = new JMenu("编辑 / Edit");
      JMenuItem newActionItem = new JMenuItem("新建动作 / New Action");
      newActionItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
      newActionItem.addActionListener((e) -> this.createNewAction());
      editMenu.add(newActionItem);
      JMenuItem deleteActionItem = new JMenuItem("删除动作 / Delete Action");
      deleteActionItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
      deleteActionItem.addActionListener((e) -> this.deleteSelectedAction());
      editMenu.add(deleteActionItem);
      menuBar.add(editMenu);
      JMenu viewMenu = new JMenu("视图 / View");
      JMenuItem expandAllItem = new JMenuItem("展开所有 / Expand All");
      expandAllItem.addActionListener((e) -> this.expandAllTreeNodes());
      viewMenu.add(expandAllItem);
      JMenuItem collapseAllItem = new JMenuItem("折叠所有 / Collapse All");
      collapseAllItem.addActionListener((e) -> this.collapseAllTreeNodes());
      viewMenu.add(collapseAllItem);
      menuBar.add(viewMenu);
      this.setJMenuBar(menuBar);
   }

   private void createStatusBar() {
      JPanel statusBar = new JPanel(new FlowLayout(0));
      statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
      statusBar.add(new JLabel("就绪 / Ready"));
      this.add(statusBar, "South");
   }

   private void setupEventHandlers() {
      this.projectTree.addTreeSelectionListener((e) -> {
         DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)this.projectTree.getLastSelectedPathComponent();
         if (selectedNode == null) {
            this.selectAction((AnimationAction)null);
         } else {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof Project) {
               Project project = (Project)userObject;
               this.loadProject(project);
               log.info("Loaded project: " + project.getName());
               String var10001 = project.getName();
               this.updateStatusMessage("已加载项目: " + var10001 + " / Loaded project: " + project.getName());
            } else if (userObject instanceof AnimationAction) {
               AnimationAction action = (AnimationAction)userObject;
               this.selectAction(action);
               log.info("Selected action: " + action.getName());
               String var6 = action.getName();
               this.updateStatusMessage("已选择动作: " + var6 + " / Selected action: " + action.getName());
            }

         }
      });
   }

   private void loadProjects() {
      try {
         File imgDirectory = new File("./img");
         if (!imgDirectory.exists()) {
            imgDirectory = new File("img");
         }

         List<File> projectDirs = ProjectScanner.scanProjects(imgDirectory);
         this.rootNode.removeAllChildren();

         for(File projectDir : projectDirs) {
            Project project = new Project(projectDir.getName(), projectDir);
            if (ProjectScanner.isValidProject(projectDir)) {
               List<AnimationAction> actions = XMLConfigUtil.loadActionsFromXML(ProjectScanner.getConfigFile(projectDir));
               project.setActions(actions);
               DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
               this.rootNode.add(projectNode);
            }
         }

         this.treeModel.reload();
         this.expandAllTreeNodes();
         int var10001 = projectDirs.size();
         this.updateStatusMessage("已加载 " + var10001 + " 个项目 / Loaded " + projectDirs.size() + " projects");
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to load projects", e);
         JOptionPane.showMessageDialog(this, "加载项目失败: " + e.getMessage() + "\nFailed to load projects: " + e.getMessage(), "错误 / Error", 0);
      }

   }

   private void loadProject(Project project) {
      this.currentProject = project;
      this.currentImageDirectory = project.getRootDirectory();
      this.currentDirectoryLabel.setText("当前目录 / Current: " + this.currentImageDirectory.getAbsolutePath());
      this.browseButton.setEnabled(true);
      this.loadImagesFromDirectory(this.currentImageDirectory);
      this.selectAction((AnimationAction)null);
      this.previewPanel.setAction((AnimationAction)null);
   }

   private void loadImagesFromDirectory(File directory) {
      SwingUtilities.invokeLater(() -> {
         this.imageGridPanel.removeAll();
         if (directory != null && directory.exists() && directory.isDirectory()) {
            for(File imageFile : ProjectScanner.scanImageFiles(directory)) {
               this.addImageToGrid(imageFile);
            }
         }

         this.imageGridPanel.revalidate();
         this.imageGridPanel.repaint();
      });
   }

   private void addImageToGrid(File imageFile) {
      JPanel imagePanel = ImageGridPanelRenderer.createImagePanel(imageFile, () -> {
         String var10001 = imageFile.getName();
         this.updateStatusMessage("选择了图片: " + var10001 + " / Selected image: " + imageFile.getName());

         for(Component comp : this.imageGridPanel.getComponents()) {
            if (comp instanceof JPanel) {
               comp.setBackground(Color.WHITE);
               ((JPanel)comp).setBorder(BorderFactory.createRaisedBevelBorder());
            }
         }

      }, () -> {
         if (this.selectedAction != null) {
            this.actionDetailsPanel.setImageForSelectedPose("/" + imageFile.getName());
            String var10001 = imageFile.getName();
            this.updateStatusMessage("设置图片: " + var10001 + " / Set image: " + imageFile.getName());
         }

      });
      this.imageGridPanel.add(imagePanel);
   }

   private void selectAction(AnimationAction action) {
      this.selectedAction = action;
      this.actionDetailsPanel.setAction(action);
      this.previewPanel.setAction(action);
      if (action != null) {
         String var10001 = action.getName();
         this.updateStatusMessage("编辑动作: " + var10001 + " / Editing action: " + action.getName());
      } else {
         this.updateStatusMessage("未选择动作 / No action selected");
      }

   }

   private void browseForImageDirectory() {
      JFileChooser chooser = new JFileChooser(this.currentImageDirectory);
      chooser.setFileSelectionMode(1);
      chooser.setDialogTitle("选择图片目录 / Select Image Directory");
      if (chooser.showOpenDialog(this) == 0) {
         File selectedDir = chooser.getSelectedFile();
         this.currentImageDirectory = selectedDir;
         this.currentDirectoryLabel.setText("当前目录 / Current: " + this.currentImageDirectory.getAbsolutePath());
         this.loadImagesFromDirectory(selectedDir);
      }

   }

   private void openProject() {
      JFileChooser chooser = new JFileChooser("./img");
      chooser.setFileSelectionMode(1);
      chooser.setDialogTitle("选择项目目录 / Select Project Directory");
      if (chooser.showOpenDialog(this) == 0) {
         File selectedDir = chooser.getSelectedFile();
         if (ProjectScanner.isValidProject(selectedDir)) {
            this.loadProjects();
         } else {
            JOptionPane.showMessageDialog(this, "选中的目录不是有效的项目目录（缺少 conf/actions.xml 文件）\nThe selected directory is not a valid project directory (missing conf/actions.xml file)", "无效项目 / Invalid Project", 2);
         }
      }

   }

   private void saveProject() {
      if (this.currentProject == null) {
         JOptionPane.showMessageDialog(this, "请先选择一个项目\nPlease select a project first", "无项目 / No Project", 2);
      } else {
         boolean success = XMLConfigUtil.saveActionsToXML(this.currentProject.getActions(), this.currentProject.getConfigFile());
         if (success) {
            JOptionPane.showMessageDialog(this, "项目保存成功\nProject saved successfully", "保存成功 / Save Success", 1);
            this.updateStatusMessage("项目已保存 / Project saved");
         } else {
            JOptionPane.showMessageDialog(this, "项目保存失败\nProject save failed", "保存失败 / Save Failed", 0);
         }

      }
   }

   private void createNewAction() {
      if (this.currentProject == null) {
         JOptionPane.showMessageDialog(this, "请先选择一个项目\nPlease select a project first", "无项目 / No Project", 2);
      } else {
         String actionName = JOptionPane.showInputDialog(this, "输入动作名称\nEnter action name:", "新动作 / New Action", 3);
         if (actionName != null && !actionName.trim().isEmpty()) {
            AnimationAction newAction = new AnimationAction(actionName.trim(), "Stay");
            this.currentProject.getActions().add(newAction);
            this.loadProjects();
            this.updateStatusMessage("创建了新动作: " + actionName + " / Created new action: " + actionName);
         }

      }
   }

   private void deleteSelectedAction() {
      if (this.selectedAction == null) {
         JOptionPane.showMessageDialog(this, "请先选择一个动作\nPlease select an action first", "无选择 / No Selection", 2);
      } else {
         int result = JOptionPane.showConfirmDialog(this, "确定要删除动作 \"" + this.selectedAction.getName() + "\" 吗？\nAre you sure you want to delete action \"" + this.selectedAction.getName() + "\"?", "确认删除 / Confirm Delete", 0);
         if (result == 0) {
            this.currentProject.getActions().remove(this.selectedAction);
            this.selectAction((AnimationAction)null);
            this.loadProjects();
            this.updateStatusMessage("删除了动作 / Deleted action");
         }

      }
   }

   private void expandAllTreeNodes() {
      for(int i = 0; i < this.projectTree.getRowCount(); ++i) {
         this.projectTree.expandRow(i);
      }

   }

   private void collapseAllTreeNodes() {
      for(int i = this.projectTree.getRowCount() - 1; i >= 1; --i) {
         this.projectTree.collapseRow(i);
      }

   }

   private void updateStatusMessage(String message) {
      log.info(message);
   }
}
