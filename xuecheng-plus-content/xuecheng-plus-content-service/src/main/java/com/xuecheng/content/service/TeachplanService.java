package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachplanService {
    public List<TeachPlanDto> findTeachplanTree(Long courseId);

    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);
    public void deleteTeachPlan(Long teachplanid);
    public void remove(String direct,Long teachplanid);



   public TeachplanMedia associationmedia(BindTeachplanMediaDto bindTeachplanMediaDto);

   public void  deletemedia(String mediaId);
}
