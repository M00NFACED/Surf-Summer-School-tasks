package config

import (
	"fmt"
	"os"
)

type Config struct {
	HTTPAddr      string
	DatabaseURL   string
	JWTSecret     string
	MigrationsDir string
	SeedDir       string
}

func Load() (Config, error) {
	cfg := Config{
		HTTPAddr:      valueOr("HTTP_ADDR", ":8080"),
		DatabaseURL:   os.Getenv("DATABASE_URL"),
		JWTSecret:     os.Getenv("JWT_SECRET"),
		MigrationsDir: valueOr("MIGRATIONS_DIR", "migrations"),
		SeedDir:       valueOr("SEED_DIR", "seed"),
	}
	if cfg.DatabaseURL == "" {
		return Config{}, fmt.Errorf("DATABASE_URL is required")
	}
	if cfg.JWTSecret == "" {
		return Config{}, fmt.Errorf("JWT_SECRET is required")
	}
	return cfg, nil
}

func valueOr(key string, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}
