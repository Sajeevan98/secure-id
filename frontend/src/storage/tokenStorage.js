const ACCESS_TOKEN_KEY = 'secureid_access_token';
const REFRESH_TOKEN_KEY = 'secureid_refresh_token';

export const tokenStorage = {
    
    getAccessToken() {
        return sessionStorage.getItem(ACCESS_TOKEN_KEY);
    },

    getRefreshToken() {
        return sessionStorage.getItem(REFRESH_TOKEN_KEY);
    },

    setTokens({ accessToken, refreshToken }) {
        sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
        sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    },

    clearTokens() {
        sessionStorage.removeItem(ACCESS_TOKEN_KEY);
        sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    },
};