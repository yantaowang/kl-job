package com.kx.service.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

//lombok的四个注解
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Stu {

    private String name;
    private Integer age;

}
