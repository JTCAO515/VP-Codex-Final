from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Database
    database_url: str = "postgresql://visepanda:visepanda@localhost:5432/visepanda"

    # JWT
    jwt_secret: str = "change-me-in-production-vp-trae-2026"
    jwt_algorithm: str = "HS256"
    jwt_expire_minutes: int = 1440  # 24h

    # SMTP (mock for now)
    smtp_host: str = "localhost"
    smtp_port: int = 1025
    smtp_username: str = ""
    smtp_password: str = ""
    smtp_from: str = "noreply@visepanda.com"

    # App
    debug: bool = True
    cors_origins: list[str] = ["*"]

    class Config:
        env_file = ".env"


settings = Settings()
