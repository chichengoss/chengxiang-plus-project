package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    public List<CourseTeacher> searchCourseTeacher(Long companyId,Long id);

    public  CourseTeacher addCourseTeacher(Long companyId,CourseTeacher courseTeacher);

   // public  CourseTeacher updateCourseTeacher(Long companyId,CourseTeacher courseTeacher);

    public  void deleteCourseTeacher( Long companyId ,Long courseid,Long teacherid);
}
