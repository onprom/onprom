/*
 * ocel
 *
 * XmlUtil.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.pm.ocel.util;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unchecked")
public class XmlUtil {
    private String rootDom = "root";
    private SAXReader sax;
    private Document document;
    private Element root;
    private String format="";

    public XmlUtil() {
        super();
        sax = new SAXReader();
    }

    public XmlUtil(String xml) {
        super();
        sax = new SAXReader();
        this.read(xml);
    }

    public XmlUtil(File xml) {
        super();
        sax = new SAXReader();
        this.read(xml);
    }

    public XmlUtil(SAXReader sax) {
        super();
        this.sax = sax;
    }

    /**
     */
    public static Element parse(Element node , String type , String val) {
        for (Iterator iter = node.elementIterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            Attribute name = element.attribute(type);
            if (name != null) {
                String value = name.getValue();
                if (val.equals(value))
                    return element;
                else
                    parse(element, type, val);
            }
        }
        return null;
    }

    /**
     * get the root node, throwing exception if there is no root node
     * @throws DocumentException
     */
    public Element read(File file){
        try {
            document = sax.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // get root node
        root = document.getRootElement();
        return root;
    }

    /**
     *
     * get the root node, auto-create a root node if there is no root node
     *
     * @throws DocumentException
     */
    public Element read(String xml) {
        if(!verifyRoot(xml)) {
            xml = addRoot(xml, rootDom);
        }

        try {
            document = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        root = document.getRootElement();
        return root;
    }

    /**
     * start from an assigned note, print all nodes and their subnodes
     * @param node
     */
    public void printAllNodes(Element node) {
        printNode(node);

        List<Element> listElement = node.elements();

        // loop all the one-level nodes
        for (Element e : listElement) {
            format += " ";
            printAllNodes(e);
        }
        format = "";
    }

    public void printAllNodes() {
        printAllNodes(root);
    }

    /**
     * print node, not include its subnodes
     * @param node
     */
    public void printNode(Element node) {
        System.out.println(format+node.getName());
        String text = node.getTextTrim();
        if(!this.emptyStr(text)) {
            System.out.println(format+" {" + text+"}");
        }
        List<Attribute> listAttr = node.attributes();// all attributes
        for (Attribute attr : listAttr) {
            String name = attr.getName();
            String value = attr.getValue();
            System.out.println(format+"  ["+ name +": "+ value+"]");
        }
    }

    /**
     * put the key-value of the node to a HashMap
     * @param node
     */
    public Map<String,String> mapAttribute(Element node) {
        Map<String, String> map = new HashMap<>();
        for(Iterator<Attribute> it = node.attributeIterator();
            it.hasNext();) {
            Attribute attribute = it.next();
            map.put(attribute.getName().trim(),
                    attribute.getText().trim());
        }
        return map;
    }

    /**
     * given an attribute of a node,and put the key-value of the node to a HashMap
     *
     * @param node
     */
    public Map<String,String> mapAttribute(Element node, String attName) {
        Map<String, String> map = new HashMap<>();
        for(Iterator<Attribute> it = node.attributeIterator();
            it.hasNext();) {
            Attribute attribute = it.next();
            if (attribute.equals(attName)){
                map.put(attribute.getName().trim(),
                        attribute.getText().trim());
            }

        }
        return map;
    }

    /**
     * judge the empty string
     * @param str
     *
     * @return true(empty)/false(not empty)
     */
    private boolean emptyStr(String str) {
        return str == null || str.length() == 0;
    }

    /**
     *
     * judge a xml string has root node or not
     * @param xml
     * @return true(has root node)/false(no root node)
     */
    public boolean verifyRoot(String xml) {
        String begin = xml.substring(xml
                .indexOf("<")+1, xml
                .indexOf(">"));
        String end = xml.substring(xml
                .lastIndexOf("/")+1, xml
                .lastIndexOf(">"));
        return begin.equals(end);
    }

    private String addRoot(String xml,String domName) {
        return "<"+domName+">"+xml+"</"+domName+">";
    }

    public String getRootDom() {
        return rootDom;
    }

    public void setRootDom(String rootDom) {
        this.rootDom = rootDom;
    }

    public SAXReader getSax() {
        return sax;
    }

    public Element getRoot() {
        return root;
    }

    public Document getDocument() {
        return document;
    }
}
