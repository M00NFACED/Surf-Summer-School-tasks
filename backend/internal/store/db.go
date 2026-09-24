package store

import (
	"context"
	"database/sql"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"time"

	_ "github.com/lib/pq"
)

func Open(ctx context.Context, databaseURL string) (*sql.DB, error) {
	db, err := sql.Open("postgres", databaseURL)
	if err != nil {
		return nil, err
	}
	db.SetMaxOpenConns(20)
	db.SetMaxIdleConns(5)
	db.SetConnMaxLifetime(30 * time.Minute)
	for attempt := 0; attempt < 30; attempt++ {
		pingContext, cancel := context.WithTimeout(ctx, 3*time.Second)
		err = db.PingContext(pingContext)
		cancel()
		if err == nil {
			return db, nil
		}
		time.Sleep(time.Second)
	}
	_ = db.Close()
	return nil, fmt.Errorf("database unavailable: %w", err)
}

func Migrate(ctx context.Context, db *sql.DB, directory string) error {
	if _, err := db.ExecContext(ctx, `CREATE TABLE IF NOT EXISTS schema_migrations (version text PRIMARY KEY, applied_at timestamptz NOT NULL DEFAULT now())`); err != nil {
		return err
	}
	files, err := sqlFiles(directory)
	if err != nil {
		return err
	}
	for _, file := range files {
		version := strings.Split(filepath.Base(file), "_")[0]
		var applied int
		if err := db.QueryRowContext(ctx, `SELECT count(*) FROM schema_migrations WHERE version = $1`, version).Scan(&applied); err != nil {
			return err
		}
		if applied > 0 {
			continue
		}
		if err := applyFile(ctx, db, file, version); err != nil {
			return err
		}
	}
	return nil
}

func SeedIfEmpty(ctx context.Context, db *sql.DB, directory string) error {
	var count int
	if err := db.QueryRowContext(ctx, `SELECT count(*) FROM instructors`).Scan(&count); err != nil {
		return err
	}
	if count > 0 {
		return nil
	}
	files, err := sqlFiles(directory)
	if err != nil {
		return err
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	for _, file := range files {
		data, err := os.ReadFile(file)
		if err != nil {
			_ = tx.Rollback()
			return err
		}
		if _, err := tx.ExecContext(ctx, string(data)); err != nil {
			_ = tx.Rollback()
			return err
		}
	}
	return tx.Commit()
}

func Health(ctx context.Context, db *sql.DB) error {
	return db.PingContext(ctx)
}

func sqlFiles(directory string) ([]string, error) {
	files, err := filepath.Glob(filepath.Join(directory, "*.sql"))
	if err != nil {
		return nil, err
	}
	sort.Strings(files)
	return files, nil
}

func applyFile(ctx context.Context, db *sql.DB, file string, version string) error {
	data, err := os.ReadFile(file)
	if err != nil {
		return err
	}
	tx, err := db.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	if _, err := tx.ExecContext(ctx, string(data)); err != nil {
		_ = tx.Rollback()
		return err
	}
	if _, err := tx.ExecContext(ctx, `INSERT INTO schema_migrations (version) VALUES ($1)`, version); err != nil {
		_ = tx.Rollback()
		return err
	}
	return tx.Commit()
}
