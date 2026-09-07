let authFailureHandler = null;

export const setAuthFailureHandler = (handler) => {
    
    authFailureHandler = handler;
};

export const notifyAuthFailure = () => {

    if (authFailureHandler) {
        authFailureHandler();
    }
};