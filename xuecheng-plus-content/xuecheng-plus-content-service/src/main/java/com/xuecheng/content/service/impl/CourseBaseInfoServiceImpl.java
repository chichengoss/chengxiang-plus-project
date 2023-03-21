package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j

public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    /**
     *
     * @param pageParams 分页查询参数多少页
     * @param courseParamsDto 查询条件
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourselist(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //详细进行分页查询的单元测试
        //拼装查询条件
        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //根据名称模糊查询,在sql中拼接 course_base.name like '%值%'
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //根据课程审核状态查询 course_base.audit_status = ?
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        //todo:按课程发布状态查询
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()),CourseBase::getStatus,courseParamsDto.getPublishStatus());

        //创建page分页参数对象，参数：当前页码，每页记录数  limit 1,10
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, lambdaQueryWrapper);
        //数据列表
        //总记录数  List<T> items, long counts, long page, long pageSize
        List<CourseBase> items = pageResult.getRecords();
        long counts = pageResult.getTotal();

        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items,counts,pageParams.getPageNo(),pageParams.getPageSize());


        return courseBasePageResult;
        //List<T> items, long counts, long page, long pageSize

    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //参数合法性校验
        //合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//
//            XueChengPlusException.cast("课程名称为空");
//           // throw new RuntimeException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new RuntimeException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new RuntimeException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new RuntimeException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new RuntimeException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new RuntimeException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new RuntimeException("收费规则为空");
//        }



        //向表course_base写
        CourseBase courseBase = new CourseBase();
        //由于一条一条的set(get)方法太复杂，调用BeanUtils的方法，自动拷贝有的属性
        BeanUtils.copyProperties(dto,courseBase);//全拷贝，dto有值的地方没值了
        //设置dto里面没有给的属性
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认未提交
        courseBase.setAuditStatus("202002");
        //发布状态未发布
        courseBase.setStatus("203001");

        int insert = courseBaseMapper.insert(courseBase);
        if(insert <= 0){
            throw new RuntimeException("添加课程失败");
        }



        //向表course_market写
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseBase.getId());


        saveCourseMarket(courseMarket);

        //从course_base 和course_market 组合获得CourseBaseInfoDto
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoDto(courseBase.getId());


        return courseBaseInfoDto;
    }


    @Override
    public  CourseBaseInfoDto getCourseBaseInfoDto(Long id){
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase==null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null) BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        //通过courseCategoryMapper查询分类信息，将分类名称放在courseBaseInfoDto对象
        //todo：课程分类的名称设置到courseBaseInfoDto
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBase.getSt());
        if(courseCategory != null)
          courseBaseInfoDto.setMtName(courseCategory.getName());
        if(courseCategory1 != null)
          courseBaseInfoDto.setStName(courseCategory1.getName());

        return courseBaseInfoDto;

    }



    /**
     * 更新CourseMarket
     * @param courseMarketNew
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //参数合法化校验
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
               // throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }
        //插入数据
        Long id = courseMarketNew.getId();
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if(courseMarket == null){
            int insert = courseMarketMapper.insert(courseMarketNew);return  insert;

        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarket);

            int i = courseMarketMapper.updateById(courseMarket);return  i;
        }



    }


    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyid, EditCourseDto addCourseDto) {

        //根据业务逻辑校验
        //拿到课程id
        CourseBase courseBase = courseBaseMapper.selectById(addCourseDto.getCourseId());
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }

        //本机构只能修改本机构的课程
        if(!companyid.equals(courseBase.getCompanyId())){

            System.out.println(companyid+" "+courseBase.getCompanyId());
            XueChengPlusException.cast("本机构只能修改本机构的课程"+courseBase.getCompanyId() + " "+companyid);
        }

        //封装数据

        BeanUtils.copyProperties(addCourseDto,courseBase);
        //修改时间
        courseBase.setChangeDate(LocalDateTime.now());

        int i = courseBaseMapper.updateById(courseBase);
        if(i <= 0){
            XueChengPlusException.cast("修改课程失败");
        }



        //todo:更新营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        return getCourseBaseInfoDto(courseBase.getId());
    }


}
