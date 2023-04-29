package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        if(id != null){//修改
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{//新增
            int count = getCount(saveTeachplanDto.getCourseId(),saveTeachplanDto.getParentid());
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
        }
    }

    /**
     * 根据id删除课程，只有大章节下没有小章节才能删
     * @param teachplanid
     */
    @Transactional
    @Override
    public void deleteTeachPlan(Long teachplanid) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanid);
        if(teachplan == null){
            XueChengPlusException.cast("没有课程计划");
        }

        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid,teachplanid);
        Integer count = teachplanMapper.selectCount(teachplanLambdaQueryWrapper);
        if(count != 0){
            XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
        }

        //删除这个章节
        teachplanMapper.deleteById(teachplan);
        //删除章节关联mediea
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanid);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
        if(teachplanMedia != null) teachplanMediaMapper.deleteById(teachplanMedia);


        //将这个章节后面的章节的orderby-1
        LambdaQueryWrapper<Teachplan> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Teachplan::getCourseId,teachplan.getCourseId());
        queryWrapper1.eq(Teachplan::getParentid,teachplan.getParentid());
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper1);
        Integer orderby = teachplan.getOrderby();
        for(Teachplan tp:teachplans){
            if(tp.getOrderby() > orderby){
                tp.setOrderby(tp.getOrderby()-1);
                teachplanMapper.updateById(tp);
            }
        }

    }

    @Override
    public void remove(String direct, Long teachplanid) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanid);
        int d = 1;
        if(direct.equals("moveup")) d = -1;
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid());
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);

        int wantorder = teachplan.getOrderby() + d;
        if(wantorder == 0 || wantorder == teachplans.size()) return;
        for(Teachplan tp:teachplans){
            if(tp.getOrderby().equals(wantorder)){
                tp.setOrderby(teachplan.getOrderby());
                teachplanMapper.updateById(tp);
                teachplan.setOrderby(wantorder);
                teachplanMapper.updateById(teachplan);
                break;
            }
        }


    }

    @Transactional
    @Override
    public TeachplanMedia associationmedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }


        //删除原有记录

//        LambdaQueryWrapper<TeachplanMedia> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(TeachplanMedia::getMediaId,bindTeachplanMediaDto.getMediaId());
//        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(lambdaQueryWrapper);
//        teachplanMediaMapper.deleteById(teachplanMedia);

        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getMediaId, bindTeachplanMediaDto.getMediaId()));

        //课程id
        Long courseId = teachplan.getCourseId();
         //添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());

        teachplanMediaMapper.insert(teachplanMedia);

        return teachplanMedia;

    }

    @Override
    public void deletemedia(String mediaId) {



//        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
//        if(teachplan==null){
//            XueChengPlusException.cast("教学计划不存在");
//        }
//        Integer grade = teachplan.getGrade();
//        if(grade!=2){
//            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
//        }
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getMediaId, mediaId));

    }

    /**
     * select count(*) from teach_plan where courid = ? and parent id = ?
     * @param courseid
     * @param parentid
     * @return
     */
    public int getCount(Long courseid,Long parentid){
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId,courseid);
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid,parentid);
        Integer i = teachplanMapper.selectCount(teachplanLambdaQueryWrapper);
        return i;

    }
}
