package com.kx.app.controller;


import com.kx.app.MinIOConfig;
import com.kx.common.enums.FileTypeEnum;
import com.kx.common.enums.UserInfoModifyType;
import com.kx.common.result.GraceJSONResult;
import com.kx.common.result.ResponseStatusEnum;
import com.kx.common.utils.MinIOUtils;
import com.kx.service.base.BaseInfoProperties;
import com.kx.service.data.bo.UpdatedUserBO;
import com.kx.service.data.pojo.Users;
import com.kx.service.data.vo.UsersVO;
import com.kx.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;
    @Autowired
    private MinIOConfig minIOConfig;
    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception {

        Users user = userService.getUser(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        Integer myFollowsCounts = 0;// 我的关注博主总数量
        Integer myFansCounts = 0;// 我的粉丝总数
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;//获赞总数
        //2.1我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        usersVO.setMyFollowsCounts(myFollowsCounts);

        //2.2我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
        usersVO.setMyFansCounts(myFansCounts);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
//        String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

//        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
//            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
//        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;

        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                          @RequestParam Integer type) throws Exception {

        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);

        return GraceJSONResult.ok(newUserInfo);
    }

    @ApiOperation(value = "修改用户头像/背景图")//接口名
    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception {
        //1.校验前端传的参数:文件类型 :1:头像 2:背景图
        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String fileName = file.getOriginalFilename();
        //2.上传文件到服务器
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());
        //3. 修改图片地址到数据库
        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;

        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);

        if (type == FileTypeEnum.BGIMG.type) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        Users users = userService.updateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(users);
    }
}
