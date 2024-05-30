package me.panxin.jumptocontroller.modulea.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author PanXin
 * @version $ Id: UserVo, v 0.1 2023/05/22 20:38 PanXin Exp $
 */
@ApiModel(description = "用户 vo")
public class UserPO implements Serializable {


    @ApiModelProperty("串行版 UID")
    private static final long serialVersionUID = 2895569737725672934L;

    @ApiModelProperty("主键")
    private Integer id;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("年龄")
    private int age; // 年龄
}