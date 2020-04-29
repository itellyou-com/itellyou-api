package com.itellyou.model.user;

import com.alibaba.fastjson.annotation.JSONField;
import com.itellyou.util.annotation.JSONDefault;
import com.itellyou.util.serialize.IpLongSerializer;
import com.itellyou.util.serialize.TimestampSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JSONDefault(includes = "base")
/**
 * 用户第三方账号绑定类
 */
public class UserThirdAccountModel {
    @JSONField(label = "base")
    private Long userId;//用户编号，与 user_info -> id 对应
    @JSONField(label = "base")
    private UserThirdAccountType type;//第三方账号类型
    @JSONField(label = "base")
    private String key;//第三方账号唯一id
    @JSONField(label = "base")
    private String name;//第三方账号名称
    @JSONField(label = "base")
    private String avatar;//第三方账号头像
    @JSONField(label = "base")
    private String home;//第三方账号主页链接
    @JSONField(label = "base")
    private Long star;//第三方账号关注人数
    @JSONField(serializeUsing = TimestampSerializer.class,label = "base")
    private Long createdTime=0l;//创建时间
    @JSONField(serializeUsing = IpLongSerializer.class)
    private Long createdIp;//创建ip
}
