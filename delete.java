 @DeleteMapping("/{bookingId}")
    @Operation(summary = "Delete a booking")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long bookingId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        bookingService.deleteBooking(bookingId, userId, role);
        return ResponseEntity.noContent().build();
    }


void deleteBooking(Long bookingId, Long userId, String role);




@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    
    @Override
    @Transactional
    public void deleteBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own bookings");
        }
        
        bookingRepository.delete(booking);
    }
}
