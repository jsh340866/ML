import axios from 'axios';

// -------------------------------------------------------------
// 공용 axios 인스턴스
//  - baseURL : 현재 BN 주소 (Spring Boot :8080)
//  - withCredentials : HttpOnly 쿠키(access-token, username)를 요청에 자동 동봉
//    → BN SecurityConfig 의 allowCredentials(true) 와 한 쌍
// -------------------------------------------------------------
const api = axios.create({
  // baseURL: 'http://localhost:8080',
  baseURL: process.env.REACT_APP_API_URL,
  withCredentials: true,
});

// -------------------------------------------------------------
// 요청 인터셉터 : 보호 API 호출 전 /validate 로 로그인 여부 확인
// -------------------------------------------------------------
api.interceptors.request.use(
  async (config) => {
    // 인증이 필요 없는 공개 경로는 검사 제외
    const publicPaths = ['/login', '/join'];
    if (publicPaths.some((path) => config.url.includes(path))) {
      return config;
    }

    try {
      // 쿠키 유효성 확인 (전역 axios 라 withCredentials 직접 명시)
      await axios.get(`${process.env.REACT_APP_API_URL}/validate`, { withCredentials: true });
      console.log('[요청 인터셉터] 인증된 상태');
      return config;
    } catch (error) {
      console.log('[요청 인터셉터] 미인증 → /login 이동', error);
      window.location.href = '/login';
      return Promise.reject('인증이 필요합니다.');
    }
  },
  (error) => {
    window.location.href = '/login';
    return Promise.reject(error);
  }
);

// // -------------------------------------------------------------
// // 응답 인터셉터 : 만료/미인증 응답 후처리
// // -------------------------------------------------------------
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
