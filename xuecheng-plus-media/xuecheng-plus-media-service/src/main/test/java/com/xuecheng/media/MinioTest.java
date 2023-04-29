package com.xuecheng.media;

import io.minio.*;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MinioTest {
 static MinioClient minioClient =
         MinioClient.builder()
                 .endpoint("http://192.168.101.65:9000")
                 .credentials("minioadmin", "minioadmin")
                 .build();
 @Test
 public  void test_upload(){
       try {
        UploadObjectArgs testbucket = UploadObjectArgs.builder()
                .bucket("testbucket")//确定桶
     //                    .object("test001.mp4")
                .object("001/test001.png")//添加子目录，指定上传后的对象名字
                .filename("E:\\Exhibition_DATA\\轮廓图\\人工1.png")//指定本地文件，要上传什么文件
              //  .contentType("video/mp4")//默认根据扩展名确定文件内容类型，也可以指定
                .build();
        minioClient.uploadObject(testbucket);
        System.out.println("上传成功");
       } catch (Exception e) {
        e.printStackTrace();
        System.out.println("上传失败");
       }
 }

    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("001/test001.png").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }


    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("001/test001.png").build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\1.png"));
        ) {
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传分块测试
     */
    @Test
    public void uploadChunk() throws Exception {

        for(int i = 0;i < 2;++i){
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")//确定桶
                    //                    .object("test001.mp4")

                    .filename("C:\\Users\\Administrator\\Desktop\\chunk\\"+i)//指定本地文件，要上传什么文件
                    //  .contentType("video/mp4")//默认根据扩展名确定文件内容类型，也可以指定
                    .object("chunk/" + i)//添加子目录，指定上传后的对象名字
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传分块"+i+"成功");
        }
    }

    @Test
    public void uploadObjects(){

    }

    /**
     * 合并分块
     */
    @Test
    public void testMerge() throws Exception{

        List<ComposeSource> list = new ArrayList<>();

        for(int i = 0;i < 2;++i) {
            ComposeSource testbucket = ComposeSource
                    .builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)
                    .build();

            list.add(testbucket);

        }

//可以用Stream流方法替换原来代码
//        List<ComposeSource> testbucket1 = Stream.iterate(0, i -> ++i).limit(2).
//                map(item ->
//                        ComposeSource
//                                .builder()
//                                .bucket("testbucket")
//                                .object("chunk/" + item)
//                                .build()
//                ).collect(Collectors.toList());


        //合并的文件位置等信息
        ComposeObjectArgs testbucket = ComposeObjectArgs.builder().
                bucket("testbucket").
                object("merge01.mp4").
                sources(list).
                build();
        
       //合并
        minioClient.composeObject(testbucket);
        

        

    }

    @Test
    public  void test() throws Exception{
        String path = "C:\\Users\\Administrator\\Desktop\\会议结稿.txt";

      // InputStream inputStream = new FileInputStream(path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\Administrator\\Desktop\\会议结稿222.txt"));
      //  FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\Administrator\\Desktop\\会议结稿111.txt"));

//       bufferedWriter.write();
//        IOUtils.copy(bufferedReader,bufferedWriter);
//        inputStream.close();
//        outputStream.close();



    }


}
