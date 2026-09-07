import { createContext, useContext, useEffect, useState } from "react";
import { getMyAccount, login as loginApi } from "../api/authApi";
import { tokenStorage } from "../storage/tokenStorage";


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


    const logout = () => {

        tokenStorage.clearTokens();

        setUser(null);
    };


    const value = {
        user,
        isAuthenticated,
        loading,
        login,
        logout
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