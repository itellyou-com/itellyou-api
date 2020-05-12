package com.itellyou.model.tag;

import com.alibaba.fastjson.annotation.JSONField;
import com.itellyou.model.user.UserInfoModel;
import com.itellyou.util.serialize.IpDeserializer;
import com.itellyou.util.serialize.IpSerializer;
import com.itellyou.util.serialize.TimestampDeserializer;
import com.itellyou.util.serialize.TimestampSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagVersionModel {
    private Long id;
    private Long tagId=0l;
    private String content = "";
    private String html = "";
    private String icon = "";
    private Integer version = 0;
    private String description = "";
    private boolean isReviewed = false;
    private boolean isDisabled = false;
    private boolean isPublished = false;
    private String remark;
    private String saveType;
    @JSONField(serializeUsing = TimestampSerializer.class,deserializeUsing = TimestampDeserializer.class)
    private Long createdTime;
    @JSONField(serialize = false)
    private Long createdUserId;
    private UserInfoModel author;
    @JSONField(serialize = false,serializeUsing = IpSerializer.class,deserializeUsing = IpDeserializer.class)
    private Long createdIp;
    @JSONField(serializeUsing = TimestampSerializer.class,deserializeUsing = TimestampDeserializer.class)
    private Long updatedTime;
    @JSONField(serialize = false)
    private Long updatedUserId;
    @JSONField(serialize = false,serializeUsing = IpSerializer.class,deserializeUsing = IpDeserializer.class)
    private Long updatedIp;
}
