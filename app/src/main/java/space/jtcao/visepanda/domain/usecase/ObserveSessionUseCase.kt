package space.jtcao.visepanda.domain.usecase

import space.jtcao.visepanda.domain.repository.AuthRepository

class ObserveSessionUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.observeSession()
}
