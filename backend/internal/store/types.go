package store

import (
	"errors"
	"time"
)

var (
	ErrSlotFull          = errors.New("slot full")
	ErrSlotUnavailable   = errors.New("slot unavailable")
	ErrBookingExists     = errors.New("booking already exists")
	ErrBookingNotFound   = errors.New("booking not found")
	ErrRentalUnavailable = errors.New("rental option unavailable")
	ErrInvalidBooking    = errors.New("invalid booking")
)

type Client struct {
	ID        string    `json:"id"`
	Phone     string    `json:"phone"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type Instructor struct {
	ID       string `json:"id"`
	FullName string `json:"full_name"`
	IsActive bool   `json:"is_active"`
}

type EquipmentOption struct {
	ID                string  `json:"id"`
	SlotID            *string `json:"slot_id,omitempty"`
	Type              string  `json:"type"`
	Name              string  `json:"name"`
	Size              *string `json:"size,omitempty"`
	Price             float64 `json:"price"`
	Currency          string  `json:"currency"`
	TotalQuantity     int     `json:"total_quantity"`
	AvailableQuantity int     `json:"available_quantity"`
	IsActive          bool    `json:"is_active"`
}

type EquipmentSelection struct {
	Source   string `json:"source"`
	OptionID string `json:"option_id,omitempty"`
}

type EquipmentSelections struct {
	Shoes   EquipmentSelection `json:"shoes"`
	Harness EquipmentSelection `json:"harness"`
}

type SlotSummary struct {
	ID                 string     `json:"id"`
	StartsAt           time.Time  `json:"starts_at"`
	EndsAt             time.Time  `json:"ends_at"`
	Format             string     `json:"format"`
	Zone               string     `json:"zone"`
	Address            string     `json:"address"`
	Instructor         Instructor `json:"instructor"`
	Capacity           int        `json:"capacity"`
	AvailablePlaces    int        `json:"available_places"`
	Status             string     `json:"status"`
	CancellationReason *string    `json:"cancellation_reason,omitempty"`
}

type SlotDetail struct {
	SlotSummary
	EquipmentOptions []EquipmentOption `json:"equipment_options"`
}

type SlotList struct {
	From  time.Time     `json:"from"`
	To    time.Time     `json:"to"`
	Items []SlotSummary `json:"items"`
}

type CancellationInfo struct {
	Status      string     `json:"status"`
	Reason      *string    `json:"reason,omitempty"`
	CancelledAt *time.Time `json:"cancelled_at,omitempty"`
}

type Rating struct {
	ID         string     `json:"id"`
	BookingID  string     `json:"booking_id"`
	Instructor Instructor `json:"instructor"`
	Score      int        `json:"score"`
	CreatedAt  time.Time  `json:"created_at"`
}

type Booking struct {
	ID             string              `json:"id"`
	Slot           SlotSummary         `json:"slot"`
	Status         string              `json:"status"`
	Equipment      EquipmentSelections `json:"equipment"`
	PaymentMethod  string              `json:"payment_method"`
	CancelDeadline time.Time           `json:"cancel_deadline"`
	Cancellation   *CancellationInfo   `json:"cancellation,omitempty"`
	CreatedAt      time.Time           `json:"created_at"`
	CancelledAt    *time.Time          `json:"cancelled_at,omitempty"`
	Rating         *Rating             `json:"rating,omitempty"`
}

type CreateBookingRequest struct {
	SlotID        string              `json:"slot_id"`
	Equipment     EquipmentSelections `json:"equipment"`
	PaymentMethod string              `json:"payment_method"`
}

type RatingRequest struct {
	Score int `json:"score"`
}

type PushTokenRequest struct {
	Platform  string `json:"platform"`
	PushToken string `json:"push_token"`
	IsEnabled bool   `json:"is_enabled"`
}

type NotificationDevice struct {
	ID        string    `json:"id"`
	ClientID  string    `json:"client_id"`
	Platform  string    `json:"platform"`
	PushToken string    `json:"push_token"`
	IsEnabled bool      `json:"is_enabled"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}
