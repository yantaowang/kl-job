package com.kx.controller;


import com.kx.MinIOConfig;
import com.kx.common.result.GraceJSONResult;
import com.kx.common.utils.MinIOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "FileController 文件上传测试的接口")
@RestController
public class FileController {

    @Autowired
    private MinIOConfig minIOConfig;

    /**
     * 在knife4j接口文档调试,上传文件即可测试
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("upload")
    public GraceJSONResult upload(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                              fileName,
                              file.getInputStream());
        //返回文件路径
        String imgUrl = minIOConfig.getFileHost()
                        + "/"
                        + minIOConfig.getBucketName()
                        + "/"
                        + fileName;

        return GraceJSONResult.ok(imgUrl);
    }
}
