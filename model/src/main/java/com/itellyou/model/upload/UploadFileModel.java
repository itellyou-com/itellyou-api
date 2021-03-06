package com.itellyou.model.upload;

import com.alibaba.fastjson.annotation.JSONField;
import com.itellyou.util.CacheEntity;
import com.itellyou.util.annotation.JSONDefault;
import com.itellyou.util.serialize.EnumSerializer;
import com.itellyou.util.serialize.IpDeserializer;
import com.itellyou.util.serialize.IpSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONDefault(includes = "base")
public class UploadFileModel implements CacheEntity {
    @JSONField(label = "base")
    private String key;
    @JSONField(label = "base")
    private String name;
    @JSONField(label = "base")
    private String extname;
    @JSONField(label = "base")
    private String domain;
    @JSONField(label = "base")
    public String getUrl(){
        return this.domain + "/" + this.key;
    }
    private String bucket;
    @JSONField(serializeUsing = EnumSerializer.class , deserializeUsing = EnumSerializer.class)
    private UploadSource source;
    @JSONField(label = "base")
    private Long size;
    @JSONField(label = "base")
    private LocalDateTime createdTime;
    private Long createdUserId=0l;
    @JSONField(serializeUsing = IpSerializer.class,deserializeUsing = IpDeserializer.class)
    private Long createdIp=0l;

    @Override
    public String cacheKey() {
        return key;
    }
}
