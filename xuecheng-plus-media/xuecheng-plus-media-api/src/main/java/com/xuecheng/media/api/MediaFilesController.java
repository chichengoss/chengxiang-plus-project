package com.xuecheng.media.api;


import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {


  @Autowired
  MediaFileService mediaFileService;


 @ApiOperation("媒资列表查询接口")
 @PostMapping("/files")
 public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
  Long companyId = 1232141425L;
  return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);

 }
 //http://localhost:8601/api/media/upload/coursefile
    @ApiOperation("上传文件接口")
    @PostMapping(value = "upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata) throws IOException {
     //springmvc上传文件会用MultipartFile类型
        Long companyId = 1232141425L;
        UploadFileParamDto uploadFileParamDto = new UploadFileParamDto();
        uploadFileParamDto.setFilename(filedata.getOriginalFilename());
        uploadFileParamDto.setFileSize(filedata.getSize());
        uploadFileParamDto.setFileType("001001");

        File minio = File.createTempFile("minio", ".temp");
        filedata.transferTo(minio);

       return mediaFileService.uploadFile(companyId,minio.getAbsolutePath(),uploadFileParamDto);

    }

   // http://localhost:8601/api/media/upload/checkfile
}
