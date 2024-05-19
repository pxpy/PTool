# PTool
## 注意
所有功能需要在idea建立索引以后,可以使用前菜单会置灰或者隐藏

![index](./doc/common/indexing.jpg)

## 安装

### 方法一
在插件市场搜索PTool
![index](./doc/common/install.png)

### 方法二
在release中下载zip文件，本地安装
![index](./doc/common/installformzip.png)

## 1. swagger-annotation-generator
可以为整个项目的controller和实体类增添swagger注解
IDEA Plugin -Generate swagger annotation

* 如果目标类上有@RestController或@Controller注解,则生成对应controller swagger注解，否则生成model swagger注解。
* 可通过选中类名、字段名、方法名生成指定swagger注解。
* 以下注释格式可自动填充到注解value中。
```
    /**
     * test
     * @desc test
     * @describe test
     * @description test
     */
    @ApiModelProperty("test test test test")
    private String test1;

    // test
    @ApiModelProperty("test")
    private String test2;

    @ApiModelProperty("")
    private String test3;
```
### 使用方法
1. 单个类，右键generate-SwaggerAnnotation
2. 批量生成，菜单栏-tool-PTool


### TODO
1. 没有注解或者注释的，调用翻译api生成注解
2. 多语言
3. 配置页面
4. swagger2注解转换为swagger3

### bug
1. 类中有重复的文档注释，会增加两条注解
2. 注释中有双引号有问题

## 本地开发
jdk 17
gradle
启用项目
Tasks--intellij--runIde