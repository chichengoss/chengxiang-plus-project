package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

      List<MediaProcess> selectListBySherdIndex(@Param("shardIndex") int shardIndex, @Param("shardTotal")int shardTotal, @Param("count")int count);

       @Select("SELECT * FROM `media_process` t WHERE t.id%#{shardTotal} = #{shardIndex}   AND t.fail_count < 3 AND t.status IN (1,3) limit #{count};")
      List<MediaProcess> selectListBySherdIndex1(@Param("shardIndex") int shardIndex, @Param("shardTotal")int shardTotal, @Param("count")int count);

//      @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = '1' or t.status = '3') and t.fail_count < 3 limit #{count}")
//      List<MediaProcess> selectListByShardIndex1(@Param("shardTotal") int shardTotal,@Param("shardIndex") int shardIndex,@Param("count") int count);



    /**
     * 开启一个任务，乐观锁
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);

}
