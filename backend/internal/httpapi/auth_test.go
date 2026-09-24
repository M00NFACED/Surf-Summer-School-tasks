package httpapi

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestRequestCodeReturnsAccepted(t *testing.T) {
	handler := New(nil, "test-secret")
	request := httptest.NewRequest(http.MethodPost, "/auth/request-code", strings.NewReader(`{"phone":"+79991234567"}`))
	response := httptest.NewRecorder()
	handler.ServeHTTP(response, request)
	if response.Code != http.StatusAccepted {
		t.Fatalf("status=%d body=%s", response.Code, response.Body.String())
	}
	if !strings.Contains(response.Body.String(), "code_sent") {
		t.Fatalf("unexpected body=%s", response.Body.String())
	}
	t.Logf("response=%s", response.Body.String())
}
