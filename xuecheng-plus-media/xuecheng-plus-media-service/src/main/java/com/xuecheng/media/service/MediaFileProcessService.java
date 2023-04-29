package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {
    /**
     * 查询待处理的mediaprocess
     * @param shardIndex 选择余数是几的处理（分片序号）
     * @param shardTotal 被除数（分片总数）
     * @param count 总共取前多少个处理
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);



    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);


    /**
     * @description 保存任务结果，更新任务的处理状态
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
