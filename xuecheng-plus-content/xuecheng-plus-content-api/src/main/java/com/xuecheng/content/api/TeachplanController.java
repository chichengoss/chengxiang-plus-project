package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
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

    //http://localhost:8601/api/content/teachplan/263
    @ApiOperation("删除课程章节信息")
    @DeleteMapping("/teachplan/{teachplanid}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void saveTeachplan(@PathVariable Long teachplanid){//@RequestBody将json转java对象
        teachplanService.deleteTeachPlan(teachplanid);
    }


    //http://localhost:8601/api/content/teachplan/moveup/325
    @ApiOperation("移动课程章节信息")
    @PostMapping("/teachplan/{move}/{teachplanid}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void saveTeachplan(@PathVariable("move") String direct,@PathVariable("teachplanid") Long teachplanid){//@RequestBody将json转java对象

        teachplanService.remove(direct,teachplanid);
    }


    //http://localhost:8601/api/content/teachplan/association/media
    @ApiOperation("课程计划与媒资信息绑定")
    @PostMapping("/teachplan/association/media") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){//@RequestBody将json转java对象

        teachplanService.associationmedia(bindTeachplanMediaDto);

    }

    //http://localhost:8601/api/content/teachplan/association/media/null/89c10b5fbac406e1cebcb13fad143dd0
    @ApiOperation("删除课程计划与媒资信息绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}") //postMapping用集合接受参数必须要用@Param注解修饰在参数前
    public void deleteMedia(@PathVariable("mediaId") String mediaId){//@RequestBody将json转java对象

        teachplanService.deletemedia(mediaId);

    }



}
