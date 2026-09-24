package store

import (
	"context"
	"database/sql"
	"time"
)

func CreateBooking(ctx context.Context, db *sql.DB, clientID string, request CreateBookingRequest) (*Booking, error) {
	if request.PaymentMethod != "on_site" || !ValidUUID(request.SlotID) {
		return nil, ErrInvalidBooking
	}
	if err := validateSelection(request.Equipment.Shoes, "climbing_shoes"); err != nil {
		return nil, err
	}
	if err := validateSelection(request.Equipment.Harness, "harness_system"); err != nil {
		return nil, err
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()
	slot, err := loadSlotForUpdate(ctx, tx, request.SlotID)
	if err != nil {
		return nil, err
	}
	if slot.Status != "available" {
		return nil, ErrSlotUnavailable
	}
	if slot.AvailablePlaces <= 0 {
		return nil, ErrSlotFull
	}
	if err := reserveEquipment(ctx, tx, request.Equipment.Shoes, "climbing_shoes"); err != nil {
		return nil, err
	}
	if err := reserveEquipment(ctx, tx, request.Equipment.Harness, "harness_system"); err != nil {
		return nil, err
	}
	createdAt := time.Now().UTC()
	var bookingID string
	err = tx.QueryRowContext(ctx, `
		INSERT INTO bookings (client_id, slot_id, status, payment_method, cancel_deadline, created_at, updated_at)
		VALUES ($1, $2, 'confirmed', 'on_site', $3, $4, $4)
		RETURNING id::text`, clientID, request.SlotID, slot.StartsAt.Add(-2*time.Hour), createdAt).Scan(&bookingID)
	if err != nil {
		if isUnique(err) {
			return nil, ErrBookingExists
		}
		return nil, err
	}
	if err := insertEquipment(ctx, tx, bookingID, request.Equipment.Shoes); err != nil {
		return nil, err
	}
	if err := insertEquipment(ctx, tx, bookingID, request.Equipment.Harness); err != nil {
		return nil, err
	}
	if _, err := tx.ExecContext(ctx, `UPDATE training_slots SET booked_count = booked_count + 1, updated_at = now() WHERE id = $1`, request.SlotID); err != nil {
		return nil, err
	}
	booking, err := loadBooking(ctx, tx, bookingID)
	if err != nil {
		return nil, err
	}
	if err := tx.Commit(); err != nil {
		return nil, err
	}
	return booking, nil
}

func CancelBooking(ctx context.Context, db *sql.DB, clientID string, bookingID string) (*Booking, error) {
	if !ValidUUID(bookingID) {
		return nil, ErrBookingNotFound
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()
	var ownerID string
	var status string
	var deadline time.Time
	err = tx.QueryRowContext(ctx, `SELECT client_id::text, status, cancel_deadline FROM bookings WHERE id = $1 FOR UPDATE`, bookingID).Scan(&ownerID, &status, &deadline)
	if err == sql.ErrNoRows {
		return nil, ErrBookingNotFound
	}
	if err != nil {
		return nil, err
	}
	if ownerID != clientID {
		return nil, ErrBookingNotFound
	}
	if status != "confirmed" {
		return nil, ErrInvalidBooking
	}
	if time.Now().UTC().After(deadline) {
		return nil, ErrInvalidBooking
	}
	if _, err := tx.ExecContext(ctx, `UPDATE bookings SET status = 'cancelled_by_client', cancelled_at = now(), updated_at = now() WHERE id = $1`, bookingID); err != nil {
		return nil, err
	}
	if _, err := tx.ExecContext(ctx, `UPDATE training_slots SET booked_count = GREATEST(booked_count - 1, 0), updated_at = now() WHERE id = (SELECT slot_id FROM bookings WHERE id = $1)`, bookingID); err != nil {
		return nil, err
	}
	booking, err := loadBooking(ctx, tx, bookingID)
	if err != nil {
		return nil, err
	}
	if err := tx.Commit(); err != nil {
		return nil, err
	}
	return booking, nil
}

func RateBooking(ctx context.Context, db *sql.DB, clientID string, bookingID string, score int) (*Rating, error) {
	if !ValidUUID(bookingID) || score < 1 || score > 5 {
		return nil, ErrInvalidBooking
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return nil, err
	}
	defer tx.Rollback()
	var ownerID string
	var status string
	var instructorID string
	err = tx.QueryRowContext(ctx, `SELECT b.client_id::text, b.status, s.instructor_id::text FROM bookings b JOIN training_slots s ON s.id = b.slot_id WHERE b.id = $1 FOR UPDATE OF b`, bookingID).Scan(&ownerID, &status, &instructorID)
	if err == sql.ErrNoRows {
		return nil, ErrBookingNotFound
	}
	if err != nil {
		return nil, err
	}
	if ownerID != clientID || status != "completed" {
		return nil, ErrInvalidBooking
	}
	var rating Rating
	rating.BookingID = bookingID
	rating.Score = score
	rating.CreatedAt = time.Now().UTC()
	if err := tx.QueryRowContext(ctx, `INSERT INTO ratings (booking_id, instructor_id, score, created_at) VALUES ($1, $2, $3, $4) RETURNING id::text, created_at`, bookingID, instructorID, score, rating.CreatedAt).Scan(&rating.ID, &rating.CreatedAt); err != nil {
		if isUnique(err) {
			return nil, ErrInvalidBooking
		}
		return nil, err
	}
	if _, err := tx.ExecContext(ctx, `UPDATE bookings SET status = 'rated', updated_at = now() WHERE id = $1`, bookingID); err != nil {
		return nil, err
	}
	if err := tx.QueryRowContext(ctx, `SELECT id::text, full_name, is_active FROM instructors WHERE id = $1`, instructorID).Scan(&rating.Instructor.ID, &rating.Instructor.FullName, &rating.Instructor.IsActive); err != nil {
		return nil, err
	}
	if err := tx.Commit(); err != nil {
		return nil, err
	}
	return &rating, nil
}
