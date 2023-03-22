package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CourseTeacheServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> searchCourseTeacher(Long companyId,Long id) {

        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,id);
        List<CourseTeacher> courseTeacher = courseTeacherMapper.selectList(queryWrapper);

        return courseTeacher;
    }

    @Override
    public CourseTeacher addCourseTeacher(Long companyId,CourseTeacher courseTeacher) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许向机构自己的课程中添加老师、删除老师");
        }
        if(courseTeacher.getId() == null){
            courseTeacherMapper.insert(courseTeacher);

        }else{
            courseTeacherMapper.updateById(courseTeacher);
        }

        return courseTeacherMapper.selectById(courseTeacher);

    }

//    @Override
//    public CourseTeacher updateCourseTeacher(Long companyId, CourseTeacher courseTeacher) {
//        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
//        if(!courseBase.getCompanyId().equals(companyId)){
//            XueChengPlusException.cast("只允许向机构自己的课程中添加老师、删除老师");
//        }
//        courseTeacherMapper.updateById(courseTeacher);
//        return courseTeacherMapper.selectById(courseTeacher);
//    }

    @Override
    public void deleteCourseTeacher( Long companyId,Long courseid, Long teacherid) {
        CourseBase courseBase = courseBaseMapper.selectById(courseid);
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("只允许向机构自己的课程中添加老师、删除老师");
        }
        CourseTeacher courseTeacher = courseTeacherMapper.selectById(teacherid);
        if(courseTeacher!= null) courseTeacherMapper.deleteById(courseTeacher);

    }
}
