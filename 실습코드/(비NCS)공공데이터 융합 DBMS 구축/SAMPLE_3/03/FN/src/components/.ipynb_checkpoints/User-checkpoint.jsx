import { useState, useEffect } from "react";
import axios from "axios"
import api from "../api/axiosConfig"
// 보호 페이지 : api 인스턴스로 호출 → 요청 인터셉터가 /validate 선확인 후 전송
const User = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [error, setError] = useState(null);
    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                // const resp = await axios.get("http://localhost:8080/user", {withCredentials: true}); // 쿠키의 JWT로 BN이 인증
                const resp = await api.get("/user"); // 쿠키의 JWT로 BN이 인증
                console.log(resp)
                setUserInfo(resp.data);               // { username, role }
                setError(null);
            } catch (err) {
                console.error("User info fetch error:", err);
                setError("사용자 정보를 가져오는 데 실패했습니다.");
            }
        };
        fetchUserInfo();
    }, []);
    return (
        <div>
            <h1>USER PAGE</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            {userInfo && (
                <div>
                    <p>Username : {userInfo.username}</p>
                    <p>Role : {userInfo.role}</p>
                </div>
            )}
        </div>
    );
};

export default User;
