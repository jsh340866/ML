import joblib
import pandas as pd


def load_model(path="model.pkl"):
    """학습된 모델 로드"""
    return joblib.load(path)


def predict(model, input_data):
    """입력 dict -> (예측 클래스, 확률 배열)"""
    df = pd.DataFrame([input_data])
    return model.predict(df)[0], model.predict_proba(df)[0]
