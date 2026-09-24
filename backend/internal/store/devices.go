package store

import (
	"context"
	"database/sql"
)

func RegisterDevice(ctx context.Context, db *sql.DB, clientID string, request PushTokenRequest) (*NotificationDevice, error) {
	var device NotificationDevice
	err := db.QueryRowContext(ctx, `
		INSERT INTO notification_devices (client_id, platform, push_token, is_enabled)
		VALUES ($1, $2, $3, $4)
		ON CONFLICT (push_token) DO UPDATE SET client_id = EXCLUDED.client_id, platform = EXCLUDED.platform, is_enabled = EXCLUDED.is_enabled, updated_at = now()
		RETURNING id::text, client_id::text, platform, push_token, is_enabled, created_at, updated_at`, clientID, request.Platform, request.PushToken, request.IsEnabled).Scan(&device.ID, &device.ClientID, &device.Platform, &device.PushToken, &device.IsEnabled, &device.CreatedAt, &device.UpdatedAt)
	return &device, err
}
