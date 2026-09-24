package store

import (
	"context"
	"database/sql"
	"strings"
	"time"
)

type SlotFilter struct {
	From         time.Time
	To           time.Time
	Format       string
	InstructorID string
}

func ListSlots(ctx context.Context, db *sql.DB, filter SlotFilter) (SlotList, error) {
	rows, err := db.QueryContext(ctx, `
		SELECT s.id::text, s.starts_at, s.ends_at, s.format, s.zone, s.address,
		       i.id::text, i.full_name, i.is_active, s.capacity, s.booked_count,
		       s.status, s.cancellation_reason
		FROM training_slots s
		JOIN instructors i ON i.id = s.instructor_id
		WHERE s.starts_at >= $1 AND s.starts_at < $2
		  AND ($3 = '' OR s.format = $3)
		  AND ($4 = '' OR i.id::text = $4)
		ORDER BY s.starts_at`, filter.From, filter.To, filter.Format, filter.InstructorID)
	if err != nil {
		return SlotList{}, err
	}
	defer rows.Close()
	items := make([]SlotSummary, 0)
	for rows.Next() {
		item, err := scanSlot(rows)
		if err != nil {
			return SlotList{}, err
		}
		items = append(items, item)
	}
	return SlotList{From: filter.From, To: filter.To, Items: items}, rows.Err()
}

func GetSlot(ctx context.Context, db *sql.DB, id string) (SlotDetail, error) {
	row := db.QueryRowContext(ctx, `
		SELECT s.id::text, s.starts_at, s.ends_at, s.format, s.zone, s.address,
		       i.id::text, i.full_name, i.is_active, s.capacity, s.booked_count,
		       s.status, s.cancellation_reason
		FROM training_slots s
		JOIN instructors i ON i.id = s.instructor_id
		WHERE s.id = $1`, id)
	item, err := scanSlot(row)
	if err == sql.ErrNoRows {
		return SlotDetail{}, ErrSlotUnavailable
	}
	if err != nil {
		return SlotDetail{}, err
	}
	equipment, err := listEquipment(ctx, db, id)
	if err != nil {
		return SlotDetail{}, err
	}
	return SlotDetail{SlotSummary: item, EquipmentOptions: equipment}, nil
}

func listEquipment(ctx context.Context, db *sql.DB, slotID string) ([]EquipmentOption, error) {
	rows, err := db.QueryContext(ctx, `
		SELECT id::text, slot_id::text, type, name, size, price::float8, currency,
		       total_quantity, available_quantity, is_active
		FROM equipment_options
		WHERE slot_id IS NULL OR slot_id = $1
		ORDER BY type, size, id`, slotID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()
	items := make([]EquipmentOption, 0)
	for rows.Next() {
		var item EquipmentOption
		var slotID, size sql.NullString
		if err := rows.Scan(&item.ID, &slotID, &item.Type, &item.Name, &size, &item.Price, &item.Currency, &item.TotalQuantity, &item.AvailableQuantity, &item.IsActive); err != nil {
			return nil, err
		}
		if slotID.Valid {
			item.SlotID = &slotID.String
		}
		if size.Valid {
			item.Size = &size.String
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

type scanner interface{ Scan(...any) error }

func scanSlot(row scanner) (SlotSummary, error) {
	var item SlotSummary
	var reason sql.NullString
	var booked int
	if err := row.Scan(&item.ID, &item.StartsAt, &item.EndsAt, &item.Format, &item.Zone, &item.Address, &item.Instructor.ID, &item.Instructor.FullName, &item.Instructor.IsActive, &item.Capacity, &booked, &item.Status, &reason); err != nil {
		return SlotSummary{}, err
	}
	item.AvailablePlaces = item.Capacity - booked
	if item.AvailablePlaces < 0 {
		item.AvailablePlaces = 0
	}
	if reason.Valid {
		item.CancellationReason = &reason.String
	}
	return item, nil
}

func ValidFormat(value string) bool {
	return value == "" || value == "novice_bouldering" || value == "rope_routes"
}

func ValidUUID(value string) bool {
	return len(value) == 36 && strings.Count(value, "-") == 4
}
