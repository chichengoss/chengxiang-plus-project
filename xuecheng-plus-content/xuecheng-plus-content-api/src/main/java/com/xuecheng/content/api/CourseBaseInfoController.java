package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/11 15:44
 */
@Api(value = "课程信息管理接口",tags = "课程信息管理接口") //swagger相关
@RestController//responseBody+Controller注解,作用响应接口数据
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;


     @ApiOperation("课程查询接口")
     @PostMapping("/course/list") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
   // @RequestMapping("/course/list")//支持各种http请求
    public PageResult<CourseBase> list( PageParams pageParams, @RequestBody(required = false)QueryCourseParamsDto queryCourseParamsDto){
    //两个参数分别是 base下的分页查询通用参数和model下的课程查询条件模型类(审核状态,课程名称,发布状态) @RequestBody把json转成对象，required = false表示这个参数不是必填项cou
         PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourselist(pageParams, queryCourseParamsDto);
         return  courseBasePageResult;

    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(1232141425L, addCourseDto);
        return courseBase;
    }


    @ApiOperation("根据id查询接口")
    @GetMapping("/course/{courseId}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){

        return courseBaseInfoService.getCourseBaseInfoDto(courseId);
    }

    @ApiOperation("修改课程")
    @PutMapping("/course") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public CourseBaseInfoDto modifyCourseBase(@RequestBody  EditCourseDto editCourseDto){//@RequestBody将json转java对象
         Long id = 1232141425L;
        return courseBaseInfoService.updateCourseBase(id,editCourseDto);
    }



}
