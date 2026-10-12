package com.group_finity.mascot.pack;
import java.nio.file.Path;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
public final class SecureXml {
 private SecureXml(){ }
 public static Document read(Path path)throws Exception {
  var f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
  f.setFeature("http://xml.org/sax/features/external-general-entities",false);f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
  f.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD,"");f.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");f.setXIncludeAware(false);f.setExpandEntityReferences(false);
  var builder=f.newDocumentBuilder();
  builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler(){
   @Override public void error(org.xml.sax.SAXParseException error)throws org.xml.sax.SAXException{throw error;}
   @Override public void fatalError(org.xml.sax.SAXParseException error)throws org.xml.sax.SAXException{throw error;}
  });
  return builder.parse(path.toFile());
 }
}
