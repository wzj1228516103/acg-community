package com.acg.community.service.impl;

import com.acg.community.entity.MakeupService;
import com.acg.community.entity.User;
import com.acg.community.enums.GoodsStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.MakeupServiceMapper;
import com.acg.community.mapper.UserMapper;
import com.acg.community.service.MakeupServiceService;
import com.acg.community.vo.MakeupServiceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MakeupServiceImpl extends ServiceImpl<MakeupServiceMapper, MakeupService> implements MakeupServiceService {

    @Resource
    private MakeupServiceMapper makeupServiceMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<MakeupServiceVO> listActiveServices(int page, int size) {
        Page<MakeupService> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MakeupService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MakeupService::getStatus, GoodsStatus.ACTIVE)
                .orderByDesc(MakeupService::getCreatedAt);
        Page<MakeupService> result = makeupServiceMapper.selectPage(pageParam, wrapper);

        Page<MakeupServiceVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public MakeupServiceVO getServiceDetail(Long id) {
        MakeupService service = getById(id);
        if (service == null) {
            throw new BusinessException("服务不存在");
        }
        return toVO(service);
    }

    private MakeupServiceVO toVO(MakeupService service) {
        MakeupServiceVO vo = new MakeupServiceVO();
        vo.setId(service.getId());
        vo.setArtistId(service.getArtistId());
        vo.setName(service.getName());
        vo.setDescription(service.getDescription());
        vo.setPrice(service.getPrice());
        vo.setDuration(service.getDuration());
        vo.setImages(service.getImages());
        vo.setStatus(service.getStatus() != null ? service.getStatus().getCode() : 0);
        vo.setCreatedAt(service.getCreatedAt());

        if (service.getArtistId() != null) {
            User artist = userMapper.selectById(service.getArtistId());
            if (artist != null) {
                vo.setArtistName(artist.getUsername());
                vo.setArtistNickname(artist.getNickname());
                vo.setArtistAvatar(artist.getAvatarUrl());
            }
        }
        return vo;
    }
}
