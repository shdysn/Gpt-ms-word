package com.example.data.service.docx

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Common XML utility functions and secure DOM parsing for DOCX files.
 */
object DocxXmlUtils {

    val secureDocBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        try {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        } catch (_: Exception) {}
    }

    fun parseXml(bytes: ByteArray): Document {
        return secureDocBuilderFactory.newDocumentBuilder().parse(ByteArrayInputStream(bytes))
    }

    fun extractElementText(elem: Element): String {
        val tNodes = elem.getElementsByTagName("w:t")
        val sb = StringBuilder()
        for (i in 0 until tNodes.length) {
            sb.append(tNodes.item(i).textContent)
        }
        return sb.toString()
    }

    fun extractHeaderFooterText(dom: Document): String {
        val sb = StringBuilder()
        fun traverse(node: Node) {
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                if (element.tagName == "w:fldSimple") {
                    val instr = element.getAttribute("w:instr")
                    when {
                        instr.contains("NUMPAGES", ignoreCase = true) -> sb.append("{pages}")
                        instr.contains("PAGE", ignoreCase = true) -> sb.append("{page}")
                        else -> {
                            val tNodes = element.getElementsByTagName("w:t")
                            for (i in 0 until tNodes.length) {
                                sb.append(tNodes.item(i).textContent)
                            }
                        }
                    }
                    return
                }
                if (element.tagName == "w:t") {
                    sb.append(element.textContent)
                    return
                }
            }
            val children = node.childNodes
            for (i in 0 until children.length) {
                traverse(children.item(i))
            }
        }
        traverse(dom.documentElement ?: dom)
        return sb.toString()
    }
}
