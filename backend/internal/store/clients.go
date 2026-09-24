package store

import (
	"context"
	"database/sql"
)

func GetOrCreateClient(ctx context.Context, db *sql.DB, phone string) (Client, error) {
	var client Client
	err := db.QueryRowContext(ctx, `
		INSERT INTO clients (phone)
		VALUES ($1)
		ON CONFLICT (phone) DO UPDATE SET updated_at = now()
		RETURNING id::text, phone, created_at, updated_at`, phone).Scan(
		&client.ID, &client.Phone, &client.CreatedAt, &client.UpdatedAt)
	return client, err
}
