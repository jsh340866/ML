import { useState, useEffect } from "react";
import api from "../api/axiosConfig";

// 예측 페이지
//  FN -> BN(Spring Boot) -> ML(FastAPI) -> model.pkl
//  - /api/spec        : 입력 폼을 자동으로 만든다
//  - /api/predictions : 예측 이력 CRUD (C 저장 / R 목록 / U 수정 / D 삭제)
const Predict = () => {
    const [spec, setSpec] = useState(null);
    const [vals, setVals] = useState({});
    const [rows, setRows] = useState([]);
    const [editId, setEditId] = useState(null);   // null 이면 신규, 값이 있으면 수정 중
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    const initVals = (s) => {
        const v = {};
        s.features.forEach((f) => { v[f.name] = f.kind === "num" ? f.default : 0; });
        return v;
    };

    // R : 이력 목록
    const loadRows = () => {
        api.get("/api/predictions")
            .then((r) => setRows(r.data))
            .catch(() => setError("이력을 불러오지 못했습니다."));
    };

    // 입력 정의를 받아 폼 초기값 구성
    useEffect(() => {
        api.get("/api/spec")
            .then((r) => {
                setSpec(r.data);
                setVals(initVals(r.data));
                loadRows();
            })
            .catch(() => setError("모델 정보를 불러오지 못했습니다. 로그인 상태를 확인하세요."));
    }, []);

    const onChange = (name, v) => setVals((prev) => ({ ...prev, [name]: v }));

    // C(신규) 또는 U(수정)
    const onSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        try {
            const r = editId === null
                ? await api.post("/api/predictions", vals)            // C
                : await api.put(`/api/predictions/${editId}`, vals);  // U
            setResult(r.data);
            setEditId(null);
            setVals(initVals(spec));
            loadRows();
        } catch (err) {
            console.error(err);
            setError("예측 요청에 실패했습니다.");
        }
    };

    // 수정 시작 : 이력의 입력값을 폼에 채운다
    const onEdit = (row) => {
        setVals(row.features);
        setEditId(row.id);
        setResult(null);
    };

    const onCancel = () => {
        setEditId(null);
        setVals(initVals(spec));
    };

    // D : 삭제
    const onDelete = async (id) => {
        try {
            await api.delete(`/api/predictions/${id}`);
            if (editId === id) onCancel();
            loadRows();
        } catch (err) {
            setError("삭제에 실패했습니다.");
        }
    };

    if (!spec) {
        return (
            <div>
                <h1>PREDICT</h1>
                {error && <p style={{ color: "red" }}>{error}</p>}
            </div>
        );
    }

    return (
        <div>
            <h1>{spec.title}</h1>
            <p>정확도 {(spec.accuracy * 100).toFixed(1)}% · ROC-AUC {spec.roc_auc}</p>

            <h3>{editId === null ? "새 예측" : `이력 #${editId} 수정`}</h3>
            <form onSubmit={onSubmit}>
                {spec.features.map((f) => (
                    <div key={f.name} style={{ margin: "6px 0" }}>
                        <label>{f.label} : </label>
                        {f.kind === "num" ? (
                            <input
                                type="number"
                                value={vals[f.name]}
                                onChange={(e) => onChange(f.name, Number(e.target.value))}
                            />
                        ) : (
                            <select
                                value={vals[f.name]}
                                onChange={(e) => onChange(f.name, Number(e.target.value))}
                            >
                                {f.options.map((o, i) => (
                                    <option key={i} value={i}>{o}</option>
                                ))}
                            </select>
                        )}
                    </div>
                ))}
                <button type="submit">{editId === null ? "예측하고 저장" : "수정 저장"}</button>
                {editId !== null && (
                    <button type="button" onClick={onCancel} style={{ marginLeft: 6 }}>취소</button>
                )}
            </form>

            {error && <p style={{ color: "red" }}>{error}</p>}

            {result && (
                <div style={{ marginTop: 16 }}>
                    <h3>예측 결과 : {result.label}</h3>
                    <pre>{JSON.stringify(result.proba, null, 2)}</pre>
                </div>
            )}

            <h3 style={{ marginTop: 24 }}>예측 이력 ({rows.length})</h3>
            <table border="1" cellPadding="6">
                <thead>
                    <tr>
                        <th>ID</th><th>결과</th><th>입력</th><th>시각</th><th>관리</th>
                    </tr>
                </thead>
                <tbody>
                    {rows.map((r) => (
                        <tr key={r.id}>
                            <td>{r.id}</td>
                            <td>{r.label}</td>
                            <td>{JSON.stringify(r.features)}</td>
                            <td>{r.created_at}</td>
                            <td>
                                <button onClick={() => onEdit(r)}>수정</button>
                                <button onClick={() => onDelete(r.id)} style={{ marginLeft: 4 }}>삭제</button>
                            </td>
                        </tr>
                    ))}
                    {rows.length === 0 && (
                        <tr><td colSpan="5">이력이 없습니다. 위에서 예측을 해보세요.</td></tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};

export default Predict;
