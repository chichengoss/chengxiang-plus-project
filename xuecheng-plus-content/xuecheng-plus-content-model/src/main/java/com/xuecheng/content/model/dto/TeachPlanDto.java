package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 课程计划信息模型
 */
@Data
@ToString
public class TeachPlanDto extends Teachplan {
   private List<Teachplan> teachPlanTreeNodes;

   private TeachplanMedia teachplanMedia;
}
