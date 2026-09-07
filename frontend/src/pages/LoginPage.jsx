import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Alert, Box, Button, Container, Paper, TextField, Typography } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

function LoginPage() {

  const [serverError, setServerError] = useState('');

  const { login } = useAuth();

  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },

  } = useForm({
    defaultValues: { email: '', password: '' }
  });


  const onSubmit = async (data) => {
    setServerError('');

    try {

      await login(data.email, data.password);
      // console.log('Login successful');

      navigate("/");

    } catch (error) {

      const message = error.response?.data?.error?.message || 'Unable to login. Please try again.';

      setServerError(message);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            width: '100%',
            padding: 4,
          }}
        >
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
          >
            Login
          </Typography>

          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}

          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
          >
            <TextField
              fullWidth
              label="Email"
              type="email"
              margin="normal"
              {...register('email', {
                required: 'Email is required',
              })}
              error={!!errors.email}
              helperText={errors.email?.message}
            />

            <TextField
              fullWidth
              label="Password"
              type="password"
              margin="normal"
              {...register('password', {
                required: 'Password is required',
              })}
              error={!!errors.password}
              helperText={errors.password?.message}
            />

            <Button
              fullWidth
              type="submit"
              variant="contained"
              disabled={isSubmitting}
              sx={{ mt: 2 }}
            >
              {isSubmitting ? 'Logging in...' : 'Login'}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}

export default LoginPage;