package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 测试大文件上传方法
 */
public class BigFileTest {

    /**
     * 分块
     * @throws Exception
     */
    @Test
    public void testChunk() throws Exception{
      //  C:\Users\Administrator\Desktop\Suzhou Museum.mp4

        File sourcefile = new File("C:\\Users\\Administrator\\Desktop\\Suzhou Museum.mp4");
        String chunkFilePath = "C:\\Users\\Administrator\\Desktop\\chunk\\";

        int chunkSize = 1024*1024*5;//

        //分块文件大小
        int chunkNum = (int)Math.ceil(sourcefile.length() * 1.0 / chunkSize);

        //分块文件读取流
         RandomAccessFile r = new RandomAccessFile(sourcefile, "r");

         //缓冲区
         byte[] bytes = new byte[1024];//1024 * 8 位

        for(int i = 0;i < chunkNum;++i){
            File file = new File(chunkFilePath + i);//存储位置

            //分块文件写入流
            RandomAccessFile rw = new RandomAccessFile(file, "rw");
            int len = -1;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
                if(file.length() >= chunkSize) break;
            }
            rw.close();
        }
        r.close();

    }

    /**
     * 合并
     */
    @Test
    public void testMerge() throws Exception{
        //文件目录
       File chunkFiles = new File("C:\\Users\\Administrator\\Desktop\\chunk\\");

       //源文件
        File sourcefile = new File("C:\\Users\\Administrator\\Desktop\\Suzhou Museum.mp4");

        //合并后的文件
        File sourcefile1 = new File("C:\\Users\\Administrator\\Desktop\\test.mp4");

        //取出所有文件快
        File[] files = chunkFiles.listFiles();
        List<File> files1 = Arrays.asList(files);
        Collections.sort(files1,
                (File f1,File f2)->{
                    if(f1.getName().compareTo(f2.getName()) > 0){
                        return  1;
                    }
                    return -1;
                });

        RandomAccessFile rw = new RandomAccessFile(sourcefile1, "rw");
        byte[] bytes = new byte[1024];
        for (File tpf : files1) {
            RandomAccessFile r = new RandomAccessFile(tpf,"r");
            int len;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
            }
           r.close();
        }
        rw.close();


        //校验
        FileInputStream fileInputStream = new FileInputStream(sourcefile);
        FileInputStream fileInputStream1 = new FileInputStream(sourcefile1);

        String s1 = DigestUtils.md5Hex(fileInputStream1);
        String s = DigestUtils.md5Hex(fileInputStream);
        System.out.println(s);
        System.out.println(s1);



    }

}
