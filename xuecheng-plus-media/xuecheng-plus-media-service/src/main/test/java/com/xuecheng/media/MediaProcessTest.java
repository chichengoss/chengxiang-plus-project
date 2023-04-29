package com.xuecheng.media;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MediaProcessTest {
    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Test
    public void test(){

       List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySherdIndex(1, 2, 2);
       System.out.println(mediaProcesses);

      List<MediaProcess> mediaProcesses1 = mediaProcessMapper.selectListBySherdIndex1(1, 2, 2);
      System.out.println(mediaProcesses1);

    }
}
