import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig"

const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const navigate = useNavigate();

    // useEffect(() => {

    //     const validateToken = async () => {
    //         try {
    //             const resp = await axios.get("http://localhost:8080/validate", {withCredentials: true,});
    //             console.log("토큰검증 성공 : ", resp);
    //             navigate("/")
    //         } catch (error) {
    //             console.log("토큰검증실패..", error);
    //         }
    //     }
    //     validateToken();

    // }, [navigate])

    const handleLogin = async () => {
        try {
            // const resp = await axios.post(
            //     "http://localhost:8080/login",
            //     { username, password },
            //     { headers: { "Content-Type": "application/json" } , withCredentials: true }, 
            // );
            const resp = await api.post("/login",
                { username, password },
                { headers: { "Content-Type": "application/json" }},
            );
            alert("로그인 성공:", resp.data);
            console.log(resp.data);
            navigate("/"); // 성공 시 / 경로로 이동
        } catch (error) {
            console.error("로그인 실패:", error.response ? error.response.data : error);
            alert("로그인 실패! 다시 시도해주세요."); // 실패 시 메시지 표시
        }
    }
    return (
        <div>
            <h1>LOGIN PAGE</h1>
            USERNAME : <input type="text" onChange={(e) => setUsername(e.target.value)} /><br />
            PASSWORD : <input type="password" onChange={(e) => setPassword(e.target.value)} /><br />
            <button onClick={handleLogin}>LOGIN</button>
        </div>
    );
};

export default Login;
