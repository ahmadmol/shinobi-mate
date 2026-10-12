package com.group_finity.mascot.animationeditor.util;

import com.group_finity.mascot.animationeditor.model.AnimationAction;
import com.group_finity.mascot.animationeditor.model.AnimationPose;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLConfigUtil {
   private static final Logger log = Logger.getLogger(XMLConfigUtil.class.getName());
   private static Document originalDocument;

   public static List<AnimationAction> loadActionsFromXML(File xmlFile) {
      List<AnimationAction> actions = new ArrayList<>();
      if (!xmlFile.exists()) {
         log.log(Level.WARNING, "Actions XML file does not exist: " + xmlFile.getPath());
         return actions;
      } else {
         try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            originalDocument = builder.parse(xmlFile);
            NodeList actionNodes = originalDocument.getElementsByTagName("Action");

            for(int i = 0; i < actionNodes.getLength(); ++i) {
               Element actionElement = (Element)actionNodes.item(i);
               NodeList animationNodes = actionElement.getElementsByTagName("Animation");
               if (animationNodes.getLength() > 0) {
                  AnimationAction action = parseActionElement(actionElement);
                  if (action != null) {
                     actions.add(action);
                  }
               }
            }
         } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load actions from XML: " + xmlFile.getPath(), e);
         }

         return actions;
      }
   }

   private static AnimationAction parseActionElement(Element actionElement) {
      try {
         String name = actionElement.getAttribute("Name");
         String type = actionElement.getAttribute("Type");
         AnimationAction action = new AnimationAction(name, type);
         action.setClassName(actionElement.getAttribute("Class"));
         action.setBorderType(actionElement.getAttribute("BorderType"));
         NamedNodeMap attributes = actionElement.getAttributes();

         for(int i = 0; i < attributes.getLength(); ++i) {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            action.setAttribute(attrName, attrValue);
         }

         NodeList animationNodes = actionElement.getElementsByTagName("Animation");
         if (animationNodes.getLength() > 0) {
            Element animationElement = (Element)animationNodes.item(0);
            NodeList poseNodes = animationElement.getElementsByTagName("Pose");

            for(int j = 0; j < poseNodes.getLength(); ++j) {
               Element poseElement = (Element)poseNodes.item(j);
               AnimationPose pose = parsePoseElement(poseElement);
               if (pose != null) {
                  action.addPose(pose);
               }
            }
         }

         return action;
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to parse action element", e);
         return null;
      }
   }

   private static AnimationPose parsePoseElement(Element poseElement) {
      try {
         AnimationPose pose = new AnimationPose();
         pose.setImage(poseElement.getAttribute("Image"));
         pose.setImageAnchor(poseElement.getAttribute("ImageAnchor"));
         pose.setVelocity(poseElement.getAttribute("Velocity"));
         pose.setSound(poseElement.getAttribute("Sound"));
         String durationStr = poseElement.getAttribute("Duration");
         if (!durationStr.isEmpty()) {
            pose.setDuration(Integer.parseInt(durationStr));
         }

         String volumeStr = poseElement.getAttribute("Volume");
         if (!volumeStr.isEmpty()) {
            pose.setVolume(Double.parseDouble(volumeStr));
         }

         return pose;
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to parse pose element", e);
         return null;
      }
   }

   public static boolean saveActionsToXML(List<AnimationAction> actions, File xmlFile) {
      try {
         if (originalDocument == null) {
            log.log(Level.SEVERE, "No original document cached. Must load first.");
            return false;
         } else {
            for(AnimationAction action : actions) {
               updateActionInDocument(originalDocument, action);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(originalDocument);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
            return true;
         }
      } catch (Exception e) {
         log.log(Level.SEVERE, "Failed to save actions to XML: " + xmlFile.getPath(), e);
         return false;
      }
   }

   private static void updateActionInDocument(Document doc, AnimationAction action) {
      try {
         NodeList actionNodes = doc.getElementsByTagName("Action");

         for(int i = 0; i < actionNodes.getLength(); ++i) {
            Element actionElement = (Element)actionNodes.item(i);
            String actionName = actionElement.getAttribute("Name");
            if (actionName.equals(action.getName())) {
               NodeList animationNodes = actionElement.getElementsByTagName("Animation");
               if (animationNodes.getLength() > 0) {
                  updateActionElement(actionElement, action);
               }
               break;
            }
         }
      } catch (Exception e) {
         log.log(Level.WARNING, "Failed to update action in document: " + action.getName(), e);
      }
   }

   private static void updateActionElement(Element actionElement, AnimationAction action) {
      Map<String, String> attributes = action.getAttributes();
      if (attributes != null) {
         for(Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.trim().isEmpty()) {
               actionElement.setAttribute(key, value);
            }
         }
      }

      NodeList animationNodes = actionElement.getElementsByTagName("Animation");
      if (animationNodes.getLength() > 0) {
         Element animationElement = (Element)animationNodes.item(0);
         NodeList poseNodes = animationElement.getElementsByTagName("Pose");

         while(poseNodes.getLength() > 0) {
            Node poseNode = poseNodes.item(0);
            animationElement.removeChild(poseNode);
         }

         for(AnimationPose pose : action.getPoses()) {
            Element poseElement = createPoseElement(actionElement.getOwnerDocument(), pose);
            animationElement.appendChild(poseElement);
         }
      }
   }

   private static Element createPoseElement(Document doc, AnimationPose pose) {
      Element poseElement = doc.createElement("Pose");
      if (pose.getImage() != null && !pose.getImage().isEmpty()) {
         poseElement.setAttribute("Image", pose.getImage());
      }

      if (pose.getImageAnchor() != null && !pose.getImageAnchor().isEmpty()) {
         poseElement.setAttribute("ImageAnchor", pose.getImageAnchor());
      }

      if (pose.getVelocity() != null && !pose.getVelocity().isEmpty()) {
         poseElement.setAttribute("Velocity", pose.getVelocity());
      }

      poseElement.setAttribute("Duration", String.valueOf(pose.getDuration()));
      if (pose.getSound() != null && !pose.getSound().isEmpty()) {
         poseElement.setAttribute("Sound", pose.getSound());
      }

      if (pose.getVolume() != (double)1.0F) {
         poseElement.setAttribute("Volume", String.valueOf(pose.getVolume()));
      }

      return poseElement;
   }
}
