package httpapi

import (
	"errors"
	"net/http"
	"strings"

	"surf.local/backend/internal/auth"
	"surf.local/backend/internal/store"
)

func (s *Server) createBooking(w http.ResponseWriter, r *http.Request, claims auth.Claims) {
	var request store.CreateBookingRequest
	if !decodeJSON(w, r, &request) {
		return
	}
	booking, err := store.CreateBooking(r.Context(), s.db, claims.ClientID, request)
	if err != nil {
		s.writeBookingError(w, err)
		return
	}
	writeJSON(w, http.StatusCreated, booking)
}

func (s *Server) listMyBookings(w http.ResponseWriter, r *http.Request, claims auth.Claims) {
	active, history, err := store.ListMyBookings(r.Context(), s.db, claims.ClientID)
	if err != nil {
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось получить брони")
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"active": active, "history": history})
}

func (s *Server) cancelBooking(w http.ResponseWriter, r *http.Request, claims auth.Claims) {
	booking, err := store.CancelBooking(r.Context(), s.db, claims.ClientID, pathID(r))
	if err != nil {
		s.writeBookingError(w, err)
		return
	}
	writeJSON(w, http.StatusOK, booking)
}

func (s *Server) rateBooking(w http.ResponseWriter, r *http.Request, claims auth.Claims) {
	var request store.RatingRequest
	if !decodeJSON(w, r, &request) {
		return
	}
	rating, err := store.RateBooking(r.Context(), s.db, claims.ClientID, pathID(r), request.Score)
	if err != nil {
		s.writeBookingError(w, err)
		return
	}
	writeJSON(w, http.StatusCreated, rating)
}

func (s *Server) registerDevice(w http.ResponseWriter, r *http.Request, claims auth.Claims) {
	var request store.PushTokenRequest
	if !decodeJSON(w, r, &request) {
		return
	}
	if (request.Platform != "ios" && request.Platform != "android") || strings.TrimSpace(request.PushToken) == "" {
		writeError(w, http.StatusBadRequest, "DEVICE_INVALID", "Некорректные данные устройства")
		return
	}
	device, err := store.RegisterDevice(r.Context(), s.db, claims.ClientID, request)
	if err != nil {
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось зарегистрировать устройство")
		return
	}
	writeJSON(w, http.StatusCreated, device)
}

func (s *Server) writeBookingError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, store.ErrSlotFull):
		writeError(w, http.StatusConflict, "SLOT_FULL", "Свободных мест больше нет")
	case errors.Is(err, store.ErrBookingExists):
		writeError(w, http.StatusConflict, "BOOKING_EXISTS", "Бронь на этот слот уже создана")
	case errors.Is(err, store.ErrBookingNotFound), errors.Is(err, store.ErrSlotUnavailable):
		writeError(w, http.StatusNotFound, "RESOURCE_NOT_FOUND", "Ресурс не найден")
	case errors.Is(err, store.ErrRentalUnavailable):
		writeError(w, http.StatusBadRequest, "RENTAL_UNAVAILABLE", "Прокат недоступен")
	case errors.Is(err, store.ErrInvalidBooking):
		writeError(w, http.StatusBadRequest, "BOOKING_INVALID", "Бронь не соответствует правилам")
	default:
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось выполнить операцию")
	}
}
