package com.xuecheng.content.service.impl;


import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes(id);

        CourseCategoryTreeDto root = null;
        for(CourseCategoryTreeDto tp:list){
            if(tp.getId().equals(id)) {
                root = tp;
                break;
            }
        }
        //拿到根节点的CourseCategoryTreeDto
        List<CourseCategoryTreeDto> re = dfs(list,root).getChildrenTreeNodes();
        return re;
    }
    public CourseCategoryTreeDto dfs(List<CourseCategoryTreeDto> list,CourseCategoryTreeDto root){

        if(root == null) return null;
        List<CourseCategoryTreeDto> childrenTreeNodes = new ArrayList<>();
        for(CourseCategoryTreeDto tp : list){
            //拿到一个孩子节点
            if(tp.getParentid().equals(root.getId())){
                CourseCategoryTreeDto cd = dfs(list,tp);
                if(cd != null) childrenTreeNodes.add(cd);
            }
            //拿到自己信息
//            if(root.getId().equals(tp.getId())){
//                root.setParentid(tp.getParentid());
//                root.setIsLeaf(tp.getIsLeaf());
//                root.setLabel(tp.getLabel());
//                root.setName(tp.getName());
//                root.setIsLeaf(tp.getIsLeaf());
//                root.setIsShow(tp.getIsShow());
//                root.setOrderby(tp.getOrderby());
//            }
        }
        if(childrenTreeNodes.size() == 0) childrenTreeNodes = null;

        root.setChildrenTreeNodes(childrenTreeNodes);
        return root;
    }
}
