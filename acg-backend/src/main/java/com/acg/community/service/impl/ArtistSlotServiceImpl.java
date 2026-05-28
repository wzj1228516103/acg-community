package com.acg.community.service.impl;

import com.acg.community.dto.CreateSlotDTO;
import com.acg.community.entity.ArtistAvailableSlot;
import com.acg.community.entity.MakeupService;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.ArtistAvailableSlotMapper;
import com.acg.community.mapper.MakeupServiceMapper;
import com.acg.community.service.ArtistSlotService;
import com.acg.community.vo.SlotVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArtistSlotServiceImpl extends ServiceImpl<ArtistAvailableSlotMapper, ArtistAvailableSlot> implements ArtistSlotService {

    @Resource
    private ArtistAvailableSlotMapper slotMapper;

    @Resource
    private MakeupServiceMapper makeupServiceMapper;

    @Override
    public void createSlots(Long artistId, CreateSlotDTO dto) {
        MakeupService service = makeupServiceMapper.selectById(dto.getServiceId());
        if (service == null || !service.getArtistId().equals(artistId)) {
            throw new BusinessException("无权操作此服务");
        }

        for (CreateSlotDTO.SlotTime slot : dto.getSlots()) {
            if (!slot.getEndTime().isAfter(slot.getStartTime())) {
                throw new BusinessException("结束时间必须晚于开始时间");
            }
            ArtistAvailableSlot entity = new ArtistAvailableSlot();
            entity.setArtistId(artistId);
            entity.setServiceId(dto.getServiceId());
            entity.setStartTime(slot.getStartTime());
            entity.setEndTime(slot.getEndTime());
            entity.setBooked(false);
            slotMapper.insert(entity);
        }
        log.info("化妆师设置可用时间, artistId={}, serviceId={}, count={}", artistId, dto.getServiceId(), dto.getSlots().size());
    }

    @Override
    public List<SlotVO> getAvailableSlots(Long serviceId) {
        List<ArtistAvailableSlot> slots = lambdaQuery()
                .eq(ArtistAvailableSlot::getServiceId, serviceId)
                .eq(ArtistAvailableSlot::getBooked, false)
                .ge(ArtistAvailableSlot::getStartTime, java.time.LocalDateTime.now())
                .orderByAsc(ArtistAvailableSlot::getStartTime)
                .list();

        return slots.stream().map(slot -> {
            SlotVO vo = new SlotVO();
            vo.setId(slot.getId());
            vo.setServiceId(slot.getServiceId());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
            vo.setBooked(slot.getBooked());
            return vo;
        }).toList();
    }
}
