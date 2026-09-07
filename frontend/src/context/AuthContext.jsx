import { createContext, useContext, useEffect, useState } from "react";
import { tokenStorage } from "../storage/tokenStorage";
import { setAuthFailureHandler } from "../auth/authFailureHandler";
import {
    getMyAccount,
    login as loginApi,
    logout as logoutApi,
    refreshAccessToken
} from "../api/authApi";


const AuthContext = createContext(null);

export function AuthProvider({ children }) {

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const isAuthenticated = !!user;

    useEffect(() => {
        const restoreAuthentication = async () => {

            const accessToken = tokenStorage.getAccessToken();

            if (!accessToken) {
                setLoading(false);
                return;
            }

            try {

                const response = await getMyAccount();
                setUser(response.data);

            } catch (error) {

                console.error("Failed to restore authentication:", error);
                tokenStorage.clearTokens();
                setUser(null);

            } finally {

                setLoading(false);
            }
        };

        restoreAuthentication();
    }, []);

    
    useEffect(() => {

        setAuthFailureHandler(() => {
            tokenStorage.clearTokens();
            setUser(null);
        });

        return () => {
            setAuthFailureHandler(null);
        };
    }, []);


    const login = async (email, password) => {

        const response = await loginApi({
            email,
            password
        });

        const { accessToken, refreshToken } = response.data;

        tokenStorage.setTokens({
            accessToken,
            refreshToken,
        });

        const accountResponse = await getMyAccount();

        setUser(accountResponse.data);

        return response;
    };


    const logout = async () => {
        const refreshToken = tokenStorage.getRefreshToken();

        try {
            if (refreshToken) {
                await logoutApi(refreshToken);
            }
        } catch (error) {
            console.error("Backend logout failed:", error);
        } finally {
            tokenStorage.clearTokens();
            setUser(null);
        }
    };

    const refresh = async () => {
        const refreshToken = tokenStorage.getRefreshToken();

        if (!refreshToken) {
            throw new Error("Refresh token is not available");
        }

        const response = await refreshAccessToken(refreshToken);

        const {
            accessToken,
            refreshToken: newRefreshToken
        } = response.data;

        tokenStorage.setTokens({
            accessToken,
            refreshToken: newRefreshToken
        });

        return accessToken;
    };


    const value = {
        user,
        isAuthenticated,
        loading,
        login,
        logout,
        refresh
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}


export function useAuth() {

    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }

    return context;
}