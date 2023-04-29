package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频任务处理类
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;


    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;


    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);

    /**
     * 2、视频处理任务，分片广播
     */
    @XxlJob("vedioJobHandler")
    public void vedioJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器的序号
        int shardTotal = XxlJobHelper.getShardTotal();//执行器的总数

        //确定本机cpu核数
        int processors = Runtime.getRuntime().availableProcessors();


        //查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 5);
        int size = mediaProcessList.size();

        //创建一个线程池，并行处理拿到的任务
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        size = mediaProcessList.size();
        log.debug("取出待处理视频任务{}条", size);
        if (size < 0) {
            return;
        }

        //使用的计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(
                 mediaProcess -> {

                     executorService.execute(()->{
                         try {
                             //任务id
                             long taskid = mediaProcess.getId();

                             //拿乐观锁,开启任务
                             boolean b = mediaFileProcessService.startTask(taskid);
                             if (!b) {
                                 log.debug("抢占任务失败，任务id:{}", taskid);
                                 return;
                             }
                             //执行视频转码
                             //桶
                             String bucket = mediaProcess.getBucket();
                             //minio上的文件路径
                             String miniofilePath = mediaProcess.getFilePath();
                             //下载minio视频到本地
                             File file = mediaFileService.downloadFileFromMinIO(bucket, miniofilePath);

                             //文件的fileid,也是转换后mp4文件的名称
                             String md5 = mediaProcess.getFileId();
                             if (file == null) {
                                 log.debug("下载的视频出错，任务id:{},bucket:{},objectName:{}", taskid, bucket, miniofilePath);
                                 mediaFileProcessService.saveProcessFinishStatus(taskid, "3", md5, null, "下载视频到本地失败");
                                 return;
                             }
                             //源avi视频的路径
                             String video_path = file.getAbsolutePath();

                             String mp4name = md5 + ".mp4";
                             //转码开始
                             //转换后mp4文件的路径
                             File mp4file = null;
                             try {
                                 mp4file = File.createTempFile("minio", ".mp4");
                             } catch (IOException e) {
                                 log.debug("创建临时文件异常，{}", e.getMessage());
                                 mediaFileProcessService.saveProcessFinishStatus(taskid, "3", md5, null, "创建临时文件异常");

                             }
                             String mp4path = mp4file.getAbsolutePath();

                             //创建工具类对象
                             Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4name, mp4path);
                             //开始视频转换，成功将返回success，转完的视频暂时存在了mp4path
                             String result = videoUtil.generateMp4();
                             if (!result.equals("success")) {
                                 log.debug("视频转码失败，原因:{},bucket:{},objectName:{},", result, bucket, md5);
                                 mediaFileProcessService.saveProcessFinishStatus(taskid, "3", md5, null, "视频转码失败");
                                 return;
                             }

                             //成功就上传视频到minio
                             String filename = mp4file.getName();
                             String extension = filename.substring(filename.lastIndexOf("."));
                             String objectName = mediaFileService.getFilePathByMd5(md5, extension);
                             String mimeType = mediaFileService.getMimeType(extension);
                             boolean b1 = mediaFileService.uploadToMinIO(bucket, objectName, mp4file.getAbsolutePath(), mimeType);

                             if (!b1) {
                                 log.debug("上传文件到minio失败，taskid:{},objectName:{}", taskid, objectName);
                                 mediaFileProcessService.saveProcessFinishStatus(taskid, "3", md5, null, "上传文件到minio失败");
                                 return;
                             }
                             //文件的url
                             String url = "/" + bucket + "/" + objectName;

                             // 保存任务处理结果
                             mediaFileProcessService.saveProcessFinishStatus(taskid, "2", md5, url, null);

                         }finally {
                                     //计数器减一
                                     countDownLatch.countDown();
                           }
                             }
                     );
                 });

                       //阻塞，指定最大限度的等待时间30min
                     countDownLatch.await(30, TimeUnit.MINUTES);


    }



}
