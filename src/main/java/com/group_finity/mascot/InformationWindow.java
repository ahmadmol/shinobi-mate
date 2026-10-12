package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent.EventType;

public class InformationWindow extends JFrame {
   private JButton btnClose;
   private JLabel lblSplashImage;
   private JEditorPane pnlEditorPane;
   private JPanel pnlFooter;
   private JScrollPane pnlScrollPane;

   public InformationWindow() {
      this.initComponents();
      com.group_finity.mascot.ui.AppIcon.apply(this);
   }

   public void init(String imageSet, Configuration config) {
      this.setLocationRelativeTo((Component)null);
      Image image = (new ImageIcon("./img/" + imageSet + "/" + config.getInformation("SplashImage"))).getImage();

      try {
         this.lblSplashImage.setIcon(new ImageIcon(image));
      } catch (Exception var11) {
      }

      Properties themeProperties = new Properties();

      try {
         String baseDir = System.getProperty("app.dir");
         if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
         }

         if (baseDir == null) {
            baseDir = ".";
         }

         String themeFile = com.group_finity.mascot.AppPaths.theme().toString();
         FileInputStream input = new FileInputStream(themeFile);
         themeProperties.load(input);
      } catch (IOException var10) {
      }

      Color textColour = Color.BLACK;
      Color linkColour = new Color(40,176,245);
      ResourceBundle language = Main.getInstance().getLanguageBundle();
      this.setTitle(config.containsInformationKey("Name") ? config.getInformation("Name") : language.getString("Information"));
      StringBuilder html = new StringBuilder("<center style=\"font:");
      if (this.lblSplashImage.getFont().getStyle() == 1) {
         html.append("bold ");
      }

      if (this.lblSplashImage.getFont().getStyle() == 2) {
         html.append("italic ");
      }

      if (this.lblSplashImage.getFont().getStyle() == 3) {
         html.append("italic bold ");
      }

      html.append(this.lblSplashImage.getFont().getSize());
      html.append("pt ");
      html.append(this.lblSplashImage.getFont().getFontName());
      html.append("; color:");
      html.append(String.format("#%02X%02X%02X", textColour.getRed(), textColour.getGreen(), textColour.getBlue()));
      html.append("\">");
      if (config.containsInformationKey("ArtistName")) {
         html.append(language.getString("ArtBy"));
         html.append(" ");
         if (config.containsInformationKey("ArtistURL")) {
            html.append("<a href=\"");
            html.append(config.getInformation("ArtistURL"));
            html.append("\" style=\"color:");
            html.append(String.format("#%02X%02X%02X", linkColour.getRed(), linkColour.getGreen(), linkColour.getBlue()));
            html.append("\">");
         }

         html.append(config.getInformation("ArtistName"));
         if (config.containsInformationKey("ArtistURL")) {
            html.append("</a>");
         }
      }

      if (config.containsInformationKey("ScripterName")) {
         if (config.containsInformationKey("ArtistName")) {
            html.append(" - ");
         }

         html.append(language.getString("ScriptedBy"));
         html.append(" ");
         if (config.containsInformationKey("ScripterURL")) {
            html.append("<a href=\"");
            html.append(config.getInformation("ScripterURL"));
            html.append("\" style=\"color:");
            html.append(String.format("#%02X%02X%02X", linkColour.getRed(), linkColour.getGreen(), linkColour.getBlue()));
            html.append("\">");
         }

         html.append(config.getInformation("ScripterName"));
         if (config.containsInformationKey("ScripterURL")) {
            html.append("</a>");
         }
      }

      if (config.containsInformationKey("CommissionerName")) {
         if (config.containsInformationKey("ArtistName") || config.containsInformationKey("ScripterName")) {
            html.append(" - ");
         }

         html.append(language.getString("CommissionedBy"));
         html.append(" ");
         if (config.containsInformationKey("CommissionerURL")) {
            html.append("<a href=\"");
            html.append(config.getInformation("CommissionerURL"));
            html.append("\" style=\"color:");
            html.append(String.format("#%02X%02X%02X", linkColour.getRed(), linkColour.getGreen(), linkColour.getBlue()));
            html.append("\">");
         }

         html.append(config.getInformation("CommissionerName"));
         if (config.containsInformationKey("CommissionerURL")) {
            html.append("</a>");
         }
      }

      if (config.containsInformationKey("SupportName")) {
         if (config.containsInformationKey("ArtistName") || config.containsInformationKey("ScripterName") || config.containsInformationKey("CommissionerName")) {
            html.append(" - ");
         }

         html.append(language.getString("SupportAt"));
         html.append(" ");
         if (config.containsInformationKey("SupportURL")) {
            html.append("<a href=\"");
            html.append(config.getInformation("SupportURL"));
            html.append("\" style=\"color:");
            html.append(String.format("#%02X%02X%02X", linkColour.getRed(), linkColour.getGreen(), linkColour.getBlue()));
            html.append("\">");
         }

         html.append(config.getInformation("SupportName"));
         if (config.containsInformationKey("SupportURL")) {
            html.append("</a>");
         }
      }

      html.append("</center>");
      this.pnlEditorPane.setText(html.toString());
      this.pnlEditorPane.addHyperlinkListener((e) -> {
         if (e.getEventType() == EventType.ACTIVATED) {
            StringTokenizer st = new StringTokenizer(e.getDescription(), " ");
            if (st.hasMoreTokens()) {
               String url = st.nextToken();
               if (JOptionPane.showConfirmDialog(this, language.getString("ConfirmVisitWebsiteMessage") + "\n" + language.getString("ExerciseCautionAndBewareSusLinksMessage") + "\n" + url, language.getString("VisitWebsite"), 0) == 0) {
                  try {
                     Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                     if (desktop == null || !desktop.isSupported(Action.BROWSE)) {
                        String var10002 = Main.getInstance().getLanguageBundle().getString("FailedOpenWebBrowserErrorMessage");
                        throw new UnsupportedOperationException(var10002 + " " + url);
                     }

                     desktop.browse(new URI(url));
                  } catch (Exception exc) {
                     JOptionPane.showMessageDialog(this, exc.getMessage(), "Error", -1);
                  }
               }
            }
         }

      });
      this.btnClose.setText(language.getString("Close"));
   }

   public void display() {
      float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96.0F;
      this.pnlEditorPane.setBackground(this.getBackground());
      this.pnlEditorPane.setBorder((Border)null);
      this.pnlScrollPane.setBorder((Border)null);
      this.pnlScrollPane.setViewportBorder((Border)null);
      this.lblSplashImage.setPreferredSize(new Dimension((int)((float)this.lblSplashImage.getIcon().getIconWidth() * menuScaling), (int)((float)this.lblSplashImage.getIcon().getIconHeight() * menuScaling)));
      this.pnlEditorPane.setPreferredSize(new Dimension((int)((float)this.pnlEditorPane.getPreferredSize().width * menuScaling), (int)((float)this.pnlEditorPane.getPreferredSize().height * menuScaling)));
      this.pnlScrollPane.setPreferredSize(new Dimension((int)((float)this.pnlScrollPane.getPreferredSize().width * menuScaling), (int)((float)this.pnlScrollPane.getPreferredSize().height * menuScaling)));
      this.btnClose.setPreferredSize(new Dimension((int)((float)this.btnClose.getPreferredSize().width * menuScaling), (int)((float)this.btnClose.getPreferredSize().height * menuScaling)));
      this.pnlFooter.setPreferredSize(new Dimension(this.pnlFooter.getPreferredSize().width, this.btnClose.getPreferredSize().height + 6));
      this.pack();
      this.setLocationRelativeTo((Component)null);
      this.setVisible(true);
   }

   private void initComponents() {
      JPanel pnlImage = new JPanel();
      this.lblSplashImage = new JLabel();
      this.pnlScrollPane = new JScrollPane();
      this.pnlEditorPane = new JEditorPane();
      this.pnlFooter = new JPanel();
      this.btnClose = new JButton();
      this.setDefaultCloseOperation(2);
      pnlImage.setLayout(new BoxLayout(pnlImage, 1));
      this.lblSplashImage.setAlignmentX(0.5F);
      pnlImage.add(this.lblSplashImage);
      this.pnlEditorPane.setEditable(false);
      this.pnlEditorPane.setBorder((Border)null);
      this.pnlEditorPane.setContentType("text/html");
      this.pnlEditorPane.setText("");
      this.pnlScrollPane.setViewportView(this.pnlEditorPane);
      this.pnlFooter.setPreferredSize(new Dimension(380, 36));
      this.pnlFooter.setLayout(new FlowLayout(1, 10, 5));
      this.btnClose.setText("Close");
      this.btnClose.setMaximumSize(new Dimension(130, 26));
      this.btnClose.setMinimumSize(new Dimension(95, 23));
      this.btnClose.setName("");
      this.btnClose.setPreferredSize(new Dimension(130, 26));
      this.btnClose.addActionListener((evt) -> this.btnCloseActionPerformed(evt));
      this.pnlFooter.add(this.btnClose);
      GroupLayout layout = new GroupLayout(this.getContentPane());
      this.getContentPane().setLayout(layout);
      layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(pnlImage, -1, 600, 32767).addComponent(this.pnlFooter, Alignment.LEADING, -1, -1, 32767).addComponent(this.pnlScrollPane, Alignment.LEADING)).addContainerGap()));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(pnlImage, -1, -1, 32767).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlScrollPane, -2, 26, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.pnlFooter, -2, -1, -2).addContainerGap()));
      this.pack();
   }

   private void btnCloseActionPerformed(ActionEvent evt) {
      this.dispose();
   }

   public static void main(String[] args) {
      try {
         for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
         Logger.getLogger(InformationWindow.class.getName()).log(Level.SEVERE, (String)null, ex);
      }

      EventQueue.invokeLater(() -> {
      });
   }
}
