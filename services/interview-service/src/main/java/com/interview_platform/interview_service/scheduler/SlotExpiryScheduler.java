//package com.interview_platform.interview_service.scheduler;
//
//import com.interview_platform.interview_service.entity.InterviewSlot;
//import com.interview_platform.interview_service.repository.InterviewSlotRepository;
//import com.interview_platform.interview_service.utils.SlotStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class SlotExpiryScheduler {
//
//    private final InterviewSlotRepository slotRepository;
//
//    @Value("${interview.booking.auto-expiry-hours:24}")
//    private int autoExpiryHours;
//
//    /**
//     * Runs every hour to expire slots that are:
//     * - Still AVAILABLE
//     * - Start time is within auto-expiry hours
//     */
//    @Scheduled(cron = "0 0 * * * *") // Every hour
//    @Transactional
//    public void expireUnbookedSlots() {
//        log.info("Running slot expiry job...");
//
//        LocalDateTime expiryThreshold = LocalDateTime.now().plusHours(autoExpiryHours);
//
//        List<InterviewSlot> expiredSlots = slotRepository.findExpiredSlots(expiryThreshold);
//
//        if (!expiredSlots.isEmpty()) {
//            expiredSlots.forEach(slot -> {
//                slot.markAsExpired();
//                log.info("Expired slot: {} (start time: {})", slot.getId(), slot.getStartTime());
//            });
//
//            slotRepository.saveAll(expiredSlots);
//            log.info("Expired {} slots", expiredSlots.size());
//        } else {
//            log.debug("No slots to expire");
//        }
//    }
//
//    /**
//     * Mark completed interviews
//     * Runs every 30 minutes
//     */
//    @Scheduled(cron = "0 */30 * * * *")
//    @Transactional
//    public void markCompletedInterviews() {
//        log.info("Running completed interviews job...");
//
//        // Find booked slots where end time has passed
//        List<InterviewSlot> completedSlots = slotRepository.findUpcomingInterviews(
//                LocalDateTime.now().minusHours(24),
//                LocalDateTime.now()
//        );
//
//        if (!completedSlots.isEmpty()) {
//            completedSlots.forEach(slot -> {
//                if (slot.getEndTime().isBefore(LocalDateTime.now()) &&
//                        slot.getStatus() == SlotStatus.BOOKED) {
//                    slot.markAsCompleted();
//                    log.info("Marked slot as completed: {}", slot.getId());
//                }
//            });
//
//            slotRepository.saveAll(completedSlots);
//            log.info("Marked {} interviews as completed", completedSlots.size());
//        }
//    }
//}