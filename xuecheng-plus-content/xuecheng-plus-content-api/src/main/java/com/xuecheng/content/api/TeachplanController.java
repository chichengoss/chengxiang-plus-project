package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {
    @Autowired
    TeachplanService teachplanService;
    // http://localhost:8601/api/content/teachplan/133/tree-nodes
    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public List<TeachPlanDto> CourseBase(@PathVariable("courseId") Long id){//@RequestBody将json转java对象

        return teachplanService.findTeachplanTree(id);
    }



    //http://localhost:8601/api/content/teachplan
    @ApiOperation("保存课程计划")
    @PostMapping("/teachplan") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){//@RequestBody将json转java对象
          teachplanService.saveTeachPlan(saveTeachplanDto);
    }


    

}
