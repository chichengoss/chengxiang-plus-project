package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTest {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    public  void test(){
        String id = "1";
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes(id);
        System.out.println(list);
    }
}
