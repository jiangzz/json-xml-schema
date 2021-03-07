package com.jangzz;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.javacrumbs.json2xml.JsonXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JSONXMLSchemaUtils {
    /**
     * 生成JSON Schema内容信息，产生json-path信息路径信息
     * @param content
     * @return
     * @throws Exception
     */
    public static List<ColumnInfo> generateJSONPathSchema(String content) throws Exception {
        //将json格式数据解析为XML数据
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(content));
        DOMResult result = new DOMResult();

        JsonXmlReader jsonXmlReader = new JsonXmlReader(null, true, "root");
        transformer.transform(new SAXSource(jsonXmlReader, source), result);
        Node node = result.getNode();

        Map<String, String> pathInfo = new HashMap<>();
         parseJsonNodePath(pathInfo,node,null,"object");
         pathInfo.remove("#document");
         pathInfo.remove("#document.root");
        ArrayList<ColumnInfo> columnInfos = new ArrayList<>();
        pathInfo.keySet().stream().sorted().forEach(key->columnInfos.add(new ColumnInfo(key.replace("#document.root.",""),pathInfo.get(key))));

        return columnInfos;
    }
    private static void  parseJsonNodePath(Map<String,String> pathInfoMaps, Node currentNode,String parentPath,String  parentType){
        String currentNodeName=currentNode.getNodeName();
        NamedNodeMap attributes = currentNode.getAttributes();
        String type="object";
        if(attributes!=null){
            Node attrType = attributes.getNamedItem("type");
            if(attrType!=null){
                type=attrType.getNodeValue();
            }
        }
        if(parentPath!=null){
            if(!parentType.equals("array")){//如果父节点是Array，则不再添加该节点信息
                pathInfoMaps.put(parentPath+"."+currentNodeName,type);
            }else{//更新类型参数
                //pathInfoMaps.put(parentPath,"array<"+type+">");
            }
        }else{
            pathInfoMaps.put(currentNodeName,type);
        }

        NodeList childNodes = currentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if(!child.getNodeName().equals("#text")){
                if(parentPath!=null){
                    if(!parentType.equals("array")){
                        parseJsonNodePath(pathInfoMaps,child,parentPath+"."+currentNodeName,type);
                    }else{//如果父类是Array，路劲自动缩退
                        parseJsonNodePath(pathInfoMaps,child,parentPath,type);
                    }
                }else{
                    parseJsonNodePath(pathInfoMaps,child,currentNodeName,type);
                }

            }
        }

    }
    /**
     * 生成XML Schema内容信息，产生xml-path信息路径信息
     * @param content
     * @param includeRootElement
     * @return
     * @throws Exception
     */
    public static List<ColumnInfo> generateXMLPathSchema(String content, Boolean includeRootElement) throws Exception {

        //解析XML数据
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
        private String column;
        private String alias;
        private String type;
        private String remark;
        private boolean isPrimaryKey=false;
        private boolean isNullable=true;
        private String enumValue;
        public ColumnInfo(String column, String columnType) {
            this.column = column;
            this.alias=column.substring(column.lastIndexOf(".")+1);
            this.type = columnType;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            isPrimaryKey = primaryKey;
        }

        public boolean isNullable() {
            return isNullable;
        }

        public void setNullable(boolean nullable) {
            isNullable = nullable;
        }

        public String getEnumValue() {
            return enumValue;
        }

        public void setEnumValue(String enumValue) {
            this.enumValue = enumValue;
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
        //按照路径深度排序
        infos=infos.stream().sorted((k1,k2)->{
            if(k1.getColumn().contains(".") || k2.getColumn().contains(".")){
                return k1.getColumn().split("\\.").length-k2.getColumn().split("\\.").length;
            }else{
                return k1.getColumn().compareTo(k2.getColumn());
            }
        }).collect(Collectors.toList());

        ArrayList<MateNode> resultsNode = new ArrayList<>();
        while (infos.size()>0){
            ColumnInfo columnInfo = infos.get(0);
            infos.remove(columnInfo);
            String columnPath = columnInfo.getColumn();
            String shortName=columnPath.substring(columnPath.lastIndexOf(".")+1);
            MateNode currentNode= new MateNode(shortName,columnPath, columnInfo.getType());
            if (columnInfo.getType().equals("object") || columnInfo.getType().equals("array")){
                //找到该模型的所有子集合
                int currentDepth = columnPath.split("\\.").length;
                List<ColumnInfo> subColumns = infos.stream().filter(item -> item.getColumn().startsWith(columnPath) && item.getColumn().split("\\.").length > currentDepth).collect(Collectors.toList());
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
