package it.unibz.inf.pm.ocel.util;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

public class XmlUtilTest {
    public static void main(String[] args) {
        String xml = "<p><img src='images/cansu.gif' title='cansu1' alt='2.jpg'/></p><a>test</a>"
                + "<p>pdata1</p><a><img src='images/cansu2.gif' title='cansu2' alt='3.jpg'/>test2</a>";

        XmlUtil test = new XmlUtil(xml);
        Document dom = test.getDocument();

        List<Element> elements = dom.selectNodes("//p/img");
        elements = dom.selectNodes("//img");
        Element element = (Element) dom.selectSingleNode("//img");


        List<Attribute> attributes = dom.selectNodes("//img/@src");
        attributes = element.attributes();
        Attribute attribute = element.attribute(2);
        attribute = element.attribute("title");
        Map<String,String> map = test.mapAttribute(element);

        test.printAllNodes();

    }
}
