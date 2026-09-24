package auth

import (
	"crypto/rand"
	"errors"
	"fmt"
	"math/big"
	"sync"
	"time"
)

var (
	ErrRateLimited = errors.New("request code rate limited")
	ErrInvalidCode = errors.New("invalid code")
	ErrExpiredCode = errors.New("expired code")
)

type otpEntry struct {
	code      string
	createdAt time.Time
	expiresAt time.Time
}

type OTPStore struct {
	mu      sync.Mutex
	entries map[string]otpEntry
}

func NewOTPStore() *OTPStore {
	return &OTPStore{entries: make(map[string]otpEntry)}
}

func (s *OTPStore) Request(phone string) (string, int, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	now := time.Now()
	if entry, ok := s.entries[phone]; ok {
		wait := int(entry.createdAt.Add(time.Minute).Sub(now).Seconds())
		if wait > 0 {
			return "", wait, ErrRateLimited
		}
	}
	code, err := generateCode()
	if err != nil {
		return "", 0, err
	}
	s.entries[phone] = otpEntry{code: code, createdAt: now, expiresAt: now.Add(5 * time.Minute)}
	return code, 60, nil
}

func (s *OTPStore) Verify(phone string, code string) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	entry, ok := s.entries[phone]
	if !ok {
		return ErrInvalidCode
	}
	if time.Now().After(entry.expiresAt) {
		delete(s.entries, phone)
		return ErrExpiredCode
	}
	if entry.code != code {
		return ErrInvalidCode
	}
	delete(s.entries, phone)
	return nil
}

func generateCode() (string, error) {
	value, err := rand.Int(rand.Reader, big.NewInt(1000000))
	if err != nil {
		return "", err
	}
	return fmt.Sprintf("%06d", value.Int64()), nil
}
