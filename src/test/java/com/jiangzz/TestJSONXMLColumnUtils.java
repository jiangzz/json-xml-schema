package com.jiangzz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jangzz.JSONXMLSchemaUtils;
import org.junit.Test;
import java.util.List;

public class TestJSONXMLColumnUtils {
    String xmlContent="<?xml version=\"1.0\" encoding=\"UTF-8\" ?><message><id>1</id><name>zhangsan</name><sex>true</sex><store><books><book><category>reference</category><author>Nigel Rees</author><title>Sayings of the Century</title><price>8.95</price></book><book><category>reference</category><author>Nigel Rees</author><title>Sayings of the Century</title><price>9.95</price></book></books><bicycles><color>yellow</color><price>19.95</price></bicycles></store></message>";
    String jsonContent="{\"id\":1,\"name\":\"jiangzz\",\"address\":{\"city\":\"beijing\",\"street\":\"东北旺西路\"},\"salary\":20000.8,\"sex\":true,\"friends\":[{\"id\":1,\"name\":\"张三\"},{\"id\":1,\"name\":\"张三\"}]}";

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
    /**
     * {
     *     "id": 1,
     *     "name": "jiangzz",
     *     "address": {
     *         "city": "beijing",
     *         "street": "东北旺西路"
     *     },
     *     "salary": 20000.8,
     *     "sex": true,
     *     "friends": [
     *         {
     *             "id": 1,
     *             "name": "张三"
     *         },
     *         {
     *             "id": 1,
     *             "name": "张三"
     *         }
     *     ]
     * }
     *
     * 返回
     * [
     *     {
     *         "columnPath": "address",
     *         "columnType": "object"
     *     },
     *     {
     *         "columnPath": "address.city",
     *         "columnType": "string"
     *     },
     *     {
     *         "columnPath": "address.street",
     *         "columnType": "string"
     *     },
     *     {
     *         "columnPath": "friends",
     *         "columnType": "array"
     *     },
     *     {
     *         "columnPath": "friends.id",
     *         "columnType": "int"
     *     },
     *     {
     *         "columnPath": "friends.name",
     *         "columnType": "string"
     *     },
     *     {
     *         "columnPath": "id",
     *         "columnType": "int"
     *     },
     *     {
     *         "columnPath": "name",
     *         "columnType": "string"
     *     },
     *     {
     *         "columnPath": "salary",
     *         "columnType": "float"
     *     },
     *     {
     *         "columnPath": "sex",
     *         "columnType": "boolean"
     *     }
     * ]
     * @throws Exception
     */
    @Test
    public void testJSONPATHSchema() throws Exception {
        System.out.println(jsonContent);
        List<JSONXMLSchemaUtils.ColumnInfo>   columnInfos= JSONXMLSchemaUtils.generateJSONPathSchema(jsonContent);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }

    /**
     * [
     *     {
     *         "name": "address",
     *         "fullPath": "address",
     *         "type": "object",
     *         "childNodes": [
     *             {
     *                 "name": "city",
     *                 "fullPath": "address.city",
     *                 "type": "string",
     *                 "childNodes": null
     *             },
     *             {
     *                 "name": "street",
     *                 "fullPath": "address.street",
     *                 "type": "string",
     *                 "childNodes": null
     *             }
     *         ]
     *     },
     *     {
     *         "name": "friends",
     *         "fullPath": "friends",
     *         "type": "array",
     *         "childNodes": [
     *             {
     *                 "name": "id",
     *                 "fullPath": "friends.id",
     *                 "type": "int",
     *                 "childNodes": null
     *             },
     *             {
     *                 "name": "name",
     *                 "fullPath": "friends.name",
     *                 "type": "string",
     *                 "childNodes": null
     *             }
     *         ]
     *     },
     *     {
     *         "name": "id",
     *         "fullPath": "id",
     *         "type": "int",
     *         "childNodes": null
     *     },
     *     {
     *         "name": "name",
     *         "fullPath": "name",
     *         "type": "string",
     *         "childNodes": null
     *     },
     *     {
     *         "name": "salary",
     *         "fullPath": "salary",
     *         "type": "float",
     *         "childNodes": null
     *     },
     *     {
     *         "name": "sex",
     *         "fullPath": "sex",
     *         "type": "boolean",
     *         "childNodes": null
     *     }
     * ]
     * @throws Exception
     */
    @Test
    public void testJSONTreeSchema() throws Exception {
        System.out.println(jsonContent);
        List<JSONXMLSchemaUtils.MateNode>    columnInfos= JSONXMLSchemaUtils.generateJSONTreeSchema(jsonContent);
        System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
    }
}
