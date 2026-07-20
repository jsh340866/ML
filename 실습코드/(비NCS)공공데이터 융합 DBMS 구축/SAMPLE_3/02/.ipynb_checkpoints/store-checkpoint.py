import json
import sqlite3
from datetime import datetime

# 예측 이력 저장소 (SQLite). 별도 설치 없이 파이썬 표준 라이브러리만 사용한다.
DB_PATH = "predictions.db"


def _con():
    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row      # 결과를 dict 처럼 사용
    return con


def init_db():
    """예측 이력 테이블 생성 (앱 시작 시 1회)"""
    con = _con()
    try:
        con.execute("""
            CREATE TABLE IF NOT EXISTS predictions (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                features   TEXT NOT NULL,
                prediction INTEGER NOT NULL,
                label      TEXT NOT NULL,
                proba      TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
        """)
        con.commit()
    finally:
        con.close()


def _to_dict(r):
    return {
        "id": r["id"],
        "features": json.loads(r["features"]),
        "prediction": r["prediction"],
        "label": r["label"],
        "proba": json.loads(r["proba"]),
        "created_at": r["created_at"],
    }


def create(features, result):
    """C : 예측 결과를 저장하고 저장된 레코드를 돌려준다"""
    con = _con()
    try:
        cur = con.execute(
            "INSERT INTO predictions(features, prediction, label, proba, created_at)"
            " VALUES (?, ?, ?, ?, ?)",
            (json.dumps(features, ensure_ascii=False), result["prediction"], result["label"],
             json.dumps(result["proba"], ensure_ascii=False),
             datetime.now().isoformat(timespec="seconds")),
        )
        con.commit()
        pid = cur.lastrowid
    finally:
        con.close()
    return get(pid)


def get(pid):
    """R : 단건 조회 (없으면 None)"""
    con = _con()
    try:
        r = con.execute("SELECT * FROM predictions WHERE id = ?", (pid,)).fetchone()
    finally:
        con.close()
    return _to_dict(r) if r else None


def list_all(limit=50):
    """R : 목록 조회 (최신순)"""
    con = _con()
    try:
        rows = con.execute(
            "SELECT * FROM predictions ORDER BY id DESC LIMIT ?", (limit,)).fetchall()
    finally:
        con.close()
    return [_to_dict(r) for r in rows]


def update(pid, features, result):
    """U : 입력을 바꿔 다시 예측한 결과로 갱신 (없으면 None)"""
    con = _con()
    try:
        cur = con.execute(
            "UPDATE predictions SET features = ?, prediction = ?, label = ?, proba = ?"
            " WHERE id = ?",
            (json.dumps(features, ensure_ascii=False), result["prediction"], result["label"],
             json.dumps(result["proba"], ensure_ascii=False), pid),
        )
        con.commit()
        changed = cur.rowcount
    finally:
        con.close()
    return get(pid) if changed else None


def delete(pid):
    """D : 삭제 (지운 게 있으면 True)"""
    con = _con()
    try:
        cur = con.execute("DELETE FROM predictions WHERE id = ?", (pid,))
        con.commit()
        return cur.rowcount > 0
    finally:
        con.close()
