package com.jiangzz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jangzz.JSONXMLSchemaUtils;
import org.junit.Test;
import java.util.List;

public class TestJSONXMLColumnUtils {
    String xmlContent="<message><id>1</id><name>zhangsan</name><sex>true</sex><store><books><book><category>reference</category><author>Nigel Rees</author><title>Sayings of the Century</title><price>8.95</price></book><book><category>reference</category><author>Nigel Rees</author><title>Sayings of the Century</title><price>9.95</price></book></books><bicycles><color>yellow</color><price>19.95</price></bicycles></store></message>";
    String jsonContent="{\"video\":{\"id\":\"29BA6ACE7A9427489C33DC5901307461\",\"title\":\"体验课01\",\"desp\":\"描述信息\",\"tags\":[\"用户\",\"旅游\"],\"cards\":[1,2,3,10.7],\"pet\":{\"nmae\":\"花花\",\"hobbies\":[\"TV\",\"GAME\"]},\"duration\":503,\"category\":\"07AD1E11DBE6FDFC\",\"image\":\"0.jpg\",\"imageindex\":0,\"SmallImages\":[{\"index\":0,\"url\":\"0.jpg\",\"other\":[{\"id\":11,\"name\":\"xxx\"}]}]}}";
    @Test
    public void testXMLPATHSchema() throws Exception {
        System.out.println(xmlContent);
        List<JSONXMLSchemaUtils.ColumnInfo> columnInfos = JSONXMLSchemaUtils.generateXMLPathSchema(xmlContent,false);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }
    @Test
    public void testXMLTreeSchema() throws Exception {
        System.out.println(xmlContent);
        List<JSONXMLSchemaUtils.MateNode> columnInfos = JSONXMLSchemaUtils.generateXMLTreeSchema(xmlContent,false);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }
    @Test
    public void testJSONPATHSchema() throws Exception {
        List<JSONXMLSchemaUtils.ColumnInfo>   columnInfos= JSONXMLSchemaUtils.generateJSONPathSchema(jsonContent);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }

    @Test
    public void testJSONTreeSchema() throws Exception {
        System.out.println(jsonContent);
        List<JSONXMLSchemaUtils.MateNode>    columnInfos= JSONXMLSchemaUtils.generateJSONTreeSchema(jsonContent);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }
}
