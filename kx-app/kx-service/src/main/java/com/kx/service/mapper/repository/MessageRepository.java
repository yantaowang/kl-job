package com.kx.service.mapper.repository;

import com.kx.service.data.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//和mongodb交互的接口
@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {

    // 通过实现Repository，自定义条件查询
    List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(Long toUserId,
                                                           Pageable pageable);
//    void deleteAllByFromUserIdAndToUserIdAndMsgType();
}
