/**
 * Copyright(c) 2000-2012 HC360.COM, All Rights Reserved.
 */
package com.hc360.rsf.config.xmlparse;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML解析试验类
 * 
 * @author zhaolei 2012-6-6
 */
public class BeansXMLPares {

	String xmlPath = "com/hc360/rsf/config/xmlparse/ConfigLoader.xml";

	@Test
	public void test_main() {
		BeansXMLPares pares = new BeansXMLPares();
		pares.read();
	}

	private void read() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlPath);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true); // 提供对 XML 名称空间的支持。
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		try {
			document = builder.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 获取文档的根元素,赋值给rootElement变量
		Element rootElement = document.getDocumentElement();
		// 获取元素的count属性
		rootElement.getAttribute("count");

		// 此节点的名称空间 URI；如果它未被指定,则返回 null。
		String namespaceURI = rootElement.getNamespaceURI();
		// 此方法检查指定的 namespaceURI 是否是默认名称空间。
		boolean isDefaultNamespace = rootElement.isDefaultNamespace(namespaceURI);
		System.out.println("NodeName="+rootElement.getNodeName()+",NodeType="+getNodeTypeName(rootElement));
		System.out.println("Prefix="+rootElement.getPrefix()  );
		System.out.println("namespaceURI=" + namespaceURI);
		System.out.println("isDefaultNamespace=" + isDefaultNamespace);
		System.out.println("toString=" + rootElement.toString());
		System.out.println("==================");

		// 获取rootElement的所有子节点（不包括属性节点）,返回一个NodeList对象
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			// 获取childNodes的第i个节点
			Node childNode = childNodes.item(i);
			
			System.out.println("NodeName="+childNode.getNodeName()+",NodeType="+getNodeTypeName(childNode));
			System.out.println("Prefix="+childNode.getPrefix()  );
			// 此节点的名称空间 URI；如果它未被指定,则返回 null。
			String namespaceURI_s = childNode.getNamespaceURI();
			// 此方法检查指定的 namespaceURI 是否是默认名称空间。
			boolean isDefaultNamespace_s = childNode.isDefaultNamespace(namespaceURI_s);
			System.out.println("namespaceURI=" + namespaceURI_s);
			System.out.println("isDefaultNamespace=" + isDefaultNamespace_s);
			System.out.println("TextContent=" + childNode.getTextContent());
			System.out.println("----------------");
			
			if (childNode.getNodeType() == Node.ELEMENT_NODE ) {
				NamedNodeMap map=childNode.getAttributes(); 
				//  检索通过名称指定的节点。
				Attr n_id=(Attr)map.getNamedItem("id");
				if(n_id!=null){
					String v=n_id.getValue();
					System.out.println(v);
				}
			}
		}
	}

	/**
	 * 返回节点类型的字符串描述
	 * @param node 节点
	 * @return 节点类型的字符串描述
	 */
	private String getNodeTypeName(Node node) {
		if (node == null) {
			return null;
		}
		String tmp = null;
		int nodeType = node.getNodeType();
		switch (nodeType) {
		case Node.ATTRIBUTE_NODE:
			tmp = "ATTRIBUTE";
			break;
		case Node.CDATA_SECTION_NODE:
			tmp = "CDATA_SECTION";
			break;
		case Node.COMMENT_NODE:
			tmp = "COMMENT";
			break;
		case Node.DOCUMENT_FRAGMENT_NODE:
			tmp = "DOCUMENT_FRAGMENT";
			break;
		case Node.DOCUMENT_NODE:
			tmp = "DOCUMENT";
			break;
		case Node.DOCUMENT_TYPE_NODE:
			tmp = "DOCUMENT_TYPE";
			break;
		case Node.ELEMENT_NODE:
			tmp = "ELEMENT";
			break;
		case Node.ENTITY_NODE:
			tmp = "ENTITY";
			break;
		case Node.ENTITY_REFERENCE_NODE:
			tmp = "ENTITY_REFERENCE";
			break;
		case Node.NOTATION_NODE:
			tmp = "NOTATION";
			break;
		case Node.PROCESSING_INSTRUCTION_NODE:
			tmp = "PROCESSING_INSTRUCTION";
			break;
		case Node.TEXT_NODE:
			tmp = "TEXT";
			break;
		default:
			return null;
		}
		return tmp;
	}
}
