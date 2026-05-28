package com.acg.community.service;

import com.acg.community.dto.CreateSlotDTO;
import com.acg.community.entity.ArtistAvailableSlot;
import com.acg.community.vo.SlotVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ArtistSlotService extends IService<ArtistAvailableSlot> {

    void createSlots(Long artistId, CreateSlotDTO dto);

    List<SlotVO> getAvailableSlots(Long serviceId);
}
