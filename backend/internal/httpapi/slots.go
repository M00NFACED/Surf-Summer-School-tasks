package httpapi

import (
	"errors"
	"net/http"
	"time"

	"surf.local/backend/internal/auth"
	"surf.local/backend/internal/store"
)

func (s *Server) listSlots(w http.ResponseWriter, r *http.Request, _ auth.Claims) {
	from := time.Now().UTC()
	to := from.Add(7 * 24 * time.Hour)
	if value := r.URL.Query().Get("from"); value != "" {
		parsed, err := time.Parse(time.RFC3339, value)
		if err != nil {
			writeError(w, http.StatusBadRequest, "FROM_INVALID", "Некорректная дата from")
			return
		}
		from = parsed
	}
	if value := r.URL.Query().Get("to"); value != "" {
		parsed, err := time.Parse(time.RFC3339, value)
		if err != nil {
			writeError(w, http.StatusBadRequest, "TO_INVALID", "Некорректная дата to")
			return
		}
		to = parsed
	}
	if !to.After(from) {
		writeError(w, http.StatusBadRequest, "RANGE_INVALID", "Период должен заканчиваться позже начала")
		return
	}
	format := r.URL.Query().Get("format")
	if !store.ValidFormat(format) {
		writeError(w, http.StatusBadRequest, "FORMAT_INVALID", "Неподдерживаемый формат")
		return
	}
	result, err := store.ListSlots(r.Context(), s.db, store.SlotFilter{From: from, To: to, Format: format, InstructorID: r.URL.Query().Get("instructor_id")})
	if err != nil {
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось получить расписание")
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) slotDetail(w http.ResponseWriter, r *http.Request, _ auth.Claims) {
	result, err := store.GetSlot(r.Context(), s.db, pathID(r))
	if err != nil {
		if errors.Is(err, store.ErrSlotUnavailable) {
			writeError(w, http.StatusNotFound, "SLOT_NOT_FOUND", "Слот не найден")
			return
		}
		writeError(w, http.StatusServiceUnavailable, "DATABASE_UNAVAILABLE", "Не удалось получить слот")
		return
	}
	writeJSON(w, http.StatusOK, result)
}
