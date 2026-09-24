package httpapi

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"
	"regexp"
	"strings"
	"time"

	"surf.local/backend/internal/auth"
	"surf.local/backend/internal/identity"
)

type Server struct {
	db        *sql.DB
	otp       *auth.OTPStore
	jwtSecret string
}

func New(db *sql.DB, jwtSecret string) http.Handler {
	server := &Server{db: db, otp: auth.NewOTPStore(), jwtSecret: jwtSecret}
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", server.health)
	mux.HandleFunc("POST /auth/request-code", server.requestCode)
	mux.HandleFunc("POST /auth/verify-code", server.verifyCode)
	mux.HandleFunc("GET /slots", server.withClient(server.listSlots))
	mux.HandleFunc("GET /slots/{id}", server.withClient(server.slotDetail))
	mux.HandleFunc("POST /bookings", server.withClient(server.createBooking))
	mux.HandleFunc("GET /bookings/my", server.withClient(server.listMyBookings))
	mux.HandleFunc("POST /bookings/{id}/cancel", server.withClient(server.cancelBooking))
	mux.HandleFunc("POST /bookings/{id}/rating", server.withClient(server.rateBooking))
	mux.HandleFunc("POST /devices/push-token", server.withClient(server.registerDevice))
	return mux
}

func (s *Server) withClient(next func(http.ResponseWriter, *http.Request, auth.Claims)) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		header := r.Header.Get("Authorization")
		if !strings.HasPrefix(header, "Bearer ") {
			writeError(w, http.StatusUnauthorized, "UNAUTHORIZED", "Требуется авторизация")
			return
		}
		claims, err := auth.ParseToken(s.jwtSecret, strings.TrimSpace(strings.TrimPrefix(header, "Bearer ")))
		if err != nil {
			writeError(w, http.StatusUnauthorized, "TOKEN_INVALID", "Сессия недействительна")
			return
		}
		next(w, r, claims)
	}
}

func (s *Server) health(w http.ResponseWriter, r *http.Request) {
	if err := s.db.PingContext(r.Context()); err != nil {
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "База данных недоступна")
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok", "database": "ok"})
}

func decodeJSON(w http.ResponseWriter, r *http.Request, target any) bool {
	decoder := json.NewDecoder(http.MaxBytesReader(w, r.Body, 1<<20))
	if err := decoder.Decode(target); err != nil {
		writeError(w, http.StatusBadRequest, "INVALID_JSON", "Некорректное тело запроса")
		return false
	}
	return true
}

func writeJSON(w http.ResponseWriter, status int, value any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(value)
}

func writeError(w http.ResponseWriter, status int, code string, message string) {
	requestID, err := identity.NewUUID()
	if err != nil {
		requestID = fmt.Sprintf("fallback-%d", time.Now().UnixNano())
	}
	writeJSON(w, status, map[string]any{"code": code, "message": message, "request_id": requestID})
}

func validPhone(value string) bool {
	return regexp.MustCompile(`^\+7[0-9]{10}$`).MatchString(value)
}

func pathID(r *http.Request) string {
	return r.PathValue("id")
}
