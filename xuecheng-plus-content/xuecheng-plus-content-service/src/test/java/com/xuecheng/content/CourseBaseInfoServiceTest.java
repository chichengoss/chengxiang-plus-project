package com.xuecheng.content;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description 内容管理服务启动类
 * @date 2023/2/11 15:49
 */

@SpringBootTest
public class CourseBaseInfoServiceTest {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    /**
     * 分页查询测试
     *
     */
    @Test
    public void testCourseBaseInfoService(){
        //查询条件
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");//课程名称查询条件
        //分页参数对象
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(2L);
        pageParams.setPageSize(4L);

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourselist(pageParams, courseParamsDto);
        System.out.println(courseBasePageResult);



    }

    //http://localhost:63040/content/course/list?pageNo=1&pageSize=30
}
