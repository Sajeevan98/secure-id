import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Alert, Box, Button, Container, Paper, TextField, Typography, } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { requestPasswordReset } from '../api/authApi';

function ForgotPasswordPage() {

  const [serverError, setServerError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    defaultValues: {
      email: '',
    },
  });

  const onSubmit = async (data) => {
    setServerError('');
    setSuccessMessage('');

    try {
      await requestPasswordReset(data.email);

      setSuccessMessage(
        'If an account exists with this email address, a password reset email has been sent.'
      );
    } catch (error) {
      const message =
        error.response?.data?.error?.message ||
        'Unable to process your request. Please try again.';

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
            Forgot Password
          </Typography>

          <Typography
            variant="body1"
            color="text.secondary"
            sx={{ mb: 2 }}
          >
            Enter your email address and we'll send you a link to
            reset your password.
          </Typography>

          {serverError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {serverError}
            </Alert>
          )}

          {successMessage && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {successMessage}
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

            <Button
              fullWidth
              type="submit"
              variant="contained"
              disabled={isSubmitting}
              sx={{ mt: 2 }}
            >
              {isSubmitting ? 'Sending...' : 'Send reset link'}
            </Button>

            <Button
              fullWidth
              variant="text"
              onClick={() => navigate('/login')}
              sx={{ mt: 1 }}
            >
              Back to Login
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}

export default ForgotPasswordPage;
