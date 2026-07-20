import json
from typing import Dict, Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

import store
from model import load_model, predict

# 모델 · 입력 정의 로드 (startup 캐싱)
spec = json.load(open("feature_spec.json", encoding="utf-8"))
model = load_model("model.pkl")
LABELS = spec["target_labels"]

app = FastAPI(title=spec["title"] + " ML API",
              description="model.pkl 을 서빙하는 예측 엔드포인트 + 예측 이력 CRUD. "
                          "Spring Boot(BN)가 호출한다.")

# React/BN 에서 호출 허용
app.add_middleware(CORSMiddleware, allow_origins=["*"],
                   allow_methods=["*"], allow_headers=["*"])

store.init_db()      # 예측 이력 테이블 준비


class PredictRequest(BaseModel):
    features: Dict[str, Any]   # {"자치구코드":3, "전용면적":84.9, ...}


def run_model(features: Dict[str, Any]) -> Dict[str, Any]:
    """model.pkl 로 예측해 결과 dict 를 만든다 (predict/CRUD 공용)"""
    pred, proba = predict(model, features)
    pred = int(pred)
    return {
        "prediction": pred,
        "label": LABELS[pred],
        "proba": {LABELS[0]: round(float(proba[0]), 4),
                  LABELS[1]: round(float(proba[1]), 4)},
    }


@app.get("/health")
def health():
    return {"status": "UP", "model": spec["title"]}


@app.get("/spec")
def get_spec():
    """입력 항목 정의(라벨·범위·선택지) - FN 폼 자동 생성에 사용"""
    return spec


@app.post("/predict")
def do_predict(req: PredictRequest):
    """예측만 한다 (저장 안 함). BN 이 사용하는 엔드포인트."""
    return run_model(req.features)


# ---------------- 예측 이력 CRUD ----------------

@app.post("/predictions", status_code=201)
def create_prediction(req: PredictRequest):
    """C : 예측 후 이력에 저장"""
    return store.create(req.features, run_model(req.features))


@app.get("/predictions")
def list_predictions(limit: int = 50):
    """R : 이력 목록 (최신순)"""
    return store.list_all(limit)


@app.get("/predictions/{pid}")
def get_prediction(pid: int):
    """R : 이력 단건"""
    row = store.get(pid)
    if row is None:
        raise HTTPException(status_code=404, detail="해당 예측 이력이 없습니다")
    return row


@app.put("/predictions/{pid}")
def update_prediction(pid: int, req: PredictRequest):
    """U : 입력을 바꿔 다시 예측하고 이력을 갱신"""
    row = store.update(pid, req.features, run_model(req.features))
    if row is None:
        raise HTTPException(status_code=404, detail="해당 예측 이력이 없습니다")
    return row


@app.delete("/predictions/{pid}")
def delete_prediction(pid: int):
    """D : 이력 삭제"""
    if not store.delete(pid):
        raise HTTPException(status_code=404, detail="해당 예측 이력이 없습니다")
    return {"deleted": pid}
