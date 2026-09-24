package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"surf.local/backend/internal/config"
	"surf.local/backend/internal/httpapi"
	"surf.local/backend/internal/store"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatal(err)
	}
	startupContext, cancel := context.WithTimeout(context.Background(), 2*time.Minute)
	defer cancel()
	db, err := store.Open(startupContext, cfg.DatabaseURL)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()
	if err := store.Migrate(startupContext, db, cfg.MigrationsDir); err != nil {
		log.Fatalf("migrations failed: %v", err)
	}
	if err := store.SeedIfEmpty(startupContext, db, cfg.SeedDir); err != nil {
		log.Fatalf("seed failed: %v", err)
	}
	server := &http.Server{Addr: cfg.HTTPAddr, Handler: httpapi.New(db, cfg.JWTSecret), ReadHeaderTimeout: 5 * time.Second, ReadTimeout: 15 * time.Second, WriteTimeout: 15 * time.Second, IdleTimeout: 60 * time.Second}
	go func() {
		<-shutdownSignal()
		shutdownContext, shutdownCancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer shutdownCancel()
		_ = server.Shutdown(shutdownContext)
	}()
	log.Printf("surf backend listening on %s", cfg.HTTPAddr)
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatal(err)
	}
}

func shutdownSignal() <-chan os.Signal {
	channel := make(chan os.Signal, 1)
	signal.Notify(channel, syscall.SIGINT, syscall.SIGTERM)
	return channel
}
