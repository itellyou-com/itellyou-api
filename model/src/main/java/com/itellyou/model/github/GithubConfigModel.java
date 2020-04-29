package com.itellyou.model.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubConfigModel {
    private String id;
    private String secret;
    private String gateway;
    private String redirectUri;
}
