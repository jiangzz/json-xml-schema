package com.jangzz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import net.javacrumbs.json2xml.JsonXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class JSONXMLSchemaUtils {
    /**
     * 生成JSON Schema内容信息，产生json-path信息路径信息
     * @param content
     * @return
     * @throws Exception
     */
    public static List<ColumnInfo> generateJSONPathSchema(String content) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(content));
        DOMResult result = new DOMResult();
        JsonXmlReader jsonXmlReader = new JsonXmlReader(null, true, "root");
        transformer.transform(new SAXSource(jsonXmlReader, source), result);
        Node node = result.getNode();
        HashMap<String, String> pathInfo = new HashMap<>();
        parseJsonNodePath(pathInfo,node,null,"object");
        ArrayList<ColumnInfo> columnInfos = new ArrayList<>();
        pathInfo.keySet().stream().sorted().forEach(key->columnInfos.add(new ColumnInfo(key,pathInfo.get(key))));
        return columnInfos;
    }

    /**
     * 生成XML Schema内容信息，产生xml-path信息路径信息
     * @param content
     * @param includeRootElement
     * @return
     * @throws Exception
     */
    public static List<ColumnInfo> generateXMLPathSchema(String content, Boolean includeRootElement) throws Exception {
       DocumentBuilderFactory factroy = DocumentBuilderFactory.newInstance();
       DocumentBuilder documentBuilder = factroy.newDocumentBuilder();
       Document document = documentBuilder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
       Node root = document.getDocumentElement();
       HashMap<String, String> pathInfo = new HashMap<>();
       parseXMLNodePath(pathInfo,root,null,includeRootElement);
       ArrayList<ColumnInfo> columnInfos = new ArrayList<>();
       pathInfo.keySet().stream().sorted().forEach(key->columnInfos.add(new ColumnInfo(key,pathInfo.get(key))));
       return columnInfos;
   }

    /**
     * 生成JSON tree Schema信息
     * @param content
     * @return
     * @throws Exception
     */
    public static List<MateNode> generateJSONTreeSchema(String content) throws Exception {
        List<ColumnInfo> columnInfos = generateJSONPathSchema(content);
        return generateTreePathSchema(columnInfos);
    }

    /**
     * 生成XML Tree Schema信息
     * @param content
     * @param includeRootElement
     * @return
     * @throws Exception
     */
    public static List<MateNode> generateXMLTreeSchema(String content,Boolean includeRootElement) throws Exception {
        List<ColumnInfo> columnInfos = generateXMLPathSchema(content,includeRootElement);
        return generateTreePathSchema(columnInfos);
    }
    private static void parseJsonNodePath(Map<String,String> pathInfo, Node node, String parentPath, String parentType){
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            String nodeName = item.getNodeName();
            String typeName=null;
            if(!nodeName.equals("#text")){
                String pathName="";
                Node type = item.getAttributes().getNamedItem("type");
                if(type!=null){
                    typeName=type.getNodeValue();
                }else{
                    typeName="object";
                }
                if(parentPath!=null ){
                    pathName=parentPath;
                    if(!parentType.equals("array")){
                        pathName=parentPath+"."+nodeName;
                    }
                }else{
                    pathName=nodeName;
                }
                if(!parentType.equals("array")){
                    if(!pathName.equals("root")){
                        pathName=pathName.replace("root.","");
                        pathInfo.put(pathName,typeName);
                    }
                }
                parseJsonNodePath(pathInfo,item,pathName,typeName);
            }
        }
    }
    private static void parseXMLNodePath(Map<String,String> pathInfo, Node currentNode, String parentPath,Boolean includeRoot){
        HashSet<String> nodeNameSet=new HashSet<String>();
        NodeList childNodes = currentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            String childNodeName = childNode.getNodeName();

            if(!childNodeName.equals("#text")){
                String prefixPath=null;
                if(parentPath!=null){
                    if(includeRoot){
                        prefixPath=parentPath+"."+currentNode.getNodeName();
                    }else{
                        prefixPath=parentPath;
                    }
                }else{
                    if(includeRoot){
                        prefixPath=currentNode.getNodeName();
                    }
                }
                nodeNameSet.add(childNodeName);
                //判斷仅仅只有一个元素
                if(childNode.getChildNodes().getLength()==1){
                    if(prefixPath==null){
                        prefixPath=childNodeName;
                    }else{
                        prefixPath=prefixPath+"."+childNodeName;
                    }
                    String v=childNode.getTextContent();
                    try {
                        Integer.valueOf(v);
                        pathInfo.put(prefixPath,"int");
                    }catch (NumberFormatException e){
                        try {
                            Float.valueOf(v);
                            pathInfo.put(prefixPath,"float");
                        } catch (NumberFormatException numberFormatException) {
                            if(v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")){
                                pathInfo.put(prefixPath,"boolean");
                            }else{
                                pathInfo.put(prefixPath,"string");
                            }
                        }
                    }
                }else{
                    parseXMLNodePath(pathInfo,childNode,prefixPath,true);
                }
            }
        }
        if(includeRoot){
            String currentPath=currentNode.getNodeName();
            if(parentPath!=null){
                currentPath=parentPath+"."+currentPath;
            }
            if(nodeNameSet.size()==1){
                pathInfo.put(currentPath,"array");
            }else{
                pathInfo.put(currentPath,"object");
            }
        }
    }

    public static class ColumnInfo{
        private String columnPath;
        private String columnType;

        public ColumnInfo(String columnPath, String columnType) {
            this.columnPath = columnPath;
            this.columnType = columnType;
        }

        public String getColumnPath() {
            return columnPath;
        }

        public void setColumnPath(String columnPath) {
            this.columnPath = columnPath;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        @Override
        public String toString() {
            return "ColumnInfo{" +
                    "columnPath='" + columnPath + '\'' +
                    ", columnType='" + columnType + '\'' +
                    '}';
        }
    }
    public static class MateNode{
        private String name;
        private String fullPath;
        private String type;
        private List<MateNode> childNodes;

        public MateNode(String name, String fullPath, String type) {
            this.name = name;
            this.fullPath = fullPath;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<MateNode> getChildNodes() {
            return childNodes;
        }

        public void setChildNodes(List<MateNode> childNodes) {
            this.childNodes = childNodes;
        }

    }



    private static List<MateNode> generateTreePathSchema(List<ColumnInfo> infos) throws JsonProcessingException {
        infos=infos.stream().sorted((k1,k2)->{
            if(k1.getColumnPath().contains(".") || k2.getColumnPath().contains(".")){
                return k1.getColumnPath().split("\\.").length-k2.getColumnPath().split("\\.").length;
            }else{
                return k1.getColumnPath().compareTo(k2.getColumnPath());
            }
        }).collect(Collectors.toList());
        ArrayList<MateNode> resultsNode = new ArrayList<>();
        while (infos.size()>0){
            ColumnInfo columnInfo = infos.get(0);
            infos.remove(columnInfo);
            String columnPath = columnInfo.getColumnPath();
            String shortName=columnPath.substring(columnPath.lastIndexOf(".")+1);
            MateNode currentNode= new MateNode(shortName,columnPath, columnInfo.getColumnType());
            if (columnInfo.getColumnType().equals("object") || columnInfo.getColumnType().equals("array")){
                //找到该模型的所有子集合
                int currentDepth = columnPath.split("\\.").length;
                List<ColumnInfo> subColumns = infos.stream().filter(item -> item.getColumnPath().startsWith(columnPath) && item.getColumnPath().split("\\.").length > currentDepth).collect(Collectors.toList());
                for (ColumnInfo subColumn : subColumns) {
                    infos.remove(subColumn);
                }
                List<MateNode> childNodes = generateTreePathSchema(subColumns);
                currentNode.setChildNodes(childNodes);
                //将当前的object或者array添加到节点中去
                resultsNode.add(currentNode);
            }else{
                resultsNode.add(currentNode);
            }
        }
        return resultsNode;
    }
}
