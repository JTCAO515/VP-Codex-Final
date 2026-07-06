package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class LoginWithEmailUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}
