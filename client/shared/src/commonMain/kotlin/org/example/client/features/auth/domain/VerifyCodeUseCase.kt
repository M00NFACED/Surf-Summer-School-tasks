package org.example.client.features.auth.domain

class VerifyCodeUseCase(
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
) {
    operator fun invoke(phone: String, code: String): Result<Pair<String, String>> {
        if (!validator.isValid(phone)) {
            return Result.failure(IllegalArgumentException("Введите номер в формате +7XXXXXXXXXX"))
        }
        if (code.length != 6 || code.any { !it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Введите шестизначный код"))
        }
        return Result.success(phone to code)
    }
}
