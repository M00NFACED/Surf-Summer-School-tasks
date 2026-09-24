package store

import (
	"context"
	"database/sql"
	"errors"

	"github.com/lib/pq"
)

func validateSelection(selection EquipmentSelection, expectedType string) error {
	if selection.Source == "own" && selection.OptionID == "" {
		return nil
	}
	if selection.Source != "rental" || !ValidUUID(selection.OptionID) {
		return ErrInvalidBooking
	}
	_ = expectedType
	return nil
}

func reserveEquipment(ctx context.Context, tx *sql.Tx, selection EquipmentSelection, expectedType string) error {
	if selection.Source == "own" {
		return nil
	}
	var actualType string
	var available int
	var active bool
	if err := tx.QueryRowContext(ctx, `SELECT type, available_quantity, is_active FROM equipment_options WHERE id = $1 FOR UPDATE`, selection.OptionID).Scan(&actualType, &available, &active); err != nil {
		if err == sql.ErrNoRows {
			return ErrRentalUnavailable
		}
		return err
	}
	if !active || available < 1 || actualType != expectedType {
		return ErrRentalUnavailable
	}
	_, err := tx.ExecContext(ctx, `UPDATE equipment_options SET available_quantity = available_quantity - 1 WHERE id = $1`, selection.OptionID)
	return err
}

func insertEquipment(ctx context.Context, tx *sql.Tx, bookingID string, selection EquipmentSelection) error {
	if selection.Source != "rental" {
		return nil
	}
	_, err := tx.ExecContext(ctx, `INSERT INTO booking_equipment (booking_id, equipment_option_id) VALUES ($1, $2)`, bookingID, selection.OptionID)
	return err
}

func isUnique(err error) bool {
	var pqErr *pq.Error
	return errors.As(err, &pqErr) && pqErr.Code == "23505"
}
