package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachplanService {
    public List<TeachPlanDto> findTeachplanTree(Long courseId);

    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);
    public void deleteTeachPlan(Long teachplanid);
    public void remove(String direct,Long teachplanid);
}
