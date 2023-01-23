package com.kx.service.data.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {
    private Long id;
    private Long vlogerId;
    private String url;
    private String cover;
    private String title;
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
    private Integer contentType;
}