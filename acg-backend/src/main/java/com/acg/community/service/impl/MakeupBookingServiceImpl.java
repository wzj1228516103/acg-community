package com.acg.community.service.impl;

import com.acg.community.dto.CreateMakeupBookingDTO;
import com.acg.community.entity.ArtistAvailableSlot;
import com.acg.community.entity.MakeupBooking;
import com.acg.community.entity.MakeupService;
import com.acg.community.enums.BookingStatus;
import com.acg.community.exception.BusinessException;
import com.acg.community.mapper.ArtistAvailableSlotMapper;
import com.acg.community.mapper.MakeupBookingMapper;
import com.acg.community.mapper.MakeupServiceMapper;
import com.acg.community.service.MakeupBookingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MakeupBookingServiceImpl extends ServiceImpl<MakeupBookingMapper, MakeupBooking> implements MakeupBookingService {

    @Resource
    private MakeupBookingMapper makeupBookingMapper;

    @Resource
    private MakeupServiceMapper makeupServiceMapper;

    @Resource
    private ArtistAvailableSlotMapper slotMapper;

    @Override
    @Transactional
    public Long createBooking(Long userId, CreateMakeupBookingDTO dto) {
        MakeupService service = makeupServiceMapper.selectById(dto.getServiceId());
        if (service == null) {
            throw new BusinessException("化妆服务不存在");
        }

        ArtistAvailableSlot slot = slotMapper.selectById(dto.getSlotId());
        if (slot == null || !slot.getServiceId().equals(dto.getServiceId())) {
            throw new BusinessException("时间段不存在");
        }
        if (Boolean.TRUE.equals(slot.getBooked())) {
            throw new BusinessException("该时间段已被预约");
        }

        slot.setBooked(true);
        slotMapper.updateById(slot);

        MakeupBooking booking = new MakeupBooking();
        booking.setUserId(userId);
        booking.setServiceId(dto.getServiceId());
        booking.setSlotId(dto.getSlotId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setNotes(dto.getNotes());
        booking.setContactName(dto.getContactName());
        booking.setContactPhone(dto.getPhone());
        makeupBookingMapper.insert(booking);

        log.info("化妆服务预约成功, bookingId={}, userId={}, serviceId={}, slotId={}", booking.getId(), userId, dto.getServiceId(), dto.getSlotId());
        return booking.getId();
    }
}
