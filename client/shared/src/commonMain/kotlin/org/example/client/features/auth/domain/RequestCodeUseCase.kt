package org.example.client.features.auth.domain

class RequestCodeUseCase(
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
) {
    operator fun invoke(phone: String): Result<String> {
        return if (validator.isValid(phone)) {
            Result.success(phone)
        } else {
            Result.failure(IllegalArgumentException("Введите номер в формате +7XXXXXXXXXX"))
        }
    }
}
