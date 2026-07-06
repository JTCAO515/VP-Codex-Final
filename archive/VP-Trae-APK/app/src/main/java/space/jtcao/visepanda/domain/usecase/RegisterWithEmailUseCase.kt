package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class RegisterWithEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, displayName: String?) =
        repository.register(email, password, displayName)
}
