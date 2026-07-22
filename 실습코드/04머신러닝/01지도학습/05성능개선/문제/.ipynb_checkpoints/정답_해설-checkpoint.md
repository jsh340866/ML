# 문제 2 (WineQuality 튜닝) 정답 해설

> 정답 노트북: 정답.ipynb (같은 폴더)

## 풀이 2단계
1. baseline: RandomForestRegressor 교차검증 평균 R2
2. GridSearchCV로 n_estimators/max_depth 튜닝 -> 최적 파라미터 + CV R2

## 해설 포인트
- 08(당뇨)과 동일한 성능개선 흐름을 새 공공데이터에 적용.
- 튜닝은 후보 x fold 만큼 학습하므로 시간이 든다 -> 후보를 신중히.
- 성능 향상이 작다면, 데이터/특성 개선이 하이퍼파라미터 튜닝보다 효과가 클 수 있다.
