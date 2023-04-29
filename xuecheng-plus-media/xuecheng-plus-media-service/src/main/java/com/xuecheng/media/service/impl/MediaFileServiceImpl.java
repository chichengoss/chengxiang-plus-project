package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaProcessMapper mediaProcessMapper;



  @Value("${minio.bucket.files}")
  private String bucket_mediafiles;

  @Value("${minio.bucket.videofiles}")
  private String bucket_video;

//  @Value("${filetype[0]}")只在application中配置自定属性
//  private String aviType ;

//   @Autowired
//   Filetype filetype;
//  private String aviType = "";

 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  
  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }




 @Override
 public UploadFileResultDto uploadFile(Long companyId, String localpath, UploadFileParamDto uploadFileParamDto) {
     //将文件上传到minio
           //文件名
          String filename = uploadFileParamDto.getFilename();
          //1根据文件名拿到mimeType
          String extention = filename.substring(filename.lastIndexOf("."));
          String mimeType = getMimeType(extention);
          //拿到时间
          String defaultFolderPath = getDefaultFolderPath();
           //md5
          String fileMd5 = getFileMd5(new File(localpath));
          //拼接
          String objectName = defaultFolderPath+fileMd5+extention;

          boolean upload = uploadToMinIO(bucket_mediafiles, objectName, localpath, mimeType);

          if(!upload){
              XueChengPlusException.cast("上传文件失败");
          }
          //路径存在数据库
           MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamDto, bucket_mediafiles, objectName);
           if(mediaFiles == null){
               XueChengPlusException.cast("保存文件信息失败");
           }
          //准备返回的对象
               UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
               BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);



     //将文件信息保存在数据库
     return uploadFileResultDto;
 }
    @Transactional
    @Override
    public   MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamDto uploadFileParamDto,String bucket,String objectName){

        //查看文件在不在数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamDto,mediaFiles);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setId(fileMd5);
            //url

            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditMind("002003");

            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
            //加入数据库的待处理事件表，如果是avi视频
            //通过mimeType或者扩展名判断文件类型，向MediaProcess表插入记录
             addWaitingTask(mediaFiles);

        }
        return mediaFiles;
    }


    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //通过mimeType或者扩展名判断文件类型
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //判断
        if(mimeType.equals("video/x-msvideo")){
     // if(mimeType.equals(aviType)){
     //   if(mimeType.equals(filetype.getAvi())){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            //状态是未处理
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);//失败次数默认0
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }


    }



        //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
  * 上传文件到mimIO
  * @param bucket  桶
  * @param objectName 对象名
  * @param localPath 本地路径
  * @param mimeType 媒体类型
  * @return
  */
 public boolean uploadToMinIO(String bucket,String objectName,String localPath,String mimeType){
   UploadObjectArgs testbucket = null;
   try {
      testbucket = UploadObjectArgs.builder()
            .bucket(bucket)//确定桶
            //                    .object("test001.mp4")
            .object(objectName)//添加子目录，指定上传后的对象路径
            .filename(localPath)//指定本地文件，要上传什么文件
            .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
            .build();
         minioClient.uploadObject(testbucket);
         return true;
   } catch (Exception e) {
       e.printStackTrace();
       log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
   }
   return false;

 }


 public  String getMimeType(String extension){
     if(extension == null){
         extension = "";
     }

     ContentInfo mimeTypeMatch = ContentInfoUtil.findExtensionMatch(extension);
     String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mineType(未知类型)
     if(mimeTypeMatch != null){
      mimeType = mimeTypeMatch.getMimeType();
     }
     return  mimeType;
 }

    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }


    /**
     * 查文件在不在缓存
     * @param fileMd5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //查询minIO
        if(mediaFiles != null){
            GetObjectArgs getObjectArgs = GetObjectArgs.
                    builder().
                    bucket(mediaFiles.getBucket()).
                    object(mediaFiles.getFilePath()).
                    build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if(inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return RestResponse.success(false);
    }


    /**
     * //查看视频分块在不在数据库
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {


        //查询minIO

            GetObjectArgs getObjectArgs = GetObjectArgs.
                    builder().
                    bucket(bucket_video).
                    object(getChunkFileFolderPath(fileMd5) + chunkIndex).
                    build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if(inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        return RestResponse.success(false);
    }

    /**
     * 上传分块
     * @param fileMd5 md5
     * @param chunk 分块序号
     * @param localchunkFilePath 本地文件路径
     * @return
     */
    @Override
    public RestResponse uploadchunk(String fileMd5, int chunk, String localchunkFilePath) {
        boolean b = uploadToMinIO(bucket_video,
                getChunkFileFolderPath(fileMd5) + chunk,
                localchunkFilePath,
                getMimeType(null));
        if(!b){
            return RestResponse.validfail(false,"上传文件分块失败");
        }
        return RestResponse.success(b);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamDto uploadFileParamsDto) {
        //=====获取分块文件路径=====
        List<String> pathlist = new ArrayList<>();

        //拿到左右路径
        Stream.iterate(0,i -> ++i).limit(chunkTotal).forEach(
                i-> pathlist.add(
                        getChunkFileFolderPath(fileMd5)+i
                ) );
        List<ComposeSource> sourceObjectList = new ArrayList<>();
        //根据所有路径取到ComposeSource
        pathlist.forEach( item->
                   sourceObjectList.add(
                           ComposeSource
                                   .builder()
                                   .bucket(bucket_video)
                                   .object(item)
                                   .build() ));


 /*         //以上两步一次解决
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(
                i -> ComposeSource
                        .builder()
                        .bucket("testbucket")
                        .object(get + i)
                        .build()
        ).collect(Collectors.toList());
     */
        //=====合并=====

            String fileName = uploadFileParamsDto.getFilename();
            String extName = fileName.substring(fileName.lastIndexOf("."));
            String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        try {
               //合并的文件位置等信息
            ComposeObjectArgs testbucket = ComposeObjectArgs.builder().
                    bucket(bucket_video).
                    object(mergeFilePath).
                    sources(sourceObjectList).
                    build();
            minioClient.composeObject(testbucket);

            log.debug("合并文件成功:{}",mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");

        }


        //验证md5
        File miniofile = downloadFileFromMinIO(bucket_video, mergeFilePath);
        if(miniofile == null){
            log.debug("下载合并后文件失败,mergeFilePath:{}",mergeFilePath);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }

        try(InputStream inputStream = new FileInputStream(miniofile))
        {
            String md5 = DigestUtils.md5Hex(inputStream);
            if(!md5.equals(fileMd5)){
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(miniofile.length());

        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");

        }finally {
            if(miniofile!=null){
                miniofile.delete();
            }
        }

        //文件请入库
        currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,bucket_video,mergeFilePath);

//=====清除分块文件=====
        clearChunkFiles(getChunkFileFolderPath(fileMd5),chunkTotal);
        return RestResponse.success(true);
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){
       try {
           List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(
                   i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i)))
           ).collect(Collectors.toList());

           RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket("video").
                   objects(deleteObjects).build();

           Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);

           results.forEach(
                   item -> {
                       DeleteError deleteError = null;
                       try {
                           deleteError = item.get();
                       } catch (Exception e) {
                           e.printStackTrace();
                           log.error("清楚分块文件失败,objectname:{}", deleteError.objectName(), e);
                       }

                   });
       }catch (Exception e) {
           e.printStackTrace();
           log.error("清除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
       }



    }




    /**
     * 从minio下载文件  暴露成接口给VideoTask调用
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */

    public File downloadFileFromMinIO(String bucket,String objectName){
        FileOutputStream outputStream = null;
        FilterInputStream inputStream = null;
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try {
            inputStream = minioClient.getObject(getObjectArgs);
            //创建临时文件
            File  minioFile = File.createTempFile("minio",".merge");
            outputStream= new FileOutputStream(minioFile);
            IOUtils.copy(inputStream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */

    public String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }



}



