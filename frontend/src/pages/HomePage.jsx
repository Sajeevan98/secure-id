import { Box, Button, Container, Paper, Typography } from '@mui/material';
import { useAuth } from '../context/AuthContext';

function HomePage() {
    const { user, logout } = useAuth();

    return (
        <Container maxWidth="md">
            <Box sx={{ mt: 6 }}>
                <Paper elevation={3} sx={{ p: 4 }}>
                    <Typography variant="h4" gutterBottom>
                        Welcome, {user?.username}
                    </Typography>

                    <Typography variant="body1">
                        You are successfully authenticated.
                    </Typography>

                    <Typography variant="body2" sx={{ mt: 2 }}>
                        Roles: {user?.roles?.join(', ')}
                    </Typography>

                    <Typography variant="body2">
                        Permissions: {user?.permissions?.join(', ')}
                    </Typography>

                    <Button
                        variant="outlined"
                        color="error"
                        sx={{ mt: 3 }}
                        onClick={logout}
                    >
                        Logout
                    </Button>
                </Paper>
            </Box>
        </Container>
    );
}

export default HomePage;