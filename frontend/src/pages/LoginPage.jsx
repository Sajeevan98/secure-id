import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Alert, Box, Button, Container, Paper, TextField, Typography, } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { resendVerification } from '../api/authApi';

function LoginPage() {

  const [serverError, setServerError] = useState('');
  const [emailNotVerified, setEmailNotVerified] = useState(false);
  const [resendMessage, setResendMessage] = useState('');
  const [resendError, setResendError] = useState('');
  const [isResending, setIsResending] = useState(false);

  const { login } = useAuth();

  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: { email: '', password: '' }
  });

  const onSubmit = async (data) => {
    setServerError('');
    setEmailNotVerified(false);
    setResendMessage('');
    setResendError('');

    try {
      await login(data.email, data.password);

      navigate("/");

    } catch (error) {

      const errorCode = error.response?.data?.error?.code;

      const message =
        error.response?.data?.error?.message ||
        'Unable to login. Please try again.';

      setServerError(message);

      if (errorCode === 'AUTH_EMAIL_NOT_VERIFIED') {
        setEmailNotVerified(true);
      }
    }
  };

  const handleResendVerification = async () => {
    setResendMessage('');
    setResendError('');
    setIsResending(true);

    try {
      const email = getValues('email');

      await resendVerification(email);

      setResendMessage(
        'A new verification email has been sent. Please check your inbox.'
      );

    } catch (error) {

      const message =
        error.response?.data?.error?.message ||
        'Unable to resend verification email. Please try again.';

      setResendError(message);

    } finally {
      setIsResending(false);
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

          {emailNotVerified && (
            <Box sx={{ mb: 2 }}>
              <Button
                variant="outlined"
                fullWidth
                onClick={handleResendVerification}
                disabled={isResending}
              >
                {isResending
                  ? 'Sending...'
                  : 'Resend verification email'}
              </Button>
            </Box>
          )}

          {resendMessage && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {resendMessage}
            </Alert>
          )}

          {resendError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {resendError}
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
