package me.panxin.plugin.idea.enums;


/**
 * Swagger 注释
 *
 * @author PanXin
 * @date 2024/05/08
 */
public enum SwaggerAnnotation {
    /**
     * RequestMapping
     */
    APIMODEL("io.swagger.annotations.ApiModel"),
    /**
     * GetMapping
     */
    API("io.swagger.annotations.Api"),
    APIOPERATION("io.swagger.annotations.ApiOperation"),
    APIMODELPROPERTY("io.swagger.annotations.ApiModelProperty"),
    SCHEMA("io.swagger.v3.oas.annotations.media.Schema"),
    PARAMETER("io.swagger.v3.oas.annotations.Parameter"),
    PARAMETERS("io.swagger.v3.oas.annotations.Parameters"),
    PARAMETERIN("io.swagger.v3.oas.annotations.enums.ParameterIn"),
    OPERATION("io.swagger.v3.oas.annotations.Operation"),
    TAG("io.swagger.v3.oas.annotations.tags.Tag");

    private final String qualifiedName;

    SwaggerAnnotation(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }


    public static SwaggerAnnotation getByShortName(String requestMapping) {
        for (SwaggerAnnotation springRequestAnnotation : SwaggerAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().endsWith(requestMapping)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public static boolean isSwaggerAnnotation(String annotationName){
        for (SwaggerAnnotation springRequestAnnotation : SwaggerAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }
    public String getQualifiedName() {
        return qualifiedName;
    }
}