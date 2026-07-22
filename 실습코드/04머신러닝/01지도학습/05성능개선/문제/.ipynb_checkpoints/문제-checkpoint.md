# 08 문제 2 (공공데이터) - 와인 품질 튜닝

## 데이터 (공공데이터, 로그인 불필요)
```python
import pandas as pd
df = pd.read_csv('https://raw.githubusercontent.com/plotly/datasets/master/winequality-red.csv')
y = df['quality']; X = df.drop(columns=['quality'])
```

## 문제
08의 성능개선 기법을 이 데이터에 적용하라.
1. `RandomForestRegressor`의 교차검증 평균 R2(baseline)를 구하라.
2. GridSearchCV로 `n_estimators`, `max_depth`를 튜닝해 최적 파라미터와 CV R2를 구하라.
3. **(생각해보기)** 튜닝으로 성능이 얼마나 올랐나? 튜닝 시간과 성능 향상의 트레이드오프를 생각해보라.

## 정답/해설
- 정답 노트북: 정답.ipynb (같은 폴더) · 해설: [정답_해설.md](정답_해설.md)
