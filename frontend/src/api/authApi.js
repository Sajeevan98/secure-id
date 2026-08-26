import axiosClient from "./axiosClient";

export const register = async (data) => {

    const response = await axiosClient.post('/auth/registration', data);
    return response.data;
}

export const login = async (data) => {

    const response = await axiosClient.post('/auth/login', data);
    return response.data;
};

export const verifyEmail = async (token) => {

    const response = await axiosClient.get('/auth/verify-email', {
        params: {
            token,
        },
    });
    return response.data;
};

export const resendVerification = async (email) => {

    const response = await axiosClient.post('/auth/resend-verification', {
        email,
    });
    return response.data;
};

export const requestPasswordReset = async (email) => {

    const response = await axiosClient.post('/auth/forgot-password', {
        email,
    });
    return response.data;
};

export const resetPassword = async (data) => {

    const response = await axiosClient.post('/auth/reset-password', data);
    return response.data;
};

export const getMyAccount = async () => {

    const response = await axiosClient.get('/accounts/me');
    return response.data;
};