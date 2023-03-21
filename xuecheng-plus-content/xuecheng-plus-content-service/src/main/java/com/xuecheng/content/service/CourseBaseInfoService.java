package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {
    /**
     * 分页查询
     * @param pageParams 分页基础信息 开始页，每页词条数
     * @param queryCourseParamsDto 审核信息（发布状态等）
     * @return
     */
    public PageResult<CourseBase> queryCourselist(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 机构id，用户登录的时候从前端获取
     * @param addCourseDto 新增课程信息
     * @return
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * 根据课程id查询课程信息
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfoDto(Long courseId);

    /**
     * 修改课程信息
     * @param companyid 机构id
     * @param addCourseDto 修改课程信息
     * @return
     */
    public CourseBaseInfoDto updateCourseBase(Long companyid,EditCourseDto addCourseDto);

}
