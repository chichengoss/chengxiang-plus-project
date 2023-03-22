package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程教师信息管理接口",tags = "课程教师信息管理接口") //swagger相关
@RestController//responseBody+Controller注解,作用响应接口数据
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;
//    http://localhost:8601/api/content/courseTeacher/list/1
    @ApiOperation("查询课程教师信息")
    @GetMapping("/courseTeacher/list/{courseId}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public List<CourseTeacher> searchCourseTeacher(@PathVariable("courseId") Long id){//@RequestBody将json转java对象
        Long companyId = 1232141425L;
        return courseTeacherService.searchCourseTeacher(companyId,id);
    }



   // http://localhost:8601/api/content/courseTeacher
   @ApiOperation("添加和修改教师信息")
   @PostMapping ("/courseTeacher") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
   public CourseTeacher searchCourseTeacher(@RequestBody CourseTeacher courseTeacher){//@RequestBody将json转java对象
       Long companyId = 1232141425L;
       return courseTeacherService.addCourseTeacher(companyId,courseTeacher);
   }
//    @ApiOperation("修改教师信息")
//    @PutMapping ("/courseTeacher") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
//    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher){//@RequestBody将json转java对象
//        Long companyId = 1232141425L;
//        return courseTeacherService.updateCourseTeacher(companyId,courseTeacher);
//    }
    @ApiOperation("删除教师信息")
    @DeleteMapping ("/courseTeacher/course/{courseid}/{teacherid}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void  deteleCourseTeacher(@PathVariable Long courseid,@PathVariable Long teacherid){//@RequestBody将json转java对象
        Long companyId = 1232141425L;

        courseTeacherService.deleteCourseTeacher(companyId,courseid,teacherid);
    }


}
