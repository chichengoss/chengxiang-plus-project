package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  * @param companyId 机构id
  * @param localpath  文件本地路径
  * @param uploadFileParamDto 文件信息
  * @return
  */
 public UploadFileResultDto uploadFile(Long companyId,String localpath, UploadFileParamDto uploadFileParamDto);

 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamDto uploadFileParamDto,String bucket,String objectName);

 /**
  * 查看数据库里文件在不在
  * @param fileMd5
  * @return
  */
 public RestResponse<Boolean> checkFile( String fileMd5);

 /**
  * 查看minIO里文件在不在
  * @param fileMd5
  * @param chunkIndex
  * @return
  */
 public RestResponse<Boolean> checkChunk(String fileMd5,int chunkIndex);

 /**
  * 上传分块
  * @param fileMd5 md5
  * @param chunk 分块序号
  * @param localchunkFilePath 本地文件路径
  * @return
  */
 public RestResponse uploadchunk(String fileMd5,int chunk,String localchunkFilePath);


 /**
  * 合并分块的文件
  * @param companyId
  * @param fileMd5
  * @param chunkTotal
  * @param uploadFileParamsDto
  * @return
  */

 public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamDto uploadFileParamsDto);

 /**
  * 从minio下载文件
  * @param bucket
  * @param objectName
  * @return
  */
 public File downloadFileFromMinIO(String bucket, String objectName);



 /**
  * 上传文件到mimIO
  * @param bucket  桶
  * @param objectName 对象名
  * @param localPath 本地路径
  * @param mimeType 媒体类型
  * @return
  */
 public boolean uploadToMinIO(String bucket,String objectName,String localPath,String mimeType);


 /**
  * 得到合并后的文件的地址
  * @param fileMd5 文件id即md5值
  * @param fileExt 文件扩展名
  * @return
  */

 public String getFilePathByMd5(String fileMd5,String fileExt);

 public  String getMimeType(String extension);

 /**
  * 根据媒资id查询文件信息
  * @param mediaId
  * @return
  */
 MediaFiles getFileById(String mediaId);
}
