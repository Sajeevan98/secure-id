import axios from 'axios';
import { tokenStorage } from '../storage/tokenStorage';

const axiosClient = axios.create({

    baseURL: import.meta.env.VITE_API_BASE_URL
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

export default axiosClient;