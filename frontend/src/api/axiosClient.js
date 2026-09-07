import axios from 'axios';
import { tokenStorage } from '../storage/tokenStorage';

const axiosClient = axios.create({

    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    }
});


axiosClient.interceptors.request.use(

    (config) => {
        const accessToken = tokenStorage.getAccessToken();

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);


axiosClient.interceptors.response.use(
    
    (response) => response,

    async (error) => {
        const originalRequest = error.config;

        if (
            error.response?.status === 401 &&
            !originalRequest._retry &&
            !originalRequest.url?.includes('/auth/refresh')
        ) {
            originalRequest._retry = true;

            const refreshToken = tokenStorage.getRefreshToken();

            if (!refreshToken) {
                tokenStorage.clearTokens();
                return Promise.reject(error);
            }

            try {
                const response = await axios.post(
                    `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
                    {
                        refreshToken
                    }
                );

                const {
                    accessToken,
                    refreshToken: newRefreshToken
                } = response.data.data;

                tokenStorage.setTokens({
                    accessToken,
                    refreshToken: newRefreshToken
                });

                originalRequest.headers.Authorization = `Bearer ${accessToken}`;

                return axiosClient(originalRequest);

            } catch (refreshError) {
                tokenStorage.clearTokens();

                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default axiosClient;