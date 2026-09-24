package auth

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"errors"
	"strings"
	"time"
)

type Claims struct {
	ClientID string `json:"client_id"`
	Phone    string `json:"phone"`
	IssuedAt int64  `json:"iat"`
	Expires  int64  `json:"exp"`
}

var ErrInvalidToken = errors.New("invalid token")

func NewToken(secret string, clientID string, phone string) (string, error) {
	header := base64.RawURLEncoding.EncodeToString([]byte(`{"alg":"HS256","typ":"JWT"}`))
	now := time.Now()
	payload, err := json.Marshal(Claims{ClientID: clientID, Phone: phone, IssuedAt: now.Unix(), Expires: now.Add(time.Hour).Unix()})
	if err != nil {
		return "", err
	}
	body := header + "." + base64.RawURLEncoding.EncodeToString(payload)
	return body + "." + sign(secret, body), nil
}

func ParseToken(secret string, value string) (Claims, error) {
	parts := strings.Split(value, ".")
	if len(parts) != 3 {
		return Claims{}, ErrInvalidToken
	}
	body := parts[0] + "." + parts[1]
	if !hmac.Equal([]byte(parts[2]), []byte(sign(secret, body))) {
		return Claims{}, ErrInvalidToken
	}
	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return Claims{}, ErrInvalidToken
	}
	var claims Claims
	if err := json.Unmarshal(payload, &claims); err != nil || claims.ClientID == "" || claims.Expires < time.Now().Unix() {
		return Claims{}, ErrInvalidToken
	}
	return claims, nil
}

func sign(secret string, body string) string {
	mac := hmac.New(sha256.New, []byte(secret))
	_, _ = mac.Write([]byte(body))
	return base64.RawURLEncoding.EncodeToString(mac.Sum(nil))
}
