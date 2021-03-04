# json-xml-schema
> 思路来自：https://github.com/lukas-krecan/json2xml 
            https://github.com/jiangzz/json2xml - 已经fork备份
```xml
<dependency>
    <groupId>net.javacrumbs</groupId>
    <artifactId>json-xml</artifactId>
    <version>4.2</version><!-- for Jackson >= 2.0 -->
</dependency>
```
schema解析
案例：
例如有以下json数据
```json
{
    "id": 1,
    "name": "jiangzz",
    "address": {
        "city": "beijing",
        "street": "东北旺西路"
    },
    "salary": 20000.8,
    "sex": true,
    "friends": [
        {
            "id": 1,
            "name": "张三"
        },
        {
            "id": 1,
            "name": "张三"
        }
    ]
}
```
获取该JSON的Tree Schema
```java
[
    {
        "name": "address",
        "fullPath": "address",
        "type": "object",
        "childNodes": [
            {
                "name": "city",
                "fullPath": "address.city",
                "type": "string",
                "childNodes": null
            },
            {
                "name": "street",
                "fullPath": "address.street",
                "type": "string",
                "childNodes": null
            }
        ]
    },
    {
        "name": "friends",
        "fullPath": "friends",
        "type": "array",
        "childNodes": [
            {
                "name": "id",
                "fullPath": "friends.id",
                "type": "int",
                "childNodes": null
            },
            {
                "name": "name",
                "fullPath": "friends.name",
                "type": "string",
                "childNodes": null
            }
        ]
    },
    {
        "name": "id",
        "fullPath": "id",
        "type": "int",
        "childNodes": null
    },
    {
        "name": "name",
        "fullPath": "name",
        "type": "string",
        "childNodes": null
    },
    {
        "name": "salary",
        "fullPath": "salary",
        "type": "float",
        "childNodes": null
    },
    {
        "name": "sex",
        "fullPath": "sex",
        "type": "boolean",
        "childNodes": null
    }
]
```
获取属性的path scahme
```java
 List<JSONXMLSchemaUtils.ColumnInfo>   columnInfos= JSONXMLSchemaUtils.generateJSONPathSchema(jsonContent);
 System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
```
```text
[
    {
        "columnPath": "address",
        "columnType": "object"
    },
    {
        "columnPath": "address.city",
        "columnType": "string"
    },
    {
        "columnPath": "address.street",
        "columnType": "string"
    },
    {
        "columnPath": "friends",
        "columnType": "array"
    },
    {
        "columnPath": "friends.id",
        "columnType": "int"
    },
    {
        "columnPath": "friends.name",
        "columnType": "string"
    },
    {
        "columnPath": "id",
        "columnType": "int"
    },
    {
        "columnPath": "name",
        "columnType": "string"
    },
    {
        "columnPath": "salary",
        "columnType": "float"
    },
    {
        "columnPath": "sex",
        "columnType": "boolean"
    }
]
```
有如下XML信息
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<message>
    <id>1</id>
    <name>zhangsan</name>
    <sex>true</sex>
    <store>
        <books>
            <book>
                <category>reference</category>
                <author>Nigel Rees</author>
                <title>Sayings of the Century</title>
                <price>8.95</price>
            </book>
            <book>
                <category>reference</category>
                <author>Nigel Rees</author>
                <title>Sayings of the Century</title>
                <price>9.95</price>
            </book>
        </books>
        <bicycles>
            <color>yellow</color>
            <price>19.95</price>
        </bicycles>
    </store>
</message>
```
tree schema信息
```java
 List<JSONXMLSchemaUtils.MateNode> columnInfos = JSONXMLSchemaUtils.generateXMLTreeSchema(xmlContent,false);
 System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
```
效果
```json
[
    {
        "name": "id",
        "fullPath": "id",
        "type": "int",
        "childNodes": null
    },
    {
        "name": "name",
        "fullPath": "name",
        "type": "string",
        "childNodes": null
    },
    {
        "name": "sex",
        "fullPath": "sex",
        "type": "boolean",
        "childNodes": null
    },
    {
        "name": "store",
        "fullPath": "store",
        "type": "object",
        "childNodes": [
            {
                "name": "bicycles",
                "fullPath": "store.bicycles",
                "type": "object",
                "childNodes": [
                    {
                        "name": "color",
                        "fullPath": "store.bicycles.color",
                        "type": "string",
                        "childNodes": null
                    },
                    {
                        "name": "price",
                        "fullPath": "store.bicycles.price",
                        "type": "float",
                        "childNodes": null
                    }
                ]
            },
            {
                "name": "books",
                "fullPath": "store.books",
                "type": "array",
                "childNodes": [
                    {
                        "name": "book",
                        "fullPath": "store.books.book",
                        "type": "object",
                        "childNodes": [
                            {
                                "name": "author",
                                "fullPath": "store.books.book.author",
                                "type": "string",
                                "childNodes": null
                            },
                            {
                                "name": "category",
                                "fullPath": "store.books.book.category",
                                "type": "string",
                                "childNodes": null
                            },
                            {
                                "name": "price",
                                "fullPath": "store.books.book.price",
                                "type": "float",
                                "childNodes": null
                            },
                            {
                                "name": "title",
                                "fullPath": "store.books.book.title",
                                "type": "string",
                                "childNodes": null
                            }
                        ]
                    }
                ]
            }
        ]
    }
]
```
xml path schema信息
```java
 List<JSONXMLSchemaUtils.ColumnInfo> columnInfos = JSONXMLSchemaUtils.generateXMLPathSchema(xmlContent,false);
  System.out.println(new ObjectMapper().writeValueAsString(columnInfos));
```
展示效果
```json
[
    {
        "columnPath": "id",
        "columnType": "int"
    },
    {
        "columnPath": "name",
        "columnType": "string"
    },
    {
        "columnPath": "sex",
        "columnType": "boolean"
    },
    {
        "columnPath": "store",
        "columnType": "object"
    },
    {
        "columnPath": "store.bicycles",
        "columnType": "object"
    },
    {
        "columnPath": "store.bicycles.color",
        "columnType": "string"
    },
    {
        "columnPath": "store.bicycles.price",
        "columnType": "float"
    },
    {
        "columnPath": "store.books",
        "columnType": "array"
    },
    {
        "columnPath": "store.books.book",
        "columnType": "object"
    },
    {
        "columnPath": "store.books.book.author",
        "columnType": "string"
    },
    {
        "columnPath": "store.books.book.category",
        "columnType": "string"
    },
    {
        "columnPath": "store.books.book.price",
        "columnType": "float"
    },
    {
        "columnPath": "store.books.book.title",
        "columnType": "string"
    }
]
```
