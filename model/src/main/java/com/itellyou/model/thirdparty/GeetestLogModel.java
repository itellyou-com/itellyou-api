package com.itellyou.model.thirdparty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeetestLogModel implements Serializable {
    private String key;
    private GeetestClientTypeEnum type;
    private Long ip;
    private Integer status;
    private String mode;
    private Long createdUserId;
    private LocalDateTime createdTime;
}
