import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Alert, Box, Button, Container, Paper, TextField, Typography, } from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword, validatePasswordResetToken, } from '../api/authApi';

function ResetPasswordPage() {

  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const validationStarted = useRef(false);

  const token = searchParams.get('token');

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      newPassword: '',
      confirmPassword: '',
    },
  });

  const newPassword = watch('newPassword');

  useEffect(() => {

    if (validationStarted.current) {
      return;
    }

    validationStarted.current = true;

    if (!token) {
      setStatus('error');
      setMessage('Password reset token is missing.');
      return;
    }

    const validateToken = async () => {

      try {
        await validatePasswordResetToken(token);

        setStatus('valid');

      } catch (error) {

        const errorCode = error.response?.data?.error?.code;

        if (errorCode === 'AUTH_PASSWORD_RESET_TOKEN_USED') {
          setMessage('Password reset link has already been used.');
        } else if (errorCode === 'AUTH_PASSWORD_RESET_TOKEN_EXPIRED') {
          setMessage('Password reset link has expired.');
        } else if (errorCode === 'AUTH_INVALID_PASSWORD_RESET_TOKEN') {
          setMessage('Invalid password reset link.');
        } else {
          setMessage(
            error.response?.data?.error?.message ||
            'Unable to validate the password reset link.'
          );
        }

        setStatus('error');
      }
    };

    validateToken();

  }, [token]);

  const onSubmit = async (data) => {

    setStatus('loading');
    setMessage('');

    try {

      await resetPassword({
        token,
        newPassword: data.newPassword,
      });

      setStatus('success');
      setMessage('Your password has been reset successfully.');

    } catch (error) {

      const errorCode = error.response?.data?.error?.code;

      if (errorCode === 'AUTH_PASSWORD_RESET_TOKEN_USED') {
        setMessage('Password reset link has already been used.');

      } else if (errorCode === 'AUTH_PASSWORD_RESET_TOKEN_EXPIRED') {
        setMessage('Password reset link has expired.');

      } else if (errorCode === 'AUTH_INVALID_PASSWORD_RESET_TOKEN') {
        setMessage('Invalid password reset link.');

      } else {
        setMessage(
          error.response?.data?.error?.message || 'Unable to reset your password. Please try again.'
        );
      }

      setStatus('error');
    }
  };

  if (status === 'loading') {
    return (
      <Container maxWidth="sm">
        <Box sx={{ mt: 8 }}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h5" gutterBottom>
              Validating reset link...
            </Typography>

            <Typography color="text.secondary">
              Please wait.
            </Typography>
          </Paper>
        </Box>
      </Container>
    );
  }

  if (status === 'error') {
    return (
      <Container maxWidth="sm">
        <Box sx={{ mt: 8 }}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h5" gutterBottom>
              Password Reset
            </Typography>

            <Alert severity="error" sx={{ mb: 3 }}>
              {message}
            </Alert>

            <Button
              variant="contained"
              onClick={() => navigate('/forgot-password')}
            >
              Request New Reset Link
            </Button>
          </Paper>
        </Box>
      </Container>
    );
  }

  if (status === 'success') {
    return (
      <Container maxWidth="sm">
        <Box sx={{ mt: 8 }}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h5" gutterBottom>
              Password Reset
            </Typography>

            <Alert severity="success" sx={{ mb: 3 }}>
              {message}
            </Alert>

            <Button
              variant="contained"
              onClick={() => navigate('/login')}
            >
              Go to Login
            </Button>
          </Paper>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8 }}>
        <Paper sx={{ p: 4 }}>

          <Typography variant="h5" gutterBottom>
            Reset Password
          </Typography>

          <Typography
            color="text.secondary"
            sx={{ mb: 3 }}
          >
            Enter your new password below.
          </Typography>

          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
          >

            <TextField
              fullWidth
              type="password"
              label="New Password"
              margin="normal"
              {...register('newPassword', {
                required: 'New password is required.',
                minLength: {
                  value: 8,
                  message: 'Password must be at least 8 characters.',
                },
              })}
              error={!!errors.newPassword}
              helperText={errors.newPassword?.message}
            />

            <TextField
              fullWidth
              type="password"
              label="Confirm Password"
              margin="normal"
              {...register('confirmPassword', {
                required: 'Please confirm your password.',
                validate: (value) =>
                  value === newPassword || 'Passwords do not match.',
              })}
              error={!!errors.confirmPassword}
              helperText={errors.confirmPassword?.message}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3 }}
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Resetting...' : 'Reset Password'}
            </Button>

          </Box>

        </Paper>
      </Box>
    </Container>
  );
}

export default ResetPasswordPage;