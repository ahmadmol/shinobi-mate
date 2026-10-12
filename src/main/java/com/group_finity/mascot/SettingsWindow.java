package com.group_finity.mascot;

import com.formdev.flatlaf.FlatLaf;
import com.group_finity.mascot.license.LicenseLevel;
import com.group_finity.mascot.license.LicenseManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.image.ImageObserver;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;

public class SettingsWindow extends JDialog {
   private final String themeFile;
   private Properties themeProperties = new Properties();
   private final Properties oldThemeProperties = new Properties();
   private LookAndFeel lookAndFeel;
   private final ArrayList listData = new ArrayList();
   private final ArrayList blacklistData = new ArrayList();
   private Boolean alwaysShowShimejiChooser = false;
   private Boolean alwaysShowInformationScreen = false;
   private String filter = "nearest";
   private double scaling = (double)1.0F;
   private double opacity = (double)1.0F;
   private Boolean windowedMode = false;
   private Dimension windowSize = new Dimension(600, 500);
   private Color backgroundColour = new Color(0, 255, 0);
   private String backgroundMode = "centre";
   private String backgroundImage = null;
   private final String[] backgroundModes = new String[]{"centre", "fill", "fit", "stretch"};
   private Color primaryColour1;
   private Color primaryColour2;
   private Color primaryColour3;
   private Color secondaryColour1;
   private Color secondaryColour2;
   private Color secondaryColour3;
   private Color blackColour;
   private Color whiteColour;
   private Font font;
   private double menuOpacity = (double)1.0F;
   private Boolean imageReloadRequired = false;
   private Boolean interactiveWindowReloadRequired = false;
   private Boolean environmentReloadRequired = false;
   private JButton btnAddInteractiveWindow;
   private JButton btnBackgroundColourChange;
   private JButton btnBackgroundImageChange;
   private JButton btnBackgroundImageRemove;
   private JButton btnBlackColourChange;
   private JButton btnCancel;
   private JButton btnChangeFont;
   private JButton btnDiscord;
   private JButton btnDone;
   private JButton btnFilterHelp;
   private JButton btnPatreon;
   private JButton btnPrimaryColour1Change;
   private JButton btnPrimaryColour2Change;
   private JButton btnPrimaryColour3Change;
   private JButton btnRemoveInteractiveWindow;
   private JButton btnReset;
   private JButton btnSecondaryColour1Change;
   private JButton btnSecondaryColour2Change;
   private JButton btnSecondaryColour3Change;
   private JButton btnWebsite;
   private JButton btnWhiteColourChange;
   private JCheckBox chkAlwaysShowInformationScreen;
   private JCheckBox chkAlwaysShowShimejiChooser;
   private JCheckBox chkWindowModeEnabled;
   private JComboBox cmbBackgroundImageMode;
   private ButtonGroup grpFilter;
   private JLabel lblBackground;
   private JLabel lblBackgroundImage;
   private JLabel lblBlackColour;
   private JLabel lblDevelopedBy;
   private JLabel lblDimensions;
   private JLabel lblFilter;
   private JLabel lblIcon;
   private JLabel lblMenuOpacity;
   private JLabel lblOpacity;
   private JLabel lblPrimaryColour1;
   private JLabel lblPrimaryColour2;
   private JLabel lblPrimaryColour3;
   private JLabel lblScaling;
   private JLabel lblSecondaryColour1;
   private JLabel lblSecondaryColour2;
   private JLabel lblSecondaryColour3;
   private JLabel lblShimejiEE;
   private JLabel lblWhiteColour;
   private JList lstInteractiveWindows;
   private JList lstInteractiveWindowsBlacklist;
   private JTabbedPane pnlInteractiveTabs;
   private JPanel pnlWhitelistTab;
   private JPanel pnlBlacklistTab;
   private JPanel pnlAboutButtons;
   private JPanel pnlBackgroundImage;
   private JPanel pnlBackgroundPreview;
   private JPanel pnlBackgroundPreviewContainer;
   private JPanel pnlBlackColourPreview;
   private JPanel pnlBlackColourPreviewContainer;
   private JPanel pnlFooter;
   private JPanel pnlInteractiveButtons;
   private JPanel pnlPrimaryColour1Preview;
   private JPanel pnlPrimaryColour1PreviewContainer;
   private JPanel pnlPrimaryColour2Preview;
   private JPanel pnlPrimaryColour2PreviewContainer;
   private JPanel pnlPrimaryColour3Preview;
   private JPanel pnlPrimaryColour3PreviewContainer;
   private JPanel pnlSecondaryColour1Preview;
   private JPanel pnlSecondaryColour1PreviewContainer;
   private JPanel pnlSecondaryColour2Preview;
   private JPanel pnlSecondaryColour2PreviewContainer;
   private JPanel pnlSecondaryColour3Preview;
   private JPanel pnlSecondaryColour3PreviewContainer;
   private JTabbedPane pnlTabs;
   private JPanel pnlThemeButtons;
   private JPanel pnlWhiteColourPreview;
   private JPanel pnlWhiteColourPreviewContainer;
   private JRadioButton radFilterBicubic;
   private JRadioButton radFilterHqx;
   private JRadioButton radFilterNearest;
   private JSlider sldMenuOpacity;
   private JSlider sldOpacity;
   private JSlider sldScaling;
   private JSpinner spnScaling;
   private JSpinner spnWindowHeight;
   private JSpinner spnWindowWidth;
   private JTextField txtBackgroundColour;
   private JTextField txtBlackColour;
   private JTextField txtPrimaryColour1;
   private JTextField txtPrimaryColour2;
   private JTextField txtPrimaryColour3;
   private JTextField txtSecondaryColour1;
   private JTextField txtSecondaryColour2;
   private JTextField txtSecondaryColour3;
   private JTextField txtWhiteColour;

   public SettingsWindow(JFrame parent, boolean modal) {
      super(parent, modal);
      com.group_finity.mascot.ui.AppIcon.apply(this);
      String baseDir = System.getProperty("app.dir");
      if (baseDir == null) {
         baseDir = System.getProperty("user.dir");
      }

      if (baseDir == null) {
         baseDir = ".";
      }

      this.themeFile = com.group_finity.mascot.AppPaths.userData().resolve("theme.properties").toString();
      this.initComponents();
   }

   public void init() {
      this.setLocationRelativeTo((Component)null);
      this.grpFilter.add(this.radFilterNearest);
      this.grpFilter.add(this.radFilterBicubic);
      this.grpFilter.add(this.radFilterHqx);
      this.sldScaling.setMinimum(5);
      this.sldScaling.setMaximum(40);
      this.sldScaling.setMajorTickSpacing(5);
      this.sldScaling.setMinorTickSpacing(1);
      this.sldScaling.setPaintLabels(true);
      this.sldScaling.setPaintTicks(true);
      this.sldScaling.setSnapToTicks(false);
      this.sldScaling.setValue(10);
      Hashtable<Integer, JLabel> labelTable = new Hashtable();
      labelTable.put(5, new JLabel("0.5x"));
      labelTable.put(10, new JLabel("1x"));
      labelTable.put(15, new JLabel("1.5x"));
      labelTable.put(20, new JLabel("2x"));
      labelTable.put(25, new JLabel("2.5x"));
      labelTable.put(30, new JLabel("3x"));
      labelTable.put(35, new JLabel("3.5x"));
      labelTable.put(40, new JLabel("4x"));
      this.sldScaling.setLabelTable(labelTable);
      Properties properties = Main.getInstance().getProperties();
      com.group_finity.mascot.settings.SettingsStore.sanitize(properties);
      this.alwaysShowShimejiChooser = Boolean.parseBoolean(properties.getProperty("AlwaysShowShimejiChooser", "false"));
      this.alwaysShowInformationScreen = Boolean.parseBoolean(properties.getProperty("AlwaysShowInformationScreen", "false"));
      String filterText = Main.getInstance().getProperties().getProperty("Filter", "false");
      this.filter = "nearest";
      if (!filterText.equalsIgnoreCase("true") && !filterText.equalsIgnoreCase("hqx")) {
         if (filterText.equalsIgnoreCase("bicubic")) {
            this.filter = "bicubic";
         }
      } else {
         this.filter = "hqx";
      }

      this.opacity = Double.parseDouble(properties.getProperty("Opacity", "1.0"));
      this.scaling = Double.parseDouble(properties.getProperty("Scaling", "1.0"));
      this.windowedMode = properties.getProperty("Environment", "generic").equals("virtual");
      String[] windowArray = properties.getProperty("WindowSize", "600x500").split("x");
      this.windowSize = new Dimension(Integer.parseInt(windowArray[0]), Integer.parseInt(windowArray[1]));
      Dimension buttonSize = this.btnDone.getPreferredSize();
      Dimension aboutButtonSize = this.btnWebsite.getPreferredSize();
      this.backgroundColour = Color.decode(properties.getProperty("Background", "#00FF00"));
      this.backgroundImage = properties.getProperty("BackgroundImage", "");
      this.backgroundMode = properties.getProperty("BackgroundMode", "centre");
      float menuScaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96.0F;
      this.chkAlwaysShowShimejiChooser.setSelected(this.alwaysShowShimejiChooser);
      this.chkAlwaysShowInformationScreen.setSelected(this.alwaysShowInformationScreen);
      if (this.filter.equals("bicubic")) {
         this.radFilterBicubic.setSelected(true);
      } else if (this.filter.equals("hqx")) {
         this.radFilterHqx.setSelected(true);
      } else {
         this.radFilterNearest.setSelected(true);
      }

      this.sldOpacity.setValue((int)(this.opacity * (double)100.0F));
      this.sldScaling.setValue((int)(this.scaling * (double)10.0F));
      this.spnScaling.setValue(this.scaling);
      this.listData.addAll(Arrays.asList(properties.getProperty("InteractiveWindows", "").split("/")));
      this.lstInteractiveWindows.setListData((String[])this.listData.toArray(new String[0]));

      for(String item : properties.getProperty("InteractiveWindowsBlacklist", "").split("/")) {
         if (!item.trim().isEmpty()) {
            this.blacklistData.add(item);
         }
      }

      this.lstInteractiveWindowsBlacklist.setListData((String[])this.blacklistData.toArray(new String[0]));
      Properties themeProperties = new Properties();

      try {
         try(FileInputStream input = new FileInputStream(com.group_finity.mascot.AppPaths.theme().toFile())) {themeProperties.load(input);}
      } catch (IOException | IllegalArgumentException var13) {
         themeProperties.clear();
      }

      this.primaryColour1 = safeColor(themeProperties,"PrimaryColour1","#1EA6EB");
      this.primaryColour2 = safeColor(themeProperties,"PrimaryColour2","#28B0F5");
      this.primaryColour3 = safeColor(themeProperties,"PrimaryColour3","#32BAFF");
      this.secondaryColour1 = safeColor(themeProperties,"SecondaryColour1","#BCBCBE");
      this.secondaryColour2 = safeColor(themeProperties,"SecondaryColour2","#C6C6C8");
      this.secondaryColour3 = safeColor(themeProperties,"SecondaryColour3","#D0D0D2");
      this.blackColour = safeColor(themeProperties,"BlackColour","#000000");
      this.whiteColour = safeColor(themeProperties,"WhiteColour","#FFFFFF");
      this.menuOpacity = (double)com.group_finity.mascot.settings.RuntimeSettings.number(themeProperties,"MenuOpacity",255,0,255) / (double)255.0F;
      String var10001 = themeProperties.getProperty("FontName", "SansSerif");
      this.font = new Font(var10001,(int)com.group_finity.mascot.settings.RuntimeSettings.number(themeProperties,"FontStyle",0,0,3),(int)com.group_finity.mascot.settings.RuntimeSettings.number(themeProperties,"FontSize",12,8,32));
      this.font = this.font.deriveFont((float)this.font.getSize() * menuScaling);
      this.pnlPrimaryColour1Preview.setBackground(this.primaryColour1);
      this.txtPrimaryColour1.setText(String.format("#%02X%02X%02X", this.primaryColour1.getRed(), this.primaryColour1.getGreen(), this.primaryColour1.getBlue()));
      this.pnlPrimaryColour2Preview.setBackground(this.primaryColour2);
      this.txtPrimaryColour2.setText(String.format("#%02X%02X%02X", this.primaryColour2.getRed(), this.primaryColour2.getGreen(), this.primaryColour2.getBlue()));
      this.pnlPrimaryColour3Preview.setBackground(this.primaryColour3);
      this.txtPrimaryColour3.setText(String.format("#%02X%02X%02X", this.primaryColour3.getRed(), this.primaryColour2.getGreen(), this.primaryColour3.getBlue()));
      this.pnlSecondaryColour1Preview.setBackground(this.secondaryColour1);
      this.txtSecondaryColour1.setText(String.format("#%02X%02X%02X", this.secondaryColour1.getRed(), this.secondaryColour1.getGreen(), this.secondaryColour1.getBlue()));
      this.pnlSecondaryColour2Preview.setBackground(this.secondaryColour2);
      this.txtSecondaryColour2.setText(String.format("#%02X%02X%02X", this.secondaryColour2.getRed(), this.secondaryColour2.getGreen(), this.secondaryColour2.getBlue()));
      this.pnlSecondaryColour3Preview.setBackground(this.secondaryColour3);
      this.txtSecondaryColour3.setText(String.format("#%02X%02X%02X", this.secondaryColour3.getRed(), this.secondaryColour3.getGreen(), this.secondaryColour3.getBlue()));
      this.pnlBlackColourPreview.setBackground(this.blackColour);
      this.txtBlackColour.setText(String.format("#%02X%02X%02X", this.blackColour.getRed(), this.blackColour.getGreen(), this.blackColour.getBlue()));
      this.pnlWhiteColourPreview.setBackground(this.whiteColour);
      this.txtWhiteColour.setText(String.format("#%02X%02X%02X", this.whiteColour.getRed(), this.whiteColour.getGreen(), this.whiteColour.getBlue()));
      this.initializeFlatLafTheme();
      this.sldMenuOpacity.setValue((int)(this.menuOpacity * (double)100.0F));
      this.lookAndFeel = UIManager.getLookAndFeel();
      this.chkWindowModeEnabled.setSelected(this.windowedMode);
      this.spnWindowWidth.setBackground(this.txtBackgroundColour.getBackground());
      this.spnWindowHeight.setBackground(this.txtBackgroundColour.getBackground());
      this.spnWindowWidth.setEnabled(this.windowedMode);
      this.spnWindowHeight.setEnabled(this.windowedMode);
      this.spnWindowWidth.setValue(this.windowSize.width);
      this.spnWindowHeight.setValue(this.windowSize.height);
      this.txtBackgroundColour.setText(String.format("#%02X%02X%02X", this.backgroundColour.getRed(), this.backgroundColour.getGreen(), this.backgroundColour.getBlue()));
      this.btnBackgroundColourChange.setEnabled(this.windowedMode);
      this.btnBackgroundImageChange.setEnabled(this.windowedMode);
      this.pnlBackgroundPreview.setBackground(this.backgroundColour);
      if (this.backgroundImage != null) {
         try {
            Dimension size = this.pnlBackgroundImage.getPreferredSize();
            this.refreshBackgroundImage();
            this.pnlBackgroundImage.setPreferredSize(size);
         } catch (Exception var12) {
            this.backgroundImage = null;
            this.lblBackgroundImage.setIcon((Icon)null);
         }
      }

      this.cmbBackgroundImageMode.setEnabled(this.windowedMode && this.backgroundImage != null);
      this.btnBackgroundImageRemove.setEnabled(this.windowedMode && this.backgroundImage != null);
      ResourceBundle language = Main.getInstance().getLanguageBundle();
      this.setTitle(language.getString("Settings"));
      this.pnlTabs.setTitleAt(0, language.getString("General"));
      this.pnlTabs.setTitleAt(1, language.getString("InteractiveWindows"));
      this.pnlTabs.setTitleAt(2, language.getString("Theme"));
      this.pnlTabs.setTitleAt(3, language.getString("WindowMode"));
      this.pnlTabs.setTitleAt(4, language.getString("About"));
      this.chkAlwaysShowShimejiChooser.setText(language.getString("AlwaysShowShimejiChooser"));
      this.chkAlwaysShowInformationScreen.setText(language.getString("AlwaysShowInformationScreen"));
      this.lblOpacity.setText(language.getString("Opacity"));
      this.lblScaling.setText(language.getString("Scaling"));
      this.lblFilter.setText(language.getString("FilterOptions"));
      this.radFilterNearest.setText(language.getString("NearestNeighbour"));
      this.radFilterHqx.setText(language.getString("Filter"));
      this.radFilterBicubic.setText(language.getString("BicubicFilter"));
      this.pnlInteractiveTabs.setTitleAt(0, language.getString("Whitelist"));
      this.pnlInteractiveTabs.setTitleAt(1, language.getString("Blacklist"));
      this.btnAddInteractiveWindow.setText(language.getString("Add"));
      this.btnRemoveInteractiveWindow.setText(language.getString("Remove"));
      this.lblPrimaryColour1.setText(language.getString("PrimaryColour1"));
      this.lblPrimaryColour2.setText(language.getString("PrimaryColour2"));
      this.lblPrimaryColour3.setText(language.getString("PrimaryColour3"));
      this.lblSecondaryColour1.setText(language.getString("SecondaryColour1"));
      this.lblSecondaryColour2.setText(language.getString("SecondaryColour2"));
      this.lblSecondaryColour3.setText(language.getString("SecondaryColour3"));
      this.lblBlackColour.setText(language.getString("BlackColour"));
      this.lblWhiteColour.setText(language.getString("WhiteColour"));
      this.lblMenuOpacity.setText(language.getString("MenuOpacity"));
      this.btnChangeFont.setText(language.getString("ChangeFont"));
      this.btnReset.setText(language.getString("Reset"));
      this.chkWindowModeEnabled.setText(language.getString("WindowedModeEnabled"));
      this.lblDimensions.setText(language.getString("Dimensions"));
      this.lblBackground.setText(language.getString("Background"));
      this.btnBackgroundColourChange.setText(language.getString("Change"));
      this.btnBackgroundImageChange.setText(language.getString("Change"));
      this.cmbBackgroundImageMode.addItem(language.getString("BackgroundModeCentre"));
      this.cmbBackgroundImageMode.addItem(language.getString("BackgroundModeFill"));
      this.cmbBackgroundImageMode.addItem(language.getString("BackgroundModeFit"));
      this.cmbBackgroundImageMode.addItem(language.getString("BackgroundModeStretch"));
      this.btnBackgroundImageRemove.setText(language.getString("Remove"));
      this.lblShimejiEE.setText(language.getString("ShimejiEE"));
      this.lblDevelopedBy.setText(language.getString("DevelopedBy"));
      this.btnWebsite.setText(language.getString("Website"));
      this.btnFilterHelp.setToolTipText(language.getString("FilterHelpTooltip"));
      this.btnDone.setText(language.getString("Done"));
      this.btnCancel.setText(language.getString("Cancel"));

      for(int index = 0; index < this.backgroundModes.length; ++index) {
         if (this.backgroundMode.equals(this.backgroundModes[index])) {
            this.cmbBackgroundImageMode.setSelectedIndex(index);
            break;
         }
      }

   }

   public void display() {
      float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      this.getContentPane().setPreferredSize(new Dimension((int)(600.0F * menuScaling), (int)(497.0F * menuScaling)));
      this.sldOpacity.setPreferredSize(new Dimension((int)((float)this.sldOpacity.getPreferredSize().width * menuScaling), (int)((float)this.sldOpacity.getPreferredSize().height * menuScaling)));
      this.sldScaling.setPreferredSize(new Dimension((int)((float)this.sldScaling.getPreferredSize().width * menuScaling), (int)((float)this.sldScaling.getPreferredSize().height * menuScaling)));
      this.spnScaling.setPreferredSize(new Dimension(DPIManager.scaleWidth(120), DPIManager.scaleHeight(50)));
      this.spnScaling.setFont(this.spnScaling.getFont().deriveFont((float)DPIManager.scaleFontSize(this.spnScaling.getFont().getSize())));
      this.btnAddInteractiveWindow.setPreferredSize(new Dimension((int)((float)this.btnAddInteractiveWindow.getPreferredSize().width * menuScaling), (int)((float)this.btnAddInteractiveWindow.getPreferredSize().height * menuScaling)));
      this.btnRemoveInteractiveWindow.setPreferredSize(new Dimension((int)((float)this.btnRemoveInteractiveWindow.getPreferredSize().width * menuScaling), (int)((float)this.btnRemoveInteractiveWindow.getPreferredSize().height * menuScaling)));
      this.pnlInteractiveButtons.setPreferredSize(new Dimension(this.pnlInteractiveButtons.getPreferredSize().width, this.btnAddInteractiveWindow.getPreferredSize().height + 6));
      this.txtPrimaryColour1.setPreferredSize(new Dimension((int)((float)this.txtPrimaryColour1.getPreferredSize().width * menuScaling), (int)((float)this.txtPrimaryColour1.getPreferredSize().height * menuScaling)));
      this.txtPrimaryColour2.setPreferredSize(new Dimension((int)((float)this.txtPrimaryColour2.getPreferredSize().width * menuScaling), (int)((float)this.txtPrimaryColour2.getPreferredSize().height * menuScaling)));
      this.txtPrimaryColour3.setPreferredSize(new Dimension((int)((float)this.txtPrimaryColour3.getPreferredSize().width * menuScaling), (int)((float)this.txtPrimaryColour3.getPreferredSize().height * menuScaling)));
      this.txtSecondaryColour1.setPreferredSize(new Dimension((int)((float)this.txtSecondaryColour1.getPreferredSize().width * menuScaling), (int)((float)this.txtSecondaryColour1.getPreferredSize().height * menuScaling)));
      this.txtSecondaryColour2.setPreferredSize(new Dimension((int)((float)this.txtSecondaryColour2.getPreferredSize().width * menuScaling), (int)((float)this.txtSecondaryColour2.getPreferredSize().height * menuScaling)));
      this.txtSecondaryColour3.setPreferredSize(new Dimension((int)((float)this.txtSecondaryColour3.getPreferredSize().width * menuScaling), (int)((float)this.txtSecondaryColour3.getPreferredSize().height * menuScaling)));
      this.txtBlackColour.setPreferredSize(new Dimension((int)((float)this.txtBlackColour.getPreferredSize().width * menuScaling), (int)((float)this.txtBlackColour.getPreferredSize().height * menuScaling)));
      this.txtWhiteColour.setPreferredSize(new Dimension((int)((float)this.txtWhiteColour.getPreferredSize().width * menuScaling), (int)((float)this.txtWhiteColour.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour1PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour1PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour1PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour1Preview.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour1Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour1Preview.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour2PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour2PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour2PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour2Preview.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour2Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour2Preview.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour3PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour3PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour3PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlPrimaryColour3Preview.setPreferredSize(new Dimension((int)((float)this.pnlPrimaryColour3Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlPrimaryColour3Preview.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour1PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour1PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour1PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour1Preview.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour1Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour1Preview.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour2PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour2PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour2PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour2Preview.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour2Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour2Preview.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour3PreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour3PreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour3PreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlSecondaryColour3Preview.setPreferredSize(new Dimension((int)((float)this.pnlSecondaryColour3Preview.getPreferredSize().width * menuScaling), (int)((float)this.pnlSecondaryColour3Preview.getPreferredSize().height * menuScaling)));
      this.pnlBlackColourPreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlBlackColourPreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlBlackColourPreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlBlackColourPreview.setPreferredSize(new Dimension((int)((float)this.pnlBlackColourPreview.getPreferredSize().width * menuScaling), (int)((float)this.pnlBlackColourPreview.getPreferredSize().height * menuScaling)));
      this.pnlWhiteColourPreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlWhiteColourPreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlWhiteColourPreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlWhiteColourPreview.setPreferredSize(new Dimension((int)((float)this.pnlWhiteColourPreview.getPreferredSize().width * menuScaling), (int)((float)this.pnlWhiteColourPreview.getPreferredSize().height * menuScaling)));
      this.btnPrimaryColour1Change.setPreferredSize(new Dimension((int)((float)this.btnPrimaryColour1Change.getPreferredSize().width * menuScaling), (int)((float)this.btnPrimaryColour1Change.getPreferredSize().height * menuScaling)));
      this.btnPrimaryColour2Change.setPreferredSize(new Dimension((int)((float)this.btnPrimaryColour2Change.getPreferredSize().width * menuScaling), (int)((float)this.btnPrimaryColour2Change.getPreferredSize().height * menuScaling)));
      this.btnPrimaryColour3Change.setPreferredSize(new Dimension((int)((float)this.btnPrimaryColour3Change.getPreferredSize().width * menuScaling), (int)((float)this.btnPrimaryColour3Change.getPreferredSize().height * menuScaling)));
      this.btnSecondaryColour1Change.setPreferredSize(new Dimension((int)((float)this.btnSecondaryColour1Change.getPreferredSize().width * menuScaling), (int)((float)this.btnSecondaryColour1Change.getPreferredSize().height * menuScaling)));
      this.btnSecondaryColour2Change.setPreferredSize(new Dimension((int)((float)this.btnSecondaryColour2Change.getPreferredSize().width * menuScaling), (int)((float)this.btnSecondaryColour2Change.getPreferredSize().height * menuScaling)));
      this.btnSecondaryColour3Change.setPreferredSize(new Dimension((int)((float)this.btnSecondaryColour3Change.getPreferredSize().width * menuScaling), (int)((float)this.btnSecondaryColour3Change.getPreferredSize().height * menuScaling)));
      this.btnBlackColourChange.setPreferredSize(new Dimension((int)((float)this.btnBlackColourChange.getPreferredSize().width * menuScaling), (int)((float)this.btnBlackColourChange.getPreferredSize().height * menuScaling)));
      this.btnWhiteColourChange.setPreferredSize(new Dimension((int)((float)this.btnWhiteColourChange.getPreferredSize().width * menuScaling), (int)((float)this.btnWhiteColourChange.getPreferredSize().height * menuScaling)));
      this.sldMenuOpacity.setPreferredSize(new Dimension((int)((float)this.sldMenuOpacity.getPreferredSize().width * menuScaling), (int)((float)this.sldMenuOpacity.getPreferredSize().height * menuScaling)));
      this.btnChangeFont.setPreferredSize(new Dimension((int)((float)this.btnChangeFont.getPreferredSize().width * menuScaling), (int)((float)this.btnChangeFont.getPreferredSize().height * menuScaling)));
      this.btnReset.setPreferredSize(new Dimension((int)((float)this.btnReset.getPreferredSize().width * menuScaling), (int)((float)this.btnReset.getPreferredSize().height * menuScaling)));
      this.pnlThemeButtons.setPreferredSize(new Dimension(this.pnlThemeButtons.getPreferredSize().width, this.btnReset.getPreferredSize().height + 6));
      this.spnWindowWidth.setPreferredSize(new Dimension((int)((float)this.spnWindowWidth.getPreferredSize().width * menuScaling), (int)((float)this.spnWindowWidth.getPreferredSize().height * menuScaling)));
      this.spnWindowHeight.setPreferredSize(new Dimension((int)((float)this.spnWindowHeight.getPreferredSize().width * menuScaling), (int)((float)this.spnWindowHeight.getPreferredSize().height * menuScaling)));
      this.txtBackgroundColour.setPreferredSize(new Dimension((int)((float)this.txtBackgroundColour.getPreferredSize().width * menuScaling), (int)((float)this.txtBackgroundColour.getPreferredSize().height * menuScaling)));
      this.pnlBackgroundPreviewContainer.setPreferredSize(new Dimension((int)((float)this.pnlBackgroundPreviewContainer.getPreferredSize().width * menuScaling), (int)((float)this.pnlBackgroundPreviewContainer.getPreferredSize().height * menuScaling)));
      this.pnlBackgroundPreview.setPreferredSize(new Dimension((int)((float)this.pnlBackgroundPreview.getPreferredSize().width * menuScaling), (int)((float)this.pnlBackgroundPreview.getPreferredSize().height * menuScaling)));
      this.btnBackgroundColourChange.setPreferredSize(new Dimension((int)((float)this.btnBackgroundColourChange.getPreferredSize().width * menuScaling), (int)((float)this.btnBackgroundColourChange.getPreferredSize().height * menuScaling)));
      this.btnBackgroundImageChange.setPreferredSize(new Dimension((int)((float)this.btnBackgroundImageChange.getPreferredSize().width * menuScaling), (int)((float)this.btnBackgroundImageChange.getPreferredSize().height * menuScaling)));
      this.btnBackgroundImageRemove.setPreferredSize(new Dimension((int)((float)this.btnBackgroundImageRemove.getPreferredSize().width * menuScaling), (int)((float)this.btnBackgroundImageRemove.getPreferredSize().height * menuScaling)));
      this.cmbBackgroundImageMode.setPreferredSize(this.btnBackgroundImageRemove.getPreferredSize());
      this.pnlBackgroundImage.setPreferredSize(new Dimension((int)((float)this.pnlBackgroundImage.getPreferredSize().width * menuScaling), (int)((float)this.pnlBackgroundImage.getPreferredSize().height * menuScaling)));
      this.pnlBackgroundImage.setMaximumSize(this.pnlBackgroundImage.getPreferredSize());
      this.lblIcon.setPreferredSize(new Dimension((int)((float)this.lblIcon.getPreferredSize().width * menuScaling), (int)((float)this.lblIcon.getPreferredSize().height * menuScaling)));
      this.lblIcon.setMaximumSize(this.lblIcon.getPreferredSize());
      if (!this.getIconImages().isEmpty()) {
         this.lblIcon.setIcon(new ImageIcon(((Image)this.getIconImages().getFirst()).getScaledInstance(this.lblIcon.getPreferredSize().width, this.lblIcon.getPreferredSize().height, 1)));
      }

      this.btnWebsite.setPreferredSize(new Dimension((int)((float)this.btnWebsite.getPreferredSize().width * menuScaling), (int)((float)this.btnWebsite.getPreferredSize().height * menuScaling)));
      this.btnDiscord.setPreferredSize(new Dimension((int)((float)this.btnDiscord.getPreferredSize().width * menuScaling), (int)((float)this.btnDiscord.getPreferredSize().height * menuScaling)));
      this.btnPatreon.setPreferredSize(new Dimension((int)((float)this.btnPatreon.getPreferredSize().width * menuScaling), (int)((float)this.btnPatreon.getPreferredSize().height * menuScaling)));
      this.pnlAboutButtons.setPreferredSize(new Dimension(this.pnlAboutButtons.getPreferredSize().width, this.btnWebsite.getPreferredSize().height + 6));
      this.btnDone.setPreferredSize(new Dimension((int)((float)this.btnDone.getPreferredSize().width * menuScaling), (int)((float)this.btnDone.getPreferredSize().height * menuScaling)));
      this.btnCancel.setPreferredSize(new Dimension((int)((float)this.btnCancel.getPreferredSize().width * menuScaling), (int)((float)this.btnCancel.getPreferredSize().height * menuScaling)));
      this.pnlFooter.setPreferredSize(new Dimension(this.pnlFooter.getPreferredSize().width, this.btnDone.getPreferredSize().height + 6));
      this.pack();
      this.setVisible(true);
   }

   private void initializeFlatLafTheme() {
      this.themeProperties.clear();
      this.themeProperties.setProperty("PrimaryColour1", String.format("#%02X%02X%02X", this.primaryColour1.getRed(), this.primaryColour1.getGreen(), this.primaryColour1.getBlue()));
      this.themeProperties.setProperty("PrimaryColour2", String.format("#%02X%02X%02X", this.primaryColour2.getRed(), this.primaryColour2.getGreen(), this.primaryColour2.getBlue()));
      this.themeProperties.setProperty("PrimaryColour3", String.format("#%02X%02X%02X", this.primaryColour3.getRed(), this.primaryColour3.getGreen(), this.primaryColour3.getBlue()));
      this.themeProperties.setProperty("SecondaryColour1", String.format("#%02X%02X%02X", this.secondaryColour1.getRed(), this.secondaryColour1.getGreen(), this.secondaryColour1.getBlue()));
      this.themeProperties.setProperty("SecondaryColour2", String.format("#%02X%02X%02X", this.secondaryColour2.getRed(), this.secondaryColour2.getGreen(), this.secondaryColour2.getBlue()));
      this.themeProperties.setProperty("SecondaryColour3", String.format("#%02X%02X%02X", this.secondaryColour3.getRed(), this.secondaryColour3.getGreen(), this.secondaryColour3.getBlue()));
      this.themeProperties.setProperty("BlackColour", String.format("#%02X%02X%02X", this.blackColour.getRed(), this.blackColour.getGreen(), this.blackColour.getBlue()));
      this.themeProperties.setProperty("WhiteColour", String.format("#%02X%02X%02X", this.whiteColour.getRed(), this.whiteColour.getGreen(), this.whiteColour.getBlue()));
      this.themeProperties.setProperty("MenuOpacity", String.valueOf((int)(this.menuOpacity * (double)255.0F)));
      this.themeProperties.setProperty("FontName", this.font.getName());
      this.themeProperties.setProperty("FontStyle", String.valueOf(this.font.getStyle()));
      this.themeProperties.setProperty("FontSize", String.valueOf(this.font.getSize()));
      this.oldThemeProperties.clear();
      this.oldThemeProperties.putAll(this.themeProperties);
   }

   private void browseToUrl(String url) {
      try {
         Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
         if (desktop == null || !desktop.isSupported(Action.BROWSE)) {
            String var10002 = Main.getInstance().getLanguageBundle().getString("FailedOpenWebBrowserErrorMessage");
            throw new UnsupportedOperationException(var10002 + " " + url);
         }

         desktop.browse(new URI(url));
      } catch (Exception e) {
         JOptionPane.showMessageDialog(this, e.getMessage(), "Error", -1);
      }

   }

   private Font showFontDialog(JFrame parent, Font currentFont) {
      String[] fontNames = new String[]{"SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput"};
      String selectedName = (String)JOptionPane.showInputDialog(parent, "选择字体:", "字体选择", 3, (Icon)null, fontNames, currentFont.getName());
      if (selectedName == null) {
         return null;
      } else {
         int style = 0;
         return new Font(selectedName, style, currentFont.getSize());
      }
   }

   public boolean getEnvironmentReloadRequired() {
      return this.environmentReloadRequired;
   }

   public boolean getImageReloadRequired() {
      return this.imageReloadRequired;
   }

   public boolean getInteractiveWindowReloadRequired() {
      return this.interactiveWindowReloadRequired;
   }

   private void initComponents() {
      this.grpFilter = new ButtonGroup();
      this.pnlTabs = new JTabbedPane();
      JPanel pnlGeneral = new JPanel();
      this.chkAlwaysShowShimejiChooser = new JCheckBox();
      this.lblScaling = new JLabel();
      this.sldScaling = new JSlider();
      this.spnScaling = new JSpinner();
      this.lblFilter = new JLabel();
      this.radFilterNearest = new JRadioButton();
      this.radFilterBicubic = new JRadioButton();
      this.radFilterHqx = new JRadioButton();
      this.btnFilterHelp = new JButton();
      this.sldOpacity = new JSlider();
      this.lblOpacity = new JLabel();
      this.chkAlwaysShowInformationScreen = new JCheckBox();
      JPanel pnlInteractiveWindows = new JPanel();
      this.pnlInteractiveTabs = new JTabbedPane();
      this.pnlWhitelistTab = new JPanel();
      this.pnlBlacklistTab = new JPanel();
      this.pnlInteractiveButtons = new JPanel();
      this.btnAddInteractiveWindow = new JButton();
      this.btnRemoveInteractiveWindow = new JButton();
      JScrollPane jScrollPane1 = new JScrollPane();
      this.lstInteractiveWindows = new JList();
      JScrollPane jScrollPane3 = new JScrollPane();
      this.lstInteractiveWindowsBlacklist = new JList();
      JPanel pnlTheme = new JPanel();
      this.pnlThemeButtons = new JPanel();
      this.btnChangeFont = new JButton();
      this.btnReset = new JButton();
      this.lblPrimaryColour1 = new JLabel();
      this.txtPrimaryColour1 = new JTextField();
      this.pnlPrimaryColour1PreviewContainer = new JPanel();
      Box.Filler gluePrimaryColour1a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlPrimaryColour1Preview = new JPanel();
      Box.Filler gluePrimaryColour1b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnPrimaryColour1Change = new JButton();
      this.lblPrimaryColour2 = new JLabel();
      this.txtPrimaryColour2 = new JTextField();
      this.pnlPrimaryColour2PreviewContainer = new JPanel();
      Box.Filler gluePrimaryColour2a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlPrimaryColour2Preview = new JPanel();
      Box.Filler gluePrimaryColour2b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnPrimaryColour2Change = new JButton();
      this.lblPrimaryColour3 = new JLabel();
      this.txtPrimaryColour3 = new JTextField();
      this.pnlPrimaryColour3PreviewContainer = new JPanel();
      Box.Filler gluePrimaryColour3a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlPrimaryColour3Preview = new JPanel();
      Box.Filler gluePrimaryColour3b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnPrimaryColour3Change = new JButton();
      this.lblSecondaryColour1 = new JLabel();
      this.txtSecondaryColour1 = new JTextField();
      this.pnlSecondaryColour1PreviewContainer = new JPanel();
      Box.Filler glueSecondaryColour1a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlSecondaryColour1Preview = new JPanel();
      Box.Filler glueSecondaryColour1b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnSecondaryColour1Change = new JButton();
      this.lblSecondaryColour2 = new JLabel();
      this.txtSecondaryColour2 = new JTextField();
      this.pnlSecondaryColour2PreviewContainer = new JPanel();
      Box.Filler glueSecondaryColour2a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlSecondaryColour2Preview = new JPanel();
      Box.Filler glueSecondaryColour2b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnSecondaryColour2Change = new JButton();
      this.lblSecondaryColour3 = new JLabel();
      this.txtSecondaryColour3 = new JTextField();
      this.pnlSecondaryColour3PreviewContainer = new JPanel();
      Box.Filler glueSecondaryColour3a = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlSecondaryColour3Preview = new JPanel();
      Box.Filler glueSecondaryColour3b = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnSecondaryColour3Change = new JButton();
      this.lblMenuOpacity = new JLabel();
      this.sldMenuOpacity = new JSlider();
      this.lblBlackColour = new JLabel();
      this.txtBlackColour = new JTextField();
      this.pnlBlackColourPreviewContainer = new JPanel();
      Box.Filler glueBlackColoura = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlBlackColourPreview = new JPanel();
      Box.Filler glueBlackColourb = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnBlackColourChange = new JButton();
      this.lblWhiteColour = new JLabel();
      this.txtWhiteColour = new JTextField();
      this.pnlWhiteColourPreviewContainer = new JPanel();
      Box.Filler glueWhiteColoura = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlWhiteColourPreview = new JPanel();
      Box.Filler glueWhiteColourb = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      this.btnWhiteColourChange = new JButton();
      JPanel pnlWindowMode = new JPanel();
      this.chkWindowModeEnabled = new JCheckBox();
      this.lblDimensions = new JLabel();
      JLabel lblDimensionsX = new JLabel();
      this.lblBackground = new JLabel();
      this.txtBackgroundColour = new JTextField();
      this.btnBackgroundColourChange = new JButton();
      this.spnWindowWidth = new JSpinner();
      this.spnWindowHeight = new JSpinner();
      JLabel lblBackgroundColour = new JLabel();
      this.pnlBackgroundPreviewContainer = new JPanel();
      Box.Filler glueBackground = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlBackgroundPreview = new JPanel();
      Box.Filler glueBackground2 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
      JLabel lblBackgroundImageCaption = new JLabel();
      this.pnlBackgroundImage = new JPanel();
      this.lblBackgroundImage = new JLabel();
      this.btnBackgroundImageChange = new JButton();
      this.btnBackgroundImageRemove = new JButton();
      this.cmbBackgroundImageMode = new JComboBox();
      JPanel pnlAbout = new JPanel();
      Box.Filler glue1 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.lblIcon = new JLabel();
      Box.Filler rigid1 = new Box.Filler(new Dimension(0, 15), new Dimension(0, 15), new Dimension(0, 15));
      this.lblShimejiEE = new JLabel();
      Box.Filler rigid2 = new Box.Filler(new Dimension(0, 10), new Dimension(0, 5), new Dimension(0, 10));
      JLabel lblVersion = new JLabel();
      Box.Filler rigid3 = new Box.Filler(new Dimension(0, 15), new Dimension(0, 15), new Dimension(0, 15));
      this.lblDevelopedBy = new JLabel();
      JLabel lblKilkakon = new JLabel();
      Box.Filler rigid4 = new Box.Filler(new Dimension(0, 30), new Dimension(0, 30), new Dimension(0, 30));
      this.pnlAboutButtons = new JPanel();
      this.btnWebsite = new JButton();
      this.btnDiscord = new JButton();
      this.btnPatreon = new JButton();
      Box.Filler glue2 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 32767));
      this.pnlFooter = new JPanel();
      this.btnDone = new JButton();
      this.btnCancel = new JButton();
      this.setDefaultCloseOperation(2);
      this.chkAlwaysShowShimejiChooser.setText("Always Show Character Chooser");
      this.chkAlwaysShowShimejiChooser.addItemListener((evt) -> this.chkAlwaysShowShimejiChooserItemStateChanged(evt));
      this.lblScaling.setText("Scaling");
      this.sldScaling.setMajorTickSpacing(10);
      this.sldScaling.setMaximum(40);
      this.sldScaling.setMinorTickSpacing(5);
      this.sldScaling.setPaintLabels(true);
      this.sldScaling.setPaintTicks(true);
      this.sldScaling.setSnapToTicks(true);
      this.sldScaling.setValue(10);
      this.sldScaling.setPreferredSize(new Dimension(300, 45));
      this.sldScaling.addChangeListener((evt) -> this.sldScalingStateChanged(evt));
      this.spnScaling.setModel(new SpinnerNumberModel((double)1.0F, (double)0.5F, (double)4.0F, 0.1));
      this.spnScaling.setPreferredSize(new Dimension(120, 50));
      this.spnScaling.addChangeListener((evt) -> this.spnScalingStateChanged(evt));
      this.lblFilter.setText("Filter");
      this.radFilterNearest.setText("Nearest");
      this.radFilterNearest.addItemListener((evt) -> this.radFilterItemStateChanged(evt));
      this.radFilterBicubic.setText("Bicubic");
      this.radFilterBicubic.addItemListener((evt) -> this.radFilterItemStateChanged(evt));
      this.radFilterHqx.setText("hqx");
      this.radFilterHqx.addItemListener((evt) -> this.radFilterItemStateChanged(evt));
      this.btnFilterHelp.setText("?");
      this.btnFilterHelp.setFont(this.lblFilter.getFont().deriveFont(1, (float)this.lblFilter.getFont().getSize()));
      this.btnFilterHelp.setPreferredSize(new Dimension(32, 32));
      this.btnFilterHelp.setMinimumSize(new Dimension(32, 32));
      this.btnFilterHelp.setMaximumSize(new Dimension(32, 32));
      this.btnFilterHelp.addActionListener((evt) -> this.btnFilterHelpActionPerformed(evt));
      this.sldOpacity.setMajorTickSpacing(10);
      this.sldOpacity.setMinorTickSpacing(5);
      this.sldOpacity.setPaintLabels(true);
      this.sldOpacity.setPaintTicks(true);
      this.sldOpacity.setSnapToTicks(true);
      this.sldOpacity.setValue(10);
      this.sldOpacity.setPreferredSize(new Dimension(300, 45));
      this.sldOpacity.addChangeListener((evt) -> this.sldOpacityStateChanged(evt));
      this.lblOpacity.setText("Opacity");
      this.chkAlwaysShowInformationScreen.setText("Always Show Information Screen");
      this.chkAlwaysShowInformationScreen.addItemListener((evt) -> this.chkAlwaysShowInformationScreenItemStateChanged(evt));
      GroupLayout pnlGeneralLayout = new GroupLayout(pnlGeneral);
      pnlGeneral.setLayout(pnlGeneralLayout);
      pnlGeneralLayout.setHorizontalGroup(pnlGeneralLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlGeneralLayout.createSequentialGroup().addContainerGap().addGroup(pnlGeneralLayout.createParallelGroup(Alignment.LEADING).addComponent(this.chkAlwaysShowShimejiChooser).addGroup(pnlGeneralLayout.createSequentialGroup().addComponent(this.lblFilter).addGap(8, 8, 8).addComponent(this.btnFilterHelp, -2, -1, -2)).addComponent(this.lblScaling).addGroup(pnlGeneralLayout.createSequentialGroup().addGap(10, 10, 10).addGroup(pnlGeneralLayout.createParallelGroup(Alignment.TRAILING).addComponent(this.sldOpacity, -2, -1, -2).addGroup(pnlGeneralLayout.createParallelGroup(Alignment.LEADING).addComponent(this.radFilterNearest).addGroup(pnlGeneralLayout.createSequentialGroup().addComponent(this.sldScaling, -2, -1, -2).addGap(10, 10, 10).addComponent(this.spnScaling, -2, -1, -2)).addComponent(this.radFilterBicubic).addComponent(this.radFilterHqx)))).addComponent(this.lblOpacity).addComponent(this.chkAlwaysShowInformationScreen)).addContainerGap(80, 32767)));
      pnlGeneralLayout.setVerticalGroup(pnlGeneralLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlGeneralLayout.createSequentialGroup().addContainerGap().addComponent(this.chkAlwaysShowShimejiChooser).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.chkAlwaysShowInformationScreen).addGap(18, 18, 18).addComponent(this.lblOpacity).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.sldOpacity, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.lblScaling).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlGeneralLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.sldScaling, -2, -1, -2).addComponent(this.spnScaling, -2, -1, -2)).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(pnlGeneralLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblFilter).addComponent(this.btnFilterHelp, -2, -1, -2)).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.radFilterNearest).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.radFilterBicubic).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.radFilterHqx).addContainerGap(75, 32767)));
      this.pnlTabs.addTab("General", pnlGeneral);
      this.pnlInteractiveButtons.setPreferredSize(new Dimension(380, 36));
      this.pnlInteractiveButtons.setLayout(new FlowLayout(1, 10, 5));
      this.btnAddInteractiveWindow.setText("Add");
      this.btnAddInteractiveWindow.setMaximumSize(new Dimension(130, 26));
      this.btnAddInteractiveWindow.setMinimumSize(new Dimension(95, 23));
      this.btnAddInteractiveWindow.setName("");
      this.btnAddInteractiveWindow.setPreferredSize(new Dimension(130, 26));
      this.btnAddInteractiveWindow.addActionListener((evt) -> this.btnAddInteractiveWindowActionPerformed(evt));
      this.pnlInteractiveButtons.add(this.btnAddInteractiveWindow);
      this.btnRemoveInteractiveWindow.setText("Remove");
      this.btnRemoveInteractiveWindow.setMaximumSize(new Dimension(130, 26));
      this.btnRemoveInteractiveWindow.setMinimumSize(new Dimension(95, 23));
      this.btnRemoveInteractiveWindow.setPreferredSize(new Dimension(130, 26));
      this.btnRemoveInteractiveWindow.addActionListener((evt) -> this.btnRemoveInteractiveWindowActionPerformed(evt));
      this.pnlInteractiveButtons.add(this.btnRemoveInteractiveWindow);
      this.lstInteractiveWindows.setModel(new AbstractListModel() {
         final String[] strings = new String[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

         public int getSize() {
            return this.strings.length;
         }

         public String getElementAt(int i) {
            return this.strings[i];
         }
      });
      jScrollPane1.setViewportView(this.lstInteractiveWindows);
      this.lstInteractiveWindowsBlacklist.setModel(new AbstractListModel() {
         final String[] strings = new String[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

         public int getSize() {
            return this.strings.length;
         }

         public String getElementAt(int i) {
            return this.strings[i];
         }
      });
      jScrollPane3.setViewportView(this.lstInteractiveWindowsBlacklist);
      GroupLayout pnlWhitelistTabLayout = new GroupLayout(this.pnlWhitelistTab);
      this.pnlWhitelistTab.setLayout(pnlWhitelistTabLayout);
      pnlWhitelistTabLayout.setHorizontalGroup(pnlWhitelistTabLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWhitelistTabLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, -1, 301, 32767).addContainerGap()));
      pnlWhitelistTabLayout.setVerticalGroup(pnlWhitelistTabLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWhitelistTabLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, -1, 237, 32767).addContainerGap()));
      this.pnlInteractiveTabs.addTab("Whitelist", this.pnlWhitelistTab);
      GroupLayout pnlBlacklistTabLayout = new GroupLayout(this.pnlBlacklistTab);
      this.pnlBlacklistTab.setLayout(pnlBlacklistTabLayout);
      pnlBlacklistTabLayout.setHorizontalGroup(pnlBlacklistTabLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlBlacklistTabLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane3, -1, 301, 32767).addContainerGap()));
      pnlBlacklistTabLayout.setVerticalGroup(pnlBlacklistTabLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlBlacklistTabLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane3, -1, 237, 32767).addContainerGap()));
      this.pnlInteractiveTabs.addTab("Blacklist", this.pnlBlacklistTab);
      GroupLayout pnlInteractiveWindowsLayout = new GroupLayout(pnlInteractiveWindows);
      pnlInteractiveWindows.setLayout(pnlInteractiveWindowsLayout);
      pnlInteractiveWindowsLayout.setHorizontalGroup(pnlInteractiveWindowsLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, pnlInteractiveWindowsLayout.createSequentialGroup().addContainerGap().addGroup(pnlInteractiveWindowsLayout.createParallelGroup(Alignment.TRAILING).addComponent(this.pnlInteractiveTabs).addComponent(this.pnlInteractiveButtons, -1, -1, 32767)).addContainerGap()));
      pnlInteractiveWindowsLayout.setVerticalGroup(pnlInteractiveWindowsLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, pnlInteractiveWindowsLayout.createSequentialGroup().addContainerGap().addComponent(this.pnlInteractiveTabs).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlInteractiveButtons, -2, -1, -2).addContainerGap()));
      this.pnlTabs.addTab("InteractiveWindows", pnlInteractiveWindows);
      this.pnlThemeButtons.setPreferredSize(new Dimension(380, 36));
      this.pnlThemeButtons.setLayout(new FlowLayout(1, 10, 5));
      this.btnChangeFont.setText("Change Font");
      this.btnChangeFont.setMaximumSize(new Dimension(130, 26));
      this.btnChangeFont.setName("");
      this.btnChangeFont.setPreferredSize(new Dimension(130, 26));
      this.btnChangeFont.addActionListener((evt) -> this.btnChangeFontActionPerformed(evt));
      this.pnlThemeButtons.add(this.btnChangeFont);
      this.btnReset.setText("Reset");
      this.btnReset.setMaximumSize(new Dimension(130, 26));
      this.btnReset.setMinimumSize(new Dimension(95, 23));
      this.btnReset.setPreferredSize(new Dimension(130, 26));
      this.btnReset.addActionListener((evt) -> this.btnResetActionPerformed(evt));
      this.pnlThemeButtons.add(this.btnReset);
      this.lblPrimaryColour1.setText("Primary 1");
      this.txtPrimaryColour1.setEditable(false);
      this.txtPrimaryColour1.setText("#00FF00");
      this.txtPrimaryColour1.setPreferredSize(new Dimension(70, 24));
      this.pnlPrimaryColour1PreviewContainer.setLayout(new BoxLayout(this.pnlPrimaryColour1PreviewContainer, 1));
      this.pnlPrimaryColour1PreviewContainer.add(gluePrimaryColour1a);
      this.pnlPrimaryColour1Preview.setBorder(new SoftBevelBorder(1));
      this.pnlPrimaryColour1Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlPrimaryColour1PreviewLayout = new GroupLayout(this.pnlPrimaryColour1Preview);
      this.pnlPrimaryColour1Preview.setLayout(pnlPrimaryColour1PreviewLayout);
      pnlPrimaryColour1PreviewLayout.setHorizontalGroup(pnlPrimaryColour1PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlPrimaryColour1PreviewLayout.setVerticalGroup(pnlPrimaryColour1PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlPrimaryColour1PreviewContainer.add(this.pnlPrimaryColour1Preview);
      this.pnlPrimaryColour1PreviewContainer.add(gluePrimaryColour1b);
      this.btnPrimaryColour1Change.setText("Change");
      this.btnPrimaryColour1Change.addActionListener((evt) -> this.btnPrimaryColour1ChangeActionPerformed(evt));
      this.lblPrimaryColour2.setText("Primary 2");
      this.txtPrimaryColour2.setEditable(false);
      this.txtPrimaryColour2.setText("#00FF00");
      this.txtPrimaryColour2.setPreferredSize(new Dimension(70, 24));
      this.pnlPrimaryColour2PreviewContainer.setLayout(new BoxLayout(this.pnlPrimaryColour2PreviewContainer, 1));
      this.pnlPrimaryColour2PreviewContainer.add(gluePrimaryColour2a);
      this.pnlPrimaryColour2Preview.setBorder(new SoftBevelBorder(1));
      this.pnlPrimaryColour2Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlPrimaryColour2PreviewLayout = new GroupLayout(this.pnlPrimaryColour2Preview);
      this.pnlPrimaryColour2Preview.setLayout(pnlPrimaryColour2PreviewLayout);
      pnlPrimaryColour2PreviewLayout.setHorizontalGroup(pnlPrimaryColour2PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlPrimaryColour2PreviewLayout.setVerticalGroup(pnlPrimaryColour2PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlPrimaryColour2PreviewContainer.add(this.pnlPrimaryColour2Preview);
      this.pnlPrimaryColour2PreviewContainer.add(gluePrimaryColour2b);
      this.btnPrimaryColour2Change.setText("Change");
      this.btnPrimaryColour2Change.addActionListener((evt) -> this.btnPrimaryColour2ChangeActionPerformed(evt));
      this.lblPrimaryColour3.setText("Primary 3");
      this.txtPrimaryColour3.setEditable(false);
      this.txtPrimaryColour3.setText("#00FF00");
      this.txtPrimaryColour3.setPreferredSize(new Dimension(70, 24));
      this.pnlPrimaryColour3PreviewContainer.setLayout(new BoxLayout(this.pnlPrimaryColour3PreviewContainer, 1));
      this.pnlPrimaryColour3PreviewContainer.add(gluePrimaryColour3a);
      this.pnlPrimaryColour3Preview.setBorder(new SoftBevelBorder(1));
      this.pnlPrimaryColour3Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlPrimaryColour3PreviewLayout = new GroupLayout(this.pnlPrimaryColour3Preview);
      this.pnlPrimaryColour3Preview.setLayout(pnlPrimaryColour3PreviewLayout);
      pnlPrimaryColour3PreviewLayout.setHorizontalGroup(pnlPrimaryColour3PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlPrimaryColour3PreviewLayout.setVerticalGroup(pnlPrimaryColour3PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlPrimaryColour3PreviewContainer.add(this.pnlPrimaryColour3Preview);
      this.pnlPrimaryColour3PreviewContainer.add(gluePrimaryColour3b);
      this.btnPrimaryColour3Change.setText("Change");
      this.btnPrimaryColour3Change.addActionListener((evt) -> this.btnPrimaryColour3ChangeActionPerformed(evt));
      this.lblSecondaryColour1.setText("Secondary 1");
      this.txtSecondaryColour1.setEditable(false);
      this.txtSecondaryColour1.setText("#00FF00");
      this.txtSecondaryColour1.setPreferredSize(new Dimension(70, 24));
      this.pnlSecondaryColour1PreviewContainer.setLayout(new BoxLayout(this.pnlSecondaryColour1PreviewContainer, 1));
      this.pnlSecondaryColour1PreviewContainer.add(glueSecondaryColour1a);
      this.pnlSecondaryColour1Preview.setBorder(new SoftBevelBorder(1));
      this.pnlSecondaryColour1Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlSecondaryColour1PreviewLayout = new GroupLayout(this.pnlSecondaryColour1Preview);
      this.pnlSecondaryColour1Preview.setLayout(pnlSecondaryColour1PreviewLayout);
      pnlSecondaryColour1PreviewLayout.setHorizontalGroup(pnlSecondaryColour1PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlSecondaryColour1PreviewLayout.setVerticalGroup(pnlSecondaryColour1PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlSecondaryColour1PreviewContainer.add(this.pnlSecondaryColour1Preview);
      this.pnlSecondaryColour1PreviewContainer.add(glueSecondaryColour1b);
      this.btnSecondaryColour1Change.setText("Change");
      this.btnSecondaryColour1Change.addActionListener((evt) -> this.btnSecondaryColour1ChangeActionPerformed(evt));
      this.lblSecondaryColour2.setText("Secondary 2");
      this.txtSecondaryColour2.setEditable(false);
      this.txtSecondaryColour2.setText("#00FF00");
      this.txtSecondaryColour2.setPreferredSize(new Dimension(70, 24));
      this.pnlSecondaryColour2PreviewContainer.setLayout(new BoxLayout(this.pnlSecondaryColour2PreviewContainer, 1));
      this.pnlSecondaryColour2PreviewContainer.add(glueSecondaryColour2a);
      this.pnlSecondaryColour2Preview.setBorder(new SoftBevelBorder(1));
      this.pnlSecondaryColour2Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlSecondaryColour2PreviewLayout = new GroupLayout(this.pnlSecondaryColour2Preview);
      this.pnlSecondaryColour2Preview.setLayout(pnlSecondaryColour2PreviewLayout);
      pnlSecondaryColour2PreviewLayout.setHorizontalGroup(pnlSecondaryColour2PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlSecondaryColour2PreviewLayout.setVerticalGroup(pnlSecondaryColour2PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlSecondaryColour2PreviewContainer.add(this.pnlSecondaryColour2Preview);
      this.pnlSecondaryColour2PreviewContainer.add(glueSecondaryColour2b);
      this.btnSecondaryColour2Change.setText("Change");
      this.btnSecondaryColour2Change.addActionListener((evt) -> this.btnSecondaryColour2ChangeActionPerformed(evt));
      this.lblSecondaryColour3.setText("Secondary 3");
      this.txtSecondaryColour3.setEditable(false);
      this.txtSecondaryColour3.setText("#00FF00");
      this.txtSecondaryColour3.setPreferredSize(new Dimension(70, 24));
      this.pnlSecondaryColour3PreviewContainer.setLayout(new BoxLayout(this.pnlSecondaryColour3PreviewContainer, 1));
      this.pnlSecondaryColour3PreviewContainer.add(glueSecondaryColour3a);
      this.pnlSecondaryColour3Preview.setBorder(new SoftBevelBorder(1));
      this.pnlSecondaryColour3Preview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlSecondaryColour3PreviewLayout = new GroupLayout(this.pnlSecondaryColour3Preview);
      this.pnlSecondaryColour3Preview.setLayout(pnlSecondaryColour3PreviewLayout);
      pnlSecondaryColour3PreviewLayout.setHorizontalGroup(pnlSecondaryColour3PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlSecondaryColour3PreviewLayout.setVerticalGroup(pnlSecondaryColour3PreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlSecondaryColour3PreviewContainer.add(this.pnlSecondaryColour3Preview);
      this.pnlSecondaryColour3PreviewContainer.add(glueSecondaryColour3b);
      this.btnSecondaryColour3Change.setText("Change");
      this.btnSecondaryColour3Change.addActionListener((evt) -> this.btnSecondaryColour3ChangeActionPerformed(evt));
      this.lblMenuOpacity.setText("Menu Opacity");
      this.sldMenuOpacity.setMajorTickSpacing(10);
      this.sldMenuOpacity.setMinorTickSpacing(5);
      this.sldMenuOpacity.setPaintLabels(true);
      this.sldMenuOpacity.setPaintTicks(true);
      this.sldMenuOpacity.setSnapToTicks(true);
      this.sldMenuOpacity.setValue(10);
      this.sldMenuOpacity.setPreferredSize(new Dimension(300, 45));
      this.sldMenuOpacity.addChangeListener((evt) -> this.sldMenuOpacityStateChanged(evt));
      this.lblBlackColour.setText("Text");
      this.txtBlackColour.setEditable(false);
      this.txtBlackColour.setText("#00FF00");
      this.txtBlackColour.setPreferredSize(new Dimension(70, 24));
      this.pnlBlackColourPreviewContainer.setLayout(new BoxLayout(this.pnlBlackColourPreviewContainer, 1));
      this.pnlBlackColourPreviewContainer.add(glueBlackColoura);
      this.pnlBlackColourPreview.setBorder(new SoftBevelBorder(1));
      this.pnlBlackColourPreview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlBlackColourPreviewLayout = new GroupLayout(this.pnlBlackColourPreview);
      this.pnlBlackColourPreview.setLayout(pnlBlackColourPreviewLayout);
      pnlBlackColourPreviewLayout.setHorizontalGroup(pnlBlackColourPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlBlackColourPreviewLayout.setVerticalGroup(pnlBlackColourPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlBlackColourPreviewContainer.add(this.pnlBlackColourPreview);
      this.pnlBlackColourPreviewContainer.add(glueBlackColourb);
      this.btnBlackColourChange.setText("Change");
      this.btnBlackColourChange.addActionListener((evt) -> this.btnBlackColourChangeActionPerformed(evt));
      this.lblWhiteColour.setText("Background");
      this.lblWhiteColour.setToolTipText("");
      this.txtWhiteColour.setEditable(false);
      this.txtWhiteColour.setText("#00FF00");
      this.txtWhiteColour.setPreferredSize(new Dimension(70, 24));
      this.pnlWhiteColourPreviewContainer.setLayout(new BoxLayout(this.pnlWhiteColourPreviewContainer, 1));
      this.pnlWhiteColourPreviewContainer.add(glueWhiteColoura);
      this.pnlWhiteColourPreview.setBorder(new SoftBevelBorder(1));
      this.pnlWhiteColourPreview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlWhiteColourPreviewLayout = new GroupLayout(this.pnlWhiteColourPreview);
      this.pnlWhiteColourPreview.setLayout(pnlWhiteColourPreviewLayout);
      pnlWhiteColourPreviewLayout.setHorizontalGroup(pnlWhiteColourPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlWhiteColourPreviewLayout.setVerticalGroup(pnlWhiteColourPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlWhiteColourPreviewContainer.add(this.pnlWhiteColourPreview);
      this.pnlWhiteColourPreviewContainer.add(glueWhiteColourb);
      this.btnWhiteColourChange.setText("Change");
      this.btnWhiteColourChange.addActionListener((evt) -> this.btnWhiteColourChangeActionPerformed(evt));
      GroupLayout pnlThemeLayout = new GroupLayout(pnlTheme);
      pnlTheme.setLayout(pnlThemeLayout);
      pnlThemeLayout.setHorizontalGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createSequentialGroup().addContainerGap().addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createSequentialGroup().addGroup(pnlThemeLayout.createParallelGroup(Alignment.TRAILING).addComponent(this.pnlThemeButtons, -1, -1, 32767).addGroup(pnlThemeLayout.createSequentialGroup().addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addComponent(this.lblPrimaryColour1).addComponent(this.lblPrimaryColour2).addComponent(this.lblPrimaryColour3).addComponent(this.lblSecondaryColour1).addComponent(this.lblSecondaryColour2).addComponent(this.lblSecondaryColour3).addComponent(this.lblBlackColour).addComponent(this.lblWhiteColour)).addGap(18, 18, 18).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtPrimaryColour1, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlPrimaryColour1PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnPrimaryColour1Change)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtPrimaryColour2, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlPrimaryColour2PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnPrimaryColour2Change)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtPrimaryColour3, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlPrimaryColour3PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnPrimaryColour3Change)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtSecondaryColour1, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlSecondaryColour1PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnSecondaryColour1Change)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtSecondaryColour2, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlSecondaryColour2PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnSecondaryColour2Change)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtSecondaryColour3, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlSecondaryColour3PreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnSecondaryColour3Change)).addGroup(pnlThemeLayout.createSequentialGroup().addGroup(pnlThemeLayout.createParallelGroup(Alignment.TRAILING, false).addGroup(Alignment.LEADING, pnlThemeLayout.createSequentialGroup().addComponent(this.txtWhiteColour, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlWhiteColourPreviewContainer, -1, -1, -2)).addGroup(pnlThemeLayout.createSequentialGroup().addComponent(this.txtBlackColour, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlBlackColourPreviewContainer, -2, -1, -2))).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addComponent(this.btnBlackColourChange).addComponent(this.btnWhiteColourChange, Alignment.TRAILING)))).addGap(0, 0, 32767))).addContainerGap()).addGroup(pnlThemeLayout.createSequentialGroup().addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addComponent(this.lblMenuOpacity).addGroup(pnlThemeLayout.createSequentialGroup().addGap(10, 10, 10).addComponent(this.sldMenuOpacity, -2, -1, -2))).addGap(0, 0, 32767)))));
      pnlThemeLayout.setVerticalGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, pnlThemeLayout.createSequentialGroup().addContainerGap().addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblPrimaryColour1).addComponent(this.txtPrimaryColour1, -2, -1, -2).addComponent(this.btnPrimaryColour1Change)).addComponent(this.pnlPrimaryColour1PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblPrimaryColour2).addComponent(this.txtPrimaryColour2, -2, -1, -2).addComponent(this.btnPrimaryColour2Change)).addComponent(this.pnlPrimaryColour2PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblPrimaryColour3).addComponent(this.txtPrimaryColour3, -2, -1, -2).addComponent(this.btnPrimaryColour3Change)).addComponent(this.pnlPrimaryColour3PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblSecondaryColour1).addComponent(this.txtSecondaryColour1, -2, -1, -2).addComponent(this.btnSecondaryColour1Change)).addComponent(this.pnlSecondaryColour1PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblSecondaryColour2).addComponent(this.txtSecondaryColour2, -2, -1, -2).addComponent(this.btnSecondaryColour2Change)).addComponent(this.pnlSecondaryColour2PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblSecondaryColour3).addComponent(this.txtSecondaryColour3, -2, -1, -2).addComponent(this.btnSecondaryColour3Change)).addComponent(this.pnlSecondaryColour3PreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblBlackColour).addComponent(this.txtBlackColour, -2, -1, -2).addComponent(this.btnBlackColourChange)).addComponent(this.pnlBlackColourPreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlThemeLayout.createParallelGroup(Alignment.LEADING, false).addGroup(pnlThemeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblWhiteColour).addComponent(this.txtWhiteColour, -2, -1, -2).addComponent(this.btnWhiteColourChange)).addComponent(this.pnlWhiteColourPreviewContainer, -2, 24, -2)).addGap(18, 18, 18).addComponent(this.lblMenuOpacity).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.sldMenuOpacity, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED, -1, 32767).addComponent(this.pnlThemeButtons, -2, -1, -2).addContainerGap()));
      this.pnlTabs.addTab("Theme", pnlTheme);
      this.chkWindowModeEnabled.setText("Enable Windowed Mode");
      this.chkWindowModeEnabled.addItemListener((evt) -> this.chkWindowModeEnabledItemStateChanged(evt));
      this.lblDimensions.setText("Dimensions");
      lblDimensionsX.setText("x");
      this.lblBackground.setText("Background");
      this.txtBackgroundColour.setEditable(false);
      this.txtBackgroundColour.setText("#00FF00");
      this.txtBackgroundColour.setPreferredSize(new Dimension(70, 24));
      this.btnBackgroundColourChange.setText("Change");
      this.btnBackgroundColourChange.addActionListener((evt) -> this.btnBackgroundColourChangeActionPerformed(evt));
      this.spnWindowWidth.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
      this.spnWindowWidth.setPreferredSize(new Dimension(60, 24));
      this.spnWindowWidth.addChangeListener((evt) -> this.spnWindowWidthStateChanged(evt));
      this.spnWindowHeight.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
      this.spnWindowHeight.setMinimumSize(new Dimension(30, 20));
      this.spnWindowHeight.setPreferredSize(new Dimension(60, 24));
      this.spnWindowHeight.addChangeListener((evt) -> this.spnWindowHeightStateChanged(evt));
      lblBackgroundColour.setText("Colour");
      this.pnlBackgroundPreviewContainer.setLayout(new BoxLayout(this.pnlBackgroundPreviewContainer, 1));
      this.pnlBackgroundPreviewContainer.add(glueBackground);
      this.pnlBackgroundPreview.setBorder(new SoftBevelBorder(1));
      this.pnlBackgroundPreview.setPreferredSize(new Dimension(20, 20));
      GroupLayout pnlBackgroundPreviewLayout = new GroupLayout(this.pnlBackgroundPreview);
      this.pnlBackgroundPreview.setLayout(pnlBackgroundPreviewLayout);
      pnlBackgroundPreviewLayout.setHorizontalGroup(pnlBackgroundPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      pnlBackgroundPreviewLayout.setVerticalGroup(pnlBackgroundPreviewLayout.createParallelGroup(Alignment.LEADING).addGap(0, 0, 32767));
      this.pnlBackgroundPreviewContainer.add(this.pnlBackgroundPreview);
      this.pnlBackgroundPreviewContainer.add(glueBackground2);
      lblBackgroundImageCaption.setText("Image");
      this.pnlBackgroundImage.setBorder(new SoftBevelBorder(1));
      this.pnlBackgroundImage.setPreferredSize(new Dimension(96, 96));
      this.pnlBackgroundImage.setLayout(new BorderLayout());
      this.lblBackgroundImage.setHorizontalAlignment(0);
      this.pnlBackgroundImage.add(this.lblBackgroundImage, "Center");
      this.btnBackgroundImageChange.setText("Change");
      this.btnBackgroundImageChange.addActionListener((evt) -> this.btnBackgroundImageChangeActionPerformed(evt));
      this.btnBackgroundImageRemove.setText("Remove");
      this.btnBackgroundImageRemove.addActionListener((evt) -> this.btnBackgroundImageRemoveActionPerformed(evt));
      this.cmbBackgroundImageMode.setModel(new DefaultComboBoxModel());
      this.cmbBackgroundImageMode.addActionListener((evt) -> this.cmbBackgroundImageModeActionPerformed(evt));
      GroupLayout pnlWindowModeLayout = new GroupLayout(pnlWindowMode);
      pnlWindowMode.setLayout(pnlWindowModeLayout);
      pnlWindowModeLayout.setHorizontalGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWindowModeLayout.createSequentialGroup().addContainerGap().addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addComponent(this.chkWindowModeEnabled).addGroup(pnlWindowModeLayout.createSequentialGroup().addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addComponent(this.lblDimensions).addComponent(this.lblBackground).addGroup(pnlWindowModeLayout.createSequentialGroup().addGap(10, 10, 10).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addComponent(lblBackgroundImageCaption).addComponent(lblBackgroundColour)))).addGap(18, 18, 18).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING, false).addGroup(pnlWindowModeLayout.createSequentialGroup().addComponent(this.spnWindowWidth, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(lblDimensionsX).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.spnWindowHeight, -2, -1, -2)).addGroup(pnlWindowModeLayout.createSequentialGroup().addComponent(this.txtBackgroundColour, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlBackgroundPreviewContainer, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(this.btnBackgroundColourChange, -1, -1, 32767)).addGroup(pnlWindowModeLayout.createSequentialGroup().addComponent(this.pnlBackgroundImage, -2, -1, -2).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING, false).addComponent(this.btnBackgroundImageRemove, -1, -1, 32767).addComponent(this.btnBackgroundImageChange, -1, -1, 32767).addComponent(this.cmbBackgroundImageMode, 0, -1, 32767)))))).addContainerGap(139, 32767)));
      pnlWindowModeLayout.setVerticalGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWindowModeLayout.createSequentialGroup().addContainerGap().addComponent(this.chkWindowModeEnabled).addGap(18, 18, 18).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.BASELINE).addComponent(this.lblDimensions).addComponent(lblDimensionsX).addComponent(this.spnWindowWidth, -2, -1, -2).addComponent(this.spnWindowHeight, -2, -1, -2)).addGap(18, 18, 18).addComponent(this.lblBackground).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblBackgroundColour).addComponent(this.txtBackgroundColour, -2, -1, -2).addComponent(this.btnBackgroundColourChange)).addComponent(this.pnlBackgroundPreviewContainer, -2, 24, -2)).addPreferredGap(ComponentPlacement.RELATED).addGroup(pnlWindowModeLayout.createParallelGroup(Alignment.LEADING).addGroup(pnlWindowModeLayout.createSequentialGroup().addComponent(this.btnBackgroundImageChange).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.cmbBackgroundImageMode, -2, -1, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnBackgroundImageRemove)).addGroup(pnlWindowModeLayout.createSequentialGroup().addGap(4, 4, 4).addComponent(lblBackgroundImageCaption)).addComponent(this.pnlBackgroundImage, -2, -1, -2)).addContainerGap(150, 32767)));
      this.pnlTabs.addTab("WindowMode", pnlWindowMode);
      pnlAbout.setLayout(new BoxLayout(pnlAbout, 1));
      pnlAbout.add(glue1);
      this.lblIcon.setAlignmentX(0.5F);
      this.lblIcon.setMinimumSize(new Dimension(64, 64));
      this.lblIcon.setPreferredSize(new Dimension(64, 64));
      pnlAbout.add(this.lblIcon);
      pnlAbout.add(rigid1);
      this.lblShimejiEE.setFont(this.lblShimejiEE.getFont().deriveFont(this.lblShimejiEE.getFont().getStyle() | 1, (float)(this.lblShimejiEE.getFont().getSize() + 10)));
      this.lblShimejiEE.setText("ShinobiMate");
      this.lblShimejiEE.setAlignmentX(0.5F);
      pnlAbout.add(this.lblShimejiEE);
      pnlAbout.add(rigid2);
      lblVersion.setFont(lblVersion.getFont().deriveFont((float)lblVersion.getFont().getSize() + 4.0F));
      lblVersion.setText(com.group_finity.mascot.BuildInfo.VERSION);
      lblVersion.setAlignmentX(0.5F);
      pnlAbout.add(lblVersion);
      pnlAbout.add(rigid3);
      this.lblDevelopedBy.setText("developed by");
      this.lblDevelopedBy.setAlignmentX(0.5F);
      pnlAbout.add(this.lblDevelopedBy);
      lblKilkakon.setText("Kilkakon & Zero & Begonia");
      lblKilkakon.setAlignmentX(0.5F);
      pnlAbout.add(lblKilkakon);
      pnlAbout.add(rigid4);
      this.pnlAboutButtons.setMaximumSize(new Dimension(32767, 36));
      this.pnlAboutButtons.setLayout(new FlowLayout(1, 10, 5));
      this.btnWebsite.setText("Website");
      this.btnWebsite.setAlignmentX(0.5F);
      this.btnWebsite.setMaximumSize(new Dimension(130, 26));
      this.btnWebsite.setPreferredSize(new Dimension(100, 26));
      this.btnWebsite.addActionListener((evt) -> this.btnWebsiteActionPerformed(evt));
      this.pnlAboutButtons.add(this.btnWebsite);
      this.btnDiscord.setText("Discord");
      this.btnDiscord.setAlignmentX(0.5F);
      this.btnDiscord.setMaximumSize(new Dimension(130, 26));
      this.btnDiscord.setPreferredSize(new Dimension(100, 26));
      this.btnDiscord.addActionListener((evt) -> this.btnDiscordActionPerformed(evt));
      this.pnlAboutButtons.add(this.btnDiscord);
      this.btnPatreon.setText("Patreon");
      this.btnPatreon.setAlignmentX(0.5F);
      this.btnPatreon.setMaximumSize(new Dimension(130, 26));
      this.btnPatreon.setPreferredSize(new Dimension(100, 26));
      this.btnPatreon.addActionListener((evt) -> this.btnPatreonActionPerformed(evt));
      this.pnlAboutButtons.add(this.btnPatreon);
      pnlAbout.add(this.pnlAboutButtons);
      pnlAbout.add(glue2);
      this.pnlTabs.addTab("About", pnlAbout);
      this.pnlFooter.setPreferredSize(new Dimension(380, 36));
      this.pnlFooter.setLayout(new FlowLayout(1, 10, 5));
      this.btnDone.setText("Done");
      this.btnDone.setMaximumSize(new Dimension(130, 26));
      this.btnDone.setMinimumSize(new Dimension(95, 23));
      this.btnDone.setName("");
      this.btnDone.setPreferredSize(new Dimension(130, 26));
      this.btnDone.addActionListener((evt) -> this.btnDoneActionPerformed(evt));
      this.pnlFooter.add(this.btnDone);
      this.btnCancel.setText("Cancel");
      this.btnCancel.setMaximumSize(new Dimension(130, 26));
      this.btnCancel.setMinimumSize(new Dimension(95, 23));
      this.btnCancel.setPreferredSize(new Dimension(130, 26));
      this.btnCancel.addActionListener((evt) -> this.btnCancelActionPerformed(evt));
      this.pnlFooter.add(this.btnCancel);
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.pnlFooter, -2, 0, 32767).addComponent(this.pnlTabs)).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.pnlTabs).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlFooter, -2, -1, -2).addContainerGap()));
      this.pack();
   }

   private static Color safeColor(Properties properties,String key,String fallback){
      try{return Color.decode(properties.getProperty(key,fallback));}catch(Exception ex){return Color.decode(fallback);}
   }

   private void btnDoneActionPerformed(ActionEvent evt) {
      try {
         Properties properties = Main.getInstance().getProperties();
      com.group_finity.mascot.settings.SettingsStore.sanitize(properties);
         String interactiveWindows = this.listData.toString().replace("[", "").replace("]", "").replace(", ", "/");
         String interactiveWindowsBlacklist = this.blacklistData.toString().replace("[", "").replace("]", "").replace(", ", "/");
         String[] windowArray = properties.getProperty("WindowSize", "600x500").split("x");
         Dimension window = new Dimension(Integer.parseInt(windowArray[0]), Integer.parseInt(windowArray[1]));
         this.environmentReloadRequired = properties.getProperty("Environment", "generic").equals("virtual") != this.windowedMode || !window.equals(this.windowSize) || !Color.decode(properties.getProperty("Background", "#00FF00")).equals(this.backgroundColour) || !properties.getProperty("BackgroundMode", "centre").equals(this.backgroundMode) || !properties.getProperty("BackgroundImage", "").equalsIgnoreCase(this.backgroundImage == null ? "" : this.backgroundImage);
         this.imageReloadRequired = !properties.getProperty("Filter", "false").equalsIgnoreCase(this.filter) || Double.parseDouble(properties.getProperty("Scaling", "1.0")) != this.scaling || Double.parseDouble(properties.getProperty("Opacity", "1.0")) != this.opacity;
         this.interactiveWindowReloadRequired = !properties.getProperty("InteractiveWindows", "").equals(interactiveWindows) || !properties.getProperty("InteractiveWindowsBlacklist", "").equals(interactiveWindowsBlacklist);
            properties.setProperty("AlwaysShowShimejiChooser", this.alwaysShowShimejiChooser.toString());
            properties.setProperty("AlwaysShowInformationScreen", this.alwaysShowInformationScreen.toString());
            properties.setProperty("Opacity", Double.toString(this.opacity));
            properties.setProperty("Scaling", Double.toString(this.scaling));
            properties.setProperty("Filter", this.filter);
            properties.setProperty("InteractiveWindows", interactiveWindows);
            properties.setProperty("InteractiveWindowsBlacklist", interactiveWindowsBlacklist);
            properties.setProperty("Environment", this.windowedMode ? "virtual" : "generic");
            if (this.windowedMode) {
               properties.setProperty("WindowSize", this.windowSize.width + "x" + this.windowSize.height);
               properties.setProperty("Background", String.format("#%02X%02X%02X", this.backgroundColour.getRed(), this.backgroundColour.getGreen(), this.backgroundColour.getBlue()));
               properties.setProperty("BackgroundMode", this.backgroundMode);
               properties.setProperty("BackgroundImage", this.backgroundImage == null ? "" : this.backgroundImage);
            }

         com.group_finity.mascot.settings.SettingsStore.save(properties,com.group_finity.mascot.settings.SettingsStore.path());
            Properties themeProps = new Properties();
            themeProps.setProperty("PrimaryColour1", String.format("#%02X%02X%02X", this.primaryColour1.getRed(), this.primaryColour1.getGreen(), this.primaryColour1.getBlue()));
            themeProps.setProperty("PrimaryColour2", String.format("#%02X%02X%02X", this.primaryColour2.getRed(), this.primaryColour2.getGreen(), this.primaryColour2.getBlue()));
            themeProps.setProperty("PrimaryColour3", String.format("#%02X%02X%02X", this.primaryColour3.getRed(), this.primaryColour3.getGreen(), this.primaryColour3.getBlue()));
            themeProps.setProperty("SecondaryColour1", String.format("#%02X%02X%02X", this.secondaryColour1.getRed(), this.secondaryColour1.getGreen(), this.secondaryColour1.getBlue()));
            themeProps.setProperty("SecondaryColour2", String.format("#%02X%02X%02X", this.secondaryColour2.getRed(), this.secondaryColour2.getGreen(), this.secondaryColour2.getBlue()));
            themeProps.setProperty("SecondaryColour3", String.format("#%02X%02X%02X", this.secondaryColour3.getRed(), this.secondaryColour3.getGreen(), this.secondaryColour3.getBlue()));
            themeProps.setProperty("BlackColour", String.format("#%02X%02X%02X", this.blackColour.getRed(), this.blackColour.getGreen(), this.blackColour.getBlue()));
            themeProps.setProperty("WhiteColour", String.format("#%02X%02X%02X", this.whiteColour.getRed(), this.whiteColour.getGreen(), this.whiteColour.getBlue()));
            themeProps.setProperty("MenuOpacity", String.valueOf((int)(this.menuOpacity * (double)255.0F)));
            themeProps.setProperty("FontName", this.font.getName());
            themeProps.setProperty("FontStyle", String.valueOf(this.font.getStyle()));
            themeProps.setProperty("FontSize", String.valueOf(this.font.getSize()));
         com.group_finity.mascot.settings.SettingsStore.save(themeProps,com.group_finity.mascot.AppPaths.userData().resolve("theme.properties"));
      } catch (Exception error) {
         java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.WARNING,"Advanced settings save failed",error);
         javax.swing.JOptionPane.showMessageDialog(this,"تعذر حفظ الإعدادات.\nCould not save settings.","ShinobiMate",javax.swing.JOptionPane.ERROR_MESSAGE);return;
      }

      this.dispose();
   }

   private void btnCancelActionPerformed(ActionEvent evt) {
      this.themeProperties = this.oldThemeProperties;
      this.refreshTheme();
      this.dispose();
   }

   private void btnAddInteractiveWindowActionPerformed(ActionEvent evt) {
      String inputValue = JOptionPane.showInputDialog(this.rootPane, Main.getInstance().getLanguageBundle().getString("InteractiveWindowHintMessage"), Main.getInstance().getLanguageBundle().getString(this.pnlInteractiveTabs.getSelectedIndex() == 0 ? "AddInteractiveWindow" : "BlacklistInteractiveWindow"), 3);
      if (inputValue != null && !inputValue.trim().isEmpty() && !inputValue.contains("/")) {
         if (this.pnlInteractiveTabs.getSelectedIndex() == 0) {
            this.listData.add(inputValue.trim());
            this.lstInteractiveWindows.setListData((String[])this.listData.toArray(new String[0]));
         } else {
            this.blacklistData.add(inputValue.trim());
            this.lstInteractiveWindowsBlacklist.setListData((String[])this.blacklistData.toArray(new String[0]));
         }
      }

   }

   private void btnRemoveInteractiveWindowActionPerformed(ActionEvent evt) {
      if (this.pnlInteractiveTabs.getSelectedIndex() == 0) {
         if (this.lstInteractiveWindows.getSelectedIndex() != -1) {
            this.listData.remove(this.lstInteractiveWindows.getSelectedIndex());
            this.lstInteractiveWindows.setListData((String[])this.listData.toArray(new String[0]));
         }
      } else if (this.lstInteractiveWindowsBlacklist.getSelectedIndex() != -1) {
         this.blacklistData.remove(this.lstInteractiveWindowsBlacklist.getSelectedIndex());
         this.lstInteractiveWindowsBlacklist.setListData((String[])this.blacklistData.toArray(new String[0]));
      }

   }

   private void chkAlwaysShowShimejiChooserItemStateChanged(ItemEvent evt) {
      this.alwaysShowShimejiChooser = evt.getStateChange() == 1;
   }

   private void radFilterItemStateChanged(ItemEvent evt) {
      if (evt.getStateChange() == 1) {
         Object source = evt.getItemSelectable();
         if (source == this.radFilterNearest) {
            this.filter = "nearest";
         } else if (source == this.radFilterHqx) {
            LicenseManager licenseManager = LicenseManager.getInstance();
            LicenseLevel currentLevel = licenseManager.getCurrentLicenseLevel();
            if (currentLevel == LicenseLevel.NO_KEY) {
               JOptionPane.showMessageDialog(this, Main.getInstance().getLanguageBundle().getString("PremiumFeatureRequired"), Main.getInstance().getLanguageBundle().getString("LicenseRequired"), 2);
               this.radFilterNearest.setSelected(true);
               this.filter = "nearest";
            } else {
               this.filter = "hqx";
            }
         } else {
            this.filter = "bicubic";
         }
      }

   }

   private void sldScalingStateChanged(ChangeEvent evt) {
      int currentValue = this.sldScaling.getValue();
      double newScaling = (double)currentValue / (double)10.0F;
      if (this.sldScaling.getValueIsAdjusting()) {
         int[] snapPoints = new int[]{5, 10, 15, 20, 25, 30, 35, 40};

         for(int snapPoint : snapPoints) {
            if (Math.abs(currentValue - snapPoint) <= 1) {
               if (currentValue != snapPoint) {
                  SwingUtilities.invokeLater(() -> this.sldScaling.setValue(snapPoint));
                  return;
               }
               break;
            }
         }

         this.sldScaling.setToolTipText(String.format("%.1fx", newScaling));
      }

      if (!this.sldScaling.getValueIsAdjusting()) {
         if (this.sldScaling.getValue() == 0) {
            this.sldScaling.setValue(5);
         } else {
            this.scaling = (double)this.sldScaling.getValue() / (double)10.0F;
            double spinnerValue = (Double)this.spnScaling.getValue();
            if (Math.abs(spinnerValue - this.scaling) > 0.01) {
               this.spnScaling.setValue(this.scaling);
            }

            if (this.scaling != (double)2.0F && this.scaling != (double)3.0F && this.scaling != (double)4.0F) {
               this.radFilterHqx.setEnabled(false);
               if (this.filter.equals("hqx")) {
                  this.radFilterNearest.setSelected(true);
                  this.filter = "nearest";
               }
            } else {
               LicenseManager licenseManager = LicenseManager.getInstance();
               LicenseLevel currentLevel = licenseManager.getCurrentLicenseLevel();
               if (currentLevel == LicenseLevel.NO_KEY) {
                  this.radFilterHqx.setEnabled(false);
                  if (this.filter.equals("hqx")) {
                     this.radFilterNearest.setSelected(true);
                     this.filter = "nearest";
                  }
               } else {
                  this.radFilterHqx.setEnabled(true);
               }
            }

            this.imageReloadRequired = true;
         }
      }

   }

   private void spnScalingStateChanged(ChangeEvent evt) {
      double value = (Double)this.spnScaling.getValue();
      int sliderValue = (int)(value * (double)10.0F);
      if (this.sldScaling.getValue() != sliderValue) {
         this.sldScaling.setValue(sliderValue);
      }

      if (Math.abs(this.scaling - value) > 0.01) {
         this.scaling = value;
         this.imageReloadRequired = true;
      }

   }

   private void btnFilterHelpActionPerformed(ActionEvent evt) {
      String title = Main.getInstance().getLanguageBundle().getString("FilterHelpTitle");
      String content = Main.getInstance().getLanguageBundle().getString("FilterHelpContent");
      float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      JDialog dialog = new JDialog(this, title, true);
      dialog.setDefaultCloseOperation(2);
      JLabel contentLabel = new JLabel(content);
      contentLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
      JButton closeButton = new JButton(Main.getInstance().getLanguageBundle().getString("Close"));
      int buttonWidth = (int)(80.0F * menuScaling);
      int buttonHeight = (int)(32.0F * menuScaling);
      closeButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
      closeButton.addActionListener((e) -> dialog.dispose());
      JPanel buttonPanel = new JPanel(new FlowLayout());
      buttonPanel.add(closeButton);
      dialog.setLayout(new BorderLayout());
      dialog.add(contentLabel, "Center");
      dialog.add(buttonPanel, "South");
      dialog.pack();
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
   }

   private void btnWebsiteActionPerformed(ActionEvent evt) {
      String websiteUrl = Main.getInstance().getProperties().getProperty("WebsiteURL", "https://github.com/ahmadmol/shinobi-mate");
      this.browseToUrl(websiteUrl);
   }

   private void btnDiscordActionPerformed(ActionEvent evt) {
      this.browseToUrl("https://discord.gg/NBq3zqfA2B");
   }

   private void btnPatreonActionPerformed(ActionEvent evt) {
      this.browseToUrl("https://patreon.com/kilkakon");
   }

   private void sldOpacityStateChanged(ChangeEvent evt) {
      if (!this.sldOpacity.getValueIsAdjusting()) {
         if (this.sldOpacity.getValue() == 0) {
            this.sldOpacity.setValue(5);
         } else {
            this.opacity = (double)this.sldOpacity.getValue() / (double)100.0F;
         }
      }

   }

   private void spnWindowHeightStateChanged(ChangeEvent evt) {
      this.windowSize.height = ((SpinnerNumberModel)this.spnWindowHeight.getModel()).getNumber().intValue();
   }

   private void spnWindowWidthStateChanged(ChangeEvent evt) {
      this.windowSize.width = ((SpinnerNumberModel)this.spnWindowWidth.getModel()).getNumber().intValue();
   }

   private void btnBackgroundColourChangeActionPerformed(ActionEvent evt) {
      this.backgroundColour = this.chooseColour(this.backgroundColour, this.txtBackgroundColour, this.pnlBackgroundPreview, "ChooseBackgroundColour");
   }

   private void chkWindowModeEnabledItemStateChanged(ItemEvent evt) {
      this.windowedMode = evt.getStateChange() == 1;
      this.spnWindowWidth.setEnabled(this.windowedMode);
      this.spnWindowHeight.setEnabled(this.windowedMode);
      this.btnBackgroundColourChange.setEnabled(this.windowedMode);
      this.btnBackgroundImageChange.setEnabled(this.windowedMode);
      this.cmbBackgroundImageMode.setEnabled(this.windowedMode && this.backgroundImage != null);
      this.btnBackgroundImageRemove.setEnabled(this.windowedMode && this.backgroundImage != null);
   }

   private void btnBackgroundImageChangeActionPerformed(ActionEvent evt) {
      JFileChooser dialog = new JFileChooser();
      dialog.setDialogTitle(Main.getInstance().getLanguageBundle().getString("ChooseBackgroundImage"));
      if (dialog.showOpenDialog(this) == 0) {
         try {
            this.backgroundImage = dialog.getSelectedFile().getCanonicalPath();
            this.refreshBackgroundImage();
         } catch (Exception var4) {
            this.backgroundImage = null;
            this.lblBackgroundImage.setIcon((Icon)null);
         }

         this.cmbBackgroundImageMode.setEnabled(this.windowedMode && this.backgroundImage != null);
         this.btnBackgroundImageRemove.setEnabled(this.windowedMode && this.backgroundImage != null);
      }

   }

   private void btnBackgroundImageRemoveActionPerformed(ActionEvent evt) {
      this.backgroundImage = null;
      this.lblBackgroundImage.setIcon((Icon)null);
      this.cmbBackgroundImageMode.setEnabled(false);
      this.btnBackgroundImageRemove.setEnabled(false);
   }

   private void cmbBackgroundImageModeActionPerformed(ActionEvent evt) {
      if (this.cmbBackgroundImageMode.getSelectedIndex() > -1) {
         this.backgroundMode = this.backgroundModes[this.cmbBackgroundImageMode.getSelectedIndex()];
      }

      this.refreshBackgroundImage();
   }

   private void btnPrimaryColour1ChangeActionPerformed(ActionEvent evt) {
      this.primaryColour1 = this.chooseColour(this.primaryColour1, this.txtPrimaryColour1, this.pnlPrimaryColour1Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnPrimaryColour2ChangeActionPerformed(ActionEvent evt) {
      this.primaryColour2 = this.chooseColour(this.primaryColour2, this.txtPrimaryColour2, this.pnlPrimaryColour2Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnPrimaryColour3ChangeActionPerformed(ActionEvent evt) {
      this.primaryColour3 = this.chooseColour(this.primaryColour3, this.txtPrimaryColour3, this.pnlPrimaryColour3Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnSecondaryColour1ChangeActionPerformed(ActionEvent evt) {
      this.secondaryColour1 = this.chooseColour(this.secondaryColour1, this.txtSecondaryColour1, this.pnlSecondaryColour1Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnSecondaryColour2ChangeActionPerformed(ActionEvent evt) {
      this.secondaryColour2 = this.chooseColour(this.secondaryColour2, this.txtSecondaryColour2, this.pnlSecondaryColour2Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnSecondaryColour3ChangeActionPerformed(ActionEvent evt) {
      this.secondaryColour3 = this.chooseColour(this.secondaryColour3, this.txtSecondaryColour3, this.pnlSecondaryColour3Preview, "ChooseColour");
      this.refreshTheme();
   }

   private void sldMenuOpacityStateChanged(ChangeEvent evt) {
      if (!this.sldMenuOpacity.getValueIsAdjusting()) {
         if (this.sldMenuOpacity.getValue() == 0) {
            this.sldMenuOpacity.setValue(1);
         } else {
            this.menuOpacity = (double)this.sldMenuOpacity.getValue() / (double)100.0F;
            this.refreshTheme();
         }
      }

   }

   private void btnChangeFontActionPerformed(ActionEvent evt) {
      JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(this);
      Font newFont = this.showFontDialog(frame, this.font);
      if (newFont != null) {
         this.font = newFont;
         this.refreshTheme();
      }

   }

   private void btnResetActionPerformed(ActionEvent evt) {
      float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      this.primaryColour1 = Color.decode("#1EA6EB");
      this.primaryColour2 = Color.decode("#28B0F5");
      this.primaryColour3 = Color.decode("#32BAFF");
      this.secondaryColour1 = Color.decode("#BCBCBE");
      this.secondaryColour2 = Color.decode("#C6C6C8");
      this.secondaryColour3 = Color.decode("#D0D0D2");
      this.blackColour = Color.decode("#000000");
      this.whiteColour = Color.decode("#FFFFFF");
      this.font = Font.decode("SansSerif-PLAIN-12");
      this.font = this.font.deriveFont((float)this.font.getSize() * menuScaling);
      this.pnlPrimaryColour1Preview.setBackground(this.primaryColour1);
      this.txtPrimaryColour1.setText(String.format("#%02X%02X%02X", this.primaryColour1.getRed(), this.primaryColour1.getGreen(), this.primaryColour1.getBlue()));
      this.pnlPrimaryColour2Preview.setBackground(this.primaryColour2);
      this.txtPrimaryColour2.setText(String.format("#%02X%02X%02X", this.primaryColour2.getRed(), this.primaryColour2.getGreen(), this.primaryColour2.getBlue()));
      this.pnlPrimaryColour3Preview.setBackground(this.primaryColour3);
      this.txtPrimaryColour3.setText(String.format("#%02X%02X%02X", this.primaryColour3.getRed(), this.primaryColour2.getGreen(), this.primaryColour3.getBlue()));
      this.pnlSecondaryColour1Preview.setBackground(this.secondaryColour1);
      this.txtSecondaryColour1.setText(String.format("#%02X%02X%02X", this.secondaryColour1.getRed(), this.secondaryColour1.getGreen(), this.secondaryColour1.getBlue()));
      this.pnlSecondaryColour2Preview.setBackground(this.secondaryColour2);
      this.txtSecondaryColour2.setText(String.format("#%02X%02X%02X", this.secondaryColour2.getRed(), this.secondaryColour2.getGreen(), this.secondaryColour2.getBlue()));
      this.pnlSecondaryColour3Preview.setBackground(this.secondaryColour3);
      this.txtSecondaryColour3.setText(String.format("#%02X%02X%02X", this.secondaryColour3.getRed(), this.secondaryColour3.getGreen(), this.secondaryColour3.getBlue()));
      this.pnlBlackColourPreview.setBackground(this.blackColour);
      this.txtBlackColour.setText(String.format("#%02X%02X%02X", this.blackColour.getRed(), this.blackColour.getGreen(), this.blackColour.getBlue()));
      this.pnlWhiteColourPreview.setBackground(this.whiteColour);
      this.txtWhiteColour.setText(String.format("#%02X%02X%02X", this.whiteColour.getRed(), this.whiteColour.getGreen(), this.whiteColour.getBlue()));
      this.menuOpacity = (double)1.0F;
      this.sldMenuOpacity.setValue((int)(this.menuOpacity * (double)100.0F));
      this.refreshTheme();
   }

   private void chkAlwaysShowInformationScreenItemStateChanged(ItemEvent evt) {
      this.alwaysShowInformationScreen = evt.getStateChange() == 1;
   }

   private void btnBlackColourChangeActionPerformed(ActionEvent evt) {
      this.blackColour = this.chooseColour(this.blackColour, this.txtBlackColour, this.pnlBlackColourPreview, "ChooseColour");
      this.refreshTheme();
   }

   private void btnWhiteColourChangeActionPerformed(ActionEvent evt) {
      this.whiteColour = this.chooseColour(this.whiteColour, this.txtWhiteColour, this.pnlWhiteColourPreview, "ChooseColour");
      this.refreshTheme();
   }

   private Color chooseColour(Color colour, JTextField field, JPanel preview, String title) {
      Color newColour = JColorChooser.showDialog(this, Main.getInstance().getLanguageBundle().getString(title), colour);
      if (newColour != null) {
         colour = newColour;
         field.setText(String.format("#%02X%02X%02X", newColour.getRed(), newColour.getGreen(), newColour.getBlue()));
         preview.setBackground(newColour);
      }

      return colour;
   }

   private void refreshBackgroundImage() {
      Dimension size = this.pnlBackgroundImage.getPreferredSize();
      Image image = (new ImageIcon(this.backgroundImage)).getImage();
      if (this.backgroundMode.equals("stretch")) {
         image = image.getScaledInstance(size.width, size.height, 4);
      } else if (!this.backgroundMode.equals("centre")) {
         double factor = this.backgroundMode.equals("fit") ? Math.min((double)size.width / (double)image.getWidth((ImageObserver)null), (double)size.height / (double)image.getHeight((ImageObserver)null)) : Math.max((double)size.width / (double)image.getWidth((ImageObserver)null), (double)size.height / (double)image.getHeight((ImageObserver)null));
         image = image.getScaledInstance((int)(factor * (double)image.getWidth((ImageObserver)null)), (int)(factor * (double)image.getHeight((ImageObserver)null)), 4);
      }

      this.lblBackgroundImage.setIcon(new ImageIcon(image));
      this.lblBackgroundImage.setPreferredSize(new Dimension(image.getWidth((ImageObserver)null), image.getHeight((ImageObserver)null)));
   }

   private void refreshTheme() {
      try {
         UIManager.put("@accentColor", this.primaryColour1);
         UIManager.put("Button.background", this.primaryColour2);
         UIManager.put("Button.hoverBackground", this.primaryColour3);
         UIManager.put("Panel.background", this.secondaryColour1);
         UIManager.put("TextField.background", this.secondaryColour2);
         UIManager.put("ComboBox.background", this.secondaryColour3);
         if (this.lookAndFeel instanceof FlatLaf) {
            FlatLaf.updateUI();
         }

         SwingUtilities.updateComponentTreeUI(this);
         this.pack();
      } catch (Exception var2) {
      }

   }
}
