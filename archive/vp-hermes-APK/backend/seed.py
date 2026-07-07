"""VisePanda 种子数据管理脚本。
用法：
  python seed.py            # 创建管理员用户 + 城市数据
  python seed.py --admin    # 仅创建管理员
"""

import argparse
import sys
import os

# 确保能找到 app 包
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import SessionLocal, engine, Base
from app.models.user import User
from app.models.destination import Destination
from app.core.security import hash_password
from app.seed.destinations import SEED_DESTINATIONS
from datetime import datetime


def seed_admin():
    db = SessionLocal()
    try:
        existing = db.query(User).filter(User.email == "admin@visepanda.com").first()
        if existing:
            print("[SEED] Admin user already exists (email=admin@visepanda.com)")
            return

        admin = User(
            email="admin@visepanda.com",
            password_hash=hash_password("admin123"),
            display_name="VisePanda Admin",
            role="admin",
            status="active",
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow(),
        )
        db.add(admin)
        db.commit()
        print(f"[SEED] Created admin user: admin@visepanda.com / admin123")
    finally:
        db.close()


def seed_destinations():
    db = SessionLocal()
    try:
        count = db.query(Destination).count()
        if count > 0:
            print(f"[SEED] {count} destinations already exist, skipping.")
            return
        for data in SEED_DESTINATIONS:
            dest = Destination(**data)
            db.add(dest)
        db.commit()
        print(f"[SEED] Imported {len(SEED_DESTINATIONS)} destinations.")
    finally:
        db.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--admin", action="store_true", help="Only seed admin user")
    args = parser.parse_args()

    Base.metadata.create_all(bind=engine)

    if args.admin:
        seed_admin()
    else:
        seed_admin()
        seed_destinations()

    print("[SEED] Done.")
