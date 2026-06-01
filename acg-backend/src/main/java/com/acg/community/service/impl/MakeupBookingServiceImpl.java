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

        int affected = slotMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ArtistAvailableSlot>()
                .eq(ArtistAvailableSlot::getId, dto.getSlotId())
                .eq(ArtistAvailableSlot::getBooked, false)
                .set(ArtistAvailableSlot::getBooked, true));
        if (affected == 0) {
            throw new BusinessException("该时间段已被预约");
        }

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

    @Override
    @Transactional
    public void confirmBooking(Long bookingId, Long artistId) {
        MakeupBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        MakeupService service = makeupServiceMapper.selectById(booking.getServiceId());
        if (service == null || !service.getArtistId().equals(artistId)) {
            throw new BusinessException("无权操作该预约");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("预约状态不允许确认");
        }
        lambdaUpdate().eq(MakeupBooking::getId, bookingId).set(MakeupBooking::getStatus, BookingStatus.CONFIRMED).update();
        log.info("预约已确认, bookingId={}", bookingId);
    }

    @Override
    @Transactional
    public void completeBooking(Long bookingId, Long artistId) {
        MakeupBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        MakeupService service = makeupServiceMapper.selectById(booking.getServiceId());
        if (service == null || !service.getArtistId().equals(artistId)) {
            throw new BusinessException("无权操作该预约");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("预约状态不允许完成");
        }
        lambdaUpdate().eq(MakeupBooking::getId, bookingId).set(MakeupBooking::getStatus, BookingStatus.COMPLETED).update();
        log.info("预约已完成, bookingId={}", bookingId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        MakeupBooking booking = getById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该预约");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("预约状态不允许取消");
        }
        lambdaUpdate().eq(MakeupBooking::getId, bookingId).set(MakeupBooking::getStatus, BookingStatus.CANCELLED).update();

        ArtistAvailableSlot slot = slotMapper.selectById(booking.getSlotId());
        if (slot != null) {
            slot.setBooked(false);
            slotMapper.updateById(slot);
        }
        log.info("预约已取消, bookingId={}, 释放时间段slotId={}", bookingId, booking.getSlotId());
    }
}
