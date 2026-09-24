package store

import (
	"context"
	"database/sql"
)

type queryer interface {
	QueryContext(context.Context, string, ...any) (*sql.Rows, error)
	QueryRowContext(context.Context, string, ...any) *sql.Row
}

func loadSlotForUpdate(ctx context.Context, tx *sql.Tx, id string) (SlotSummary, error) {
	row := tx.QueryRowContext(ctx, `
		SELECT s.id::text, s.starts_at, s.ends_at, s.format, s.zone, s.address,
		       i.id::text, i.full_name, i.is_active, s.capacity, s.booked_count, s.status, s.cancellation_reason
		FROM training_slots s JOIN instructors i ON i.id = s.instructor_id
		WHERE s.id = $1 FOR UPDATE`, id)
	slot, err := scanSlot(row)
	if err == sql.ErrNoRows {
		return SlotSummary{}, ErrSlotUnavailable
	}
	return slot, err
}

func loadBooking(ctx context.Context, q queryer, id string) (*Booking, error) {
	var booking Booking
	var cancellationReason sql.NullString
	var slotCancellationReason sql.NullString
	var cancelledAt sql.NullTime
	var booked int
	err := q.QueryRowContext(ctx, `
		SELECT b.id::text, b.status, b.payment_method, b.cancel_deadline, b.cancellation_reason,
		       b.created_at, b.cancelled_at,
		       s.id::text, s.starts_at, s.ends_at, s.format, s.zone, s.address,
		       i.id::text, i.full_name, i.is_active, s.capacity, s.booked_count, s.status, s.cancellation_reason
		FROM bookings b
		JOIN training_slots s ON s.id = b.slot_id
		JOIN instructors i ON i.id = s.instructor_id
		WHERE b.id = $1`, id).Scan(
		&booking.ID, &booking.Status, &booking.PaymentMethod, &booking.CancelDeadline, &cancellationReason,
		&booking.CreatedAt, &cancelledAt,
		&booking.Slot.ID, &booking.Slot.StartsAt, &booking.Slot.EndsAt, &booking.Slot.Format, &booking.Slot.Zone, &booking.Slot.Address,
		&booking.Slot.Instructor.ID, &booking.Slot.Instructor.FullName, &booking.Slot.Instructor.IsActive,
		&booking.Slot.Capacity, &booked, &booking.Slot.Status, &slotCancellationReason)
	if err == sql.ErrNoRows {
		return nil, ErrBookingNotFound
	}
	if err != nil {
		return nil, err
	}
	booking.Slot.AvailablePlaces = booking.Slot.Capacity - booked
	if booking.Slot.AvailablePlaces < 0 {
		booking.Slot.AvailablePlaces = 0
	}
	if slotCancellationReason.Valid {
		booking.Slot.CancellationReason = &slotCancellationReason.String
	}
	if cancellationReason.Valid || booking.Status == "cancelled_by_client" || booking.Status == "cancelled_by_venue" {
		booking.Cancellation = &CancellationInfo{Status: booking.Status}
		if cancellationReason.Valid {
			booking.Cancellation.Reason = &cancellationReason.String
		}
	}
	if cancelledAt.Valid {
		booking.CancelledAt = &cancelledAt.Time
		if booking.Cancellation != nil {
			booking.Cancellation.CancelledAt = &cancelledAt.Time
		}
	}
	booking.Equipment = loadEquipmentSelections(ctx, q, id)
	if booking.Status == "rated" {
		booking.Rating, _ = loadRating(ctx, q, id)
	}
	return &booking, nil
}

func loadEquipmentSelections(ctx context.Context, q queryer, bookingID string) EquipmentSelections {
	selections := EquipmentSelections{Shoes: EquipmentSelection{Source: "own"}, Harness: EquipmentSelection{Source: "own"}}
	rows, err := q.QueryContext(ctx, `
		SELECT e.type, be.equipment_option_id::text
		FROM booking_equipment be JOIN equipment_options e ON e.id = be.equipment_option_id
		WHERE be.booking_id = $1`, bookingID)
	if err != nil {
		return selections
	}
	defer rows.Close()
	for rows.Next() {
		var typ, optionID string
		if rows.Scan(&typ, &optionID) != nil {
			continue
		}
		if typ == "climbing_shoes" {
			selections.Shoes = EquipmentSelection{Source: "rental", OptionID: optionID}
		}
		if typ == "harness_system" {
			selections.Harness = EquipmentSelection{Source: "rental", OptionID: optionID}
		}
	}
	return selections
}

func loadRating(ctx context.Context, q queryer, bookingID string) (*Rating, error) {
	var rating Rating
	err := q.QueryRowContext(ctx, `
		SELECT r.id::text, r.booking_id::text, i.id::text, i.full_name, i.is_active, r.score, r.created_at
		FROM ratings r JOIN instructors i ON i.id = r.instructor_id WHERE r.booking_id = $1`, bookingID).Scan(&rating.ID, &rating.BookingID, &rating.Instructor.ID, &rating.Instructor.FullName, &rating.Instructor.IsActive, &rating.Score, &rating.CreatedAt)
	return &rating, err
}

func ListMyBookings(ctx context.Context, db *sql.DB, clientID string) ([]Booking, []Booking, error) {
	rows, err := db.QueryContext(ctx, `SELECT id::text FROM bookings WHERE client_id = $1 ORDER BY created_at DESC`, clientID)
	if err != nil {
		return nil, nil, err
	}
	defer rows.Close()
	active := make([]Booking, 0)
	history := make([]Booking, 0)
	for rows.Next() {
		var id string
		if err := rows.Scan(&id); err != nil {
			return nil, nil, err
		}
		booking, err := loadBooking(ctx, db, id)
		if err != nil {
			return nil, nil, err
		}
		if booking.Status == "confirmed" {
			active = append(active, *booking)
		} else {
			history = append(history, *booking)
		}
	}
	return active, history, rows.Err()
}
