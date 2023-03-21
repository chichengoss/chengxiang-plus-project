package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {


    /**
     * 课程分类查询
     * @param id 根节点
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
