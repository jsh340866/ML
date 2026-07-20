import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
// import axios from "axios"
import axios from "axios"
import api from "../api/axiosConfig"

// 로그아웃 : 진입 즉시 BN /logout 호출 → BN이 Redis RT 삭제 + 쿠키 제거 → /login 이동
const Logout = () => {
    const navigate = useNavigate();
    useEffect(() => {
        const performLogout = async () => {
            try {
                // await axios.post("http://localhost:8080/logout", {}, { withCredentials: true });
                await api.post("/logout");
            } catch (error) {
                console.error("로그아웃 실패:", error);
            } finally {
                navigate("/login"); // 성공/실패 무관하게 로그인으로
            }
        };
        performLogout();
    }, [navigate]);
    return (
        <div>
            <h1>LOGOUT PAGE</h1>
            <p>로그아웃 처리중...</p>
        </div>
    );
};
export default Logout;
