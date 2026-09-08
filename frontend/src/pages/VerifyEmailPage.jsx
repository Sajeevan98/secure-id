import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Alert, Box, Button, CircularProgress, Container, Paper, Typography } from '@mui/material';
import { verifyEmail } from '../api/authApi';

function VerifyEmailPage() {

  const verificationStarted = useRef(false);

  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [status, setStatus] = useState('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {

    if (verificationStarted.current) {
      return;
    }

    verificationStarted.current = true;

    const token = searchParams.get('token');

    if (!token) {
      setStatus('error');
      setMessage('Email verification token is missing.');
      return;
    }

    const verify = async () => {

      try {
        const response = await verifyEmail(token);

        setStatus('success');
        setMessage(
          response.data?.data?.message ||
          'Your email has been verified successfully.'
        );

      } catch (error) {

        const errorMessage =
          error.response?.data?.error?.message ||
          'Email verification failed. The link may be invalid or expired.';

        setStatus('error');
        setMessage(errorMessage);
      }
    };

    verify();

  }, [searchParams]);

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8 }}>
        <Paper
          elevation={3}
          sx={{
            p: 4,
            textAlign: 'center',
          }}
        >

          {status === 'loading' && (
            <>
              <CircularProgress />

              <Typography variant="h5" sx={{ mt: 3 }}>
                Verifying your email...
              </Typography>
            </>
          )}

          {status === 'success' && (
            <>
              <Typography variant="h5" gutterBottom>
                Email Verified
              </Typography>

              <Alert severity="success" sx={{ mb: 3 }}>
                {message}
              </Alert>

              <Button
                variant="contained"
                onClick={() => navigate('/login')}
              >
                Go To Login
              </Button>
            </>
          )}

          {status === 'error' && (
            <>
              <Typography variant="h5" gutterBottom>
                Verification Failed
              </Typography>

              <Alert severity="error" sx={{ mb: 3 }}>
                {message}
              </Alert>

              <Button
                variant="outlined"
                onClick={() => navigate('/login')}
              >
                Go To Login
              </Button>
            </>
          )}

        </Paper>
      </Box>
    </Container>
  );
}

export default VerifyEmailPage;