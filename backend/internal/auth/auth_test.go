package auth

import (
	"errors"
	"testing"
)

func TestOTPStoreIssuesSixDigitCodeAndVerifies(t *testing.T) {
	store := NewOTPStore()
	code, _, err := store.Request("+79991234567")
	if err != nil {
		t.Fatal(err)
	}
	if len(code) != 6 {
		t.Fatalf("expected six digits, got %q", code)
	}
	wrongCode := "000000"
	if code == wrongCode {
		wrongCode = "000001"
	}
	if err := store.Verify("+79991234567", wrongCode); !errors.Is(err, ErrInvalidCode) {
		t.Fatalf("expected invalid code, got %v", err)
	}
	if err := store.Verify("+79991234567", code); err != nil {
		t.Fatal(err)
	}
}

func TestOTPStoreRateLimits(t *testing.T) {
	store := NewOTPStore()
	_, _, err := store.Request("+79991234567")
	if err != nil {
		t.Fatal(err)
	}
	_, retry, err := store.Request("+79991234567")
	if !errors.Is(err, ErrRateLimited) || retry < 1 {
		t.Fatalf("expected rate limit, retry=%d err=%v", retry, err)
	}
}

func TestTokenRoundTrip(t *testing.T) {
	token, err := NewToken("secret", "client-id", "+79991234567")
	if err != nil {
		t.Fatal(err)
	}
	claims, err := ParseToken("secret", token)
	if err != nil || claims.ClientID != "client-id" {
		t.Fatalf("claims=%+v err=%v", claims, err)
	}
	if _, err := ParseToken("other", token); !errors.Is(err, ErrInvalidToken) {
		t.Fatal("expected invalid token for wrong secret")
	}
}
