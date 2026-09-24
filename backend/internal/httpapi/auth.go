package httpapi

import (
	"errors"
	"log"
	"net/http"
	"time"

	"surf.local/backend/internal/auth"
	"surf.local/backend/internal/store"
)

type requestCodeRequest struct {
	Phone string `json:"phone"`
}

type verifyCodeRequest struct {
	Phone string `json:"phone"`
	Code  string `json:"code"`
}

func (s *Server) requestCode(w http.ResponseWriter, r *http.Request) {
	var request requestCodeRequest
	if !decodeJSON(w, r, &request) || !validPhone(request.Phone) {
		if request.Phone != "" {
			writeError(w, http.StatusBadRequest, "PHONE_INVALID", "Номер должен быть в формате +7XXXXXXXXXX")
		}
		return
	}
	code, retryAfter, err := s.otp.Request(request.Phone)
	if errors.Is(err, auth.ErrRateLimited) {
		w.Header().Set("Retry-After", time.Duration(retryAfter).String())
		writeError(w, http.StatusTooManyRequests, "OTP_RATE_LIMITED", "Повторите запрос позже")
		return
	}
	if err != nil {
		writeError(w, http.StatusServiceUnavailable, "SMS_UNAVAILABLE", "Не удалось отправить код")
		return
	}
	log.Printf("OTP phone=%s code=%s", request.Phone, code)
	writeJSON(w, http.StatusAccepted, map[string]any{"status": "code_sent", "message": "Код отправлен", "expires_in": 300, "retry_after": retryAfter})
}

func (s *Server) verifyCode(w http.ResponseWriter, r *http.Request) {
	var request verifyCodeRequest
	if !decodeJSON(w, r, &request) {
		return
	}
	if !validPhone(request.Phone) || len(request.Code) != 6 || !allDigits(request.Code) {
		writeError(w, http.StatusBadRequest, "OTP_INVALID", "Введите корректный шестизначный код")
		return
	}
	if err := s.otp.Verify(request.Phone, request.Code); err != nil {
		if errors.Is(err, auth.ErrExpiredCode) {
			writeError(w, http.StatusBadRequest, "OTP_EXPIRED", "Срок действия кода истёк")
			return
		}
		writeError(w, http.StatusUnauthorized, "OTP_INVALID", "Неверный код")
		return
	}
	client, err := store.GetOrCreateClient(r.Context(), s.db, request.Phone)
	if err != nil {
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось создать клиента")
		return
	}
	token, err := auth.NewToken(s.jwtSecret, client.ID, client.Phone)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "TOKEN_CREATE_FAILED", "Не удалось создать сессию")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"access_token": token, "token_type": "Bearer", "expires_in": 3600, "client": client})
}

func allDigits(value string) bool {
	for _, character := range value {
		if character < '0' || character > '9' {
			return false
		}
	}
	return true
}
