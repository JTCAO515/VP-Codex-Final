package space.jtcao.visepanda.data.auth

object AuthDependencies {
    val repository: AuthRepositoryImpl by lazy {
        AuthRepositoryImpl()
    }
}
