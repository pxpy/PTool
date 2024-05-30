package me.panxin.jumptocontroller.modulea.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author PanXin
 * @version $ Id: UserVo, v 0.1 2023/05/22 20:38 PanXin Exp $
 */
@ApiModel(description = "用户 vo")
public class UserVO {

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String userName;

    /** 用户 ID */
    private String userId;

    @ApiModelProperty("年龄")
    private int age; // 年龄
}