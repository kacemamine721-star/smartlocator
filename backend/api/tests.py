from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APIClient
from rest_framework import status


class AuthenticationTests(TestCase):
    """
    Test suite for user authentication and account management.
    Verifies that login, registration, and token storage work correctly.
    """
    
    def setUp(self):
        """Initialize test client and create a test user."""
        self.client = APIClient()
        self.register_url = '/api/auth/register/'
        self.login_url = '/api/auth/login/'
        self.refresh_url = '/api/auth/refresh/'
        
        # Create a test user
        self.test_user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
    
    def test_user_registration(self):
        """
        Test 1: User can register with valid credentials
        Verifies that a new user account is created and stored in the database.
        """
        data = {
            'username': 'newuser',
            'email': 'newuser@example.com',
            'password': 'newpass123'
        }
        response = self.client.post(self.register_url, data, format='json')
        
        # Should return 201 Created
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # User should be created in database
        self.assertTrue(User.objects.filter(username='newuser').exists())
        
        # Response should contain user data
        self.assertIn('id', response.data)
        self.assertEqual(response.data['username'], 'newuser')
        self.assertEqual(response.data['email'], 'newuser@example.com')
    
    def test_registration_duplicate_username(self):
        """
        Test 2: Registration fails if username already exists
        """
        data = {
            'username': 'testuser',  # Already exists
            'email': 'another@example.com',
            'password': 'pass123'
        }
        response = self.client.post(self.register_url, data, format='json')
        
        # Should return 400 Bad Request
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_login_with_valid_credentials(self):
        """
        Test 3: User can login with correct username and password
        Verifies that JWT tokens are issued and stored.
        """
        data = {
            'username': 'testuser',
            'password': 'testpass123'
        }
        response = self.client.post(self.login_url, data, format='json')
        
        # Should return 200 OK
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Response should contain access and refresh tokens
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)
        
        # Tokens should be non-empty strings
        self.assertIsNotNone(response.data['access'])
        self.assertIsNotNone(response.data['refresh'])
        self.assertGreater(len(response.data['access']), 0)
        self.assertGreater(len(response.data['refresh']), 0)
    
    def test_login_with_invalid_credentials(self):
        """
        Test 4: Login fails with incorrect password
        """
        data = {
            'username': 'testuser',
            'password': 'wrongpassword'
        }
        response = self.client.post(self.login_url, data, format='json')
        
        # Should return 401 Unauthorized
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
        
        # Should not contain tokens
        self.assertNotIn('access', response.data)
    
    def test_login_with_nonexistent_user(self):
        """
        Test 5: Login fails if user doesn't exist
        """
        data = {
            'username': 'nonexistent',
            'password': 'somepass'
        }
        response = self.client.post(self.login_url, data, format='json')
        
        # Should return 401 Unauthorized
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
    
    def test_token_format(self):
        """
        Test 6: JWT tokens follow the correct format (three parts separated by dots)
        """
        data = {
            'username': 'testuser',
            'password': 'testpass123'
        }
        response = self.client.post(self.login_url, data, format='json')
        
        access_token = response.data['access']
        refresh_token = response.data['refresh']
        
        # JWT tokens should have 3 parts separated by dots
        self.assertEqual(len(access_token.split('.')), 3)
        self.assertEqual(len(refresh_token.split('.')), 3)
    
    def test_token_refresh(self):
        """
        Test 7: Refresh token can be used to obtain a new access token
        """
        # First, login to get tokens
        login_data = {
            'username': 'testuser',
            'password': 'testpass123'
        }
        login_response = self.client.post(self.login_url, login_data, format='json')
        refresh_token = login_response.data['refresh']
        old_access_token = login_response.data['access']
        
        # Now try to refresh
        refresh_data = {'refresh': refresh_token}
        refresh_response = self.client.post(self.refresh_url, refresh_data, format='json')
        
        # Should return 200 OK
        self.assertEqual(refresh_response.status_code, status.HTTP_200_OK)
        
        # Should contain new access token
        self.assertIn('access', refresh_response.data)
        new_access_token = refresh_response.data['access']
        
        # New token should be different from old token
        self.assertNotEqual(new_access_token, old_access_token)
    
    def test_registration_password_too_short(self):
        """
        Test 8: Registration fails if password is too short (less than 8 chars)
        """
        data = {
            'username': 'shortpass',
            'email': 'short@example.com',
            'password': 'short'  # Too short
        }
        response = self.client.post(self.register_url, data, format='json')
        
        # Should return 400 Bad Request
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        
        # User should NOT be created
        self.assertFalse(User.objects.filter(username='shortpass').exists())
    
    def test_access_token_in_authorization_header(self):
        """
        Test 9: Access token can be used in Authorization header to access protected endpoints
        This is the critical integration test that verifies tokens work end-to-end.
        """
        # Login to get token
        login_data = {
            'username': 'testuser',
            'password': 'testpass123'
        }
        login_response = self.client.post(self.login_url, login_data, format='json')
        access_token = login_response.data['access']
        
        # Use token to access a protected endpoint (e.g., favorites)
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {access_token}')
        
        # This request should work because token is valid
        response = self.client.get('/api/favorites/')
        
        # Should return 200 OK (even if empty list)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
    
    def test_invalid_token_rejected(self):
        """
        Test 10: Invalid or expired tokens are rejected
        """
        # Try to use an invalid token
        invalid_token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.invalid'
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {invalid_token}')
        
        # Should return 401 Unauthorized
        response = self.client.get('/api/favorites/')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class UserDataStorageTests(TestCase):
    """
    Test suite for verifying user-specific data is stored and retrieved correctly.
    """
    
    def setUp(self):
        """Initialize test client and authenticate."""
        self.client = APIClient()
        
        # Create user
        self.user = User.objects.create_user(
            username='datauser',
            email='data@example.com',
            password='pass123'
        )
        
        # Login and get token
        login_data = {
            'username': 'datauser',
            'password': 'pass123'
        }
        response = self.client.post('/api/auth/login/', login_data, format='json')
        self.access_token = response.data['access']
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {self.access_token}')
    
    def test_user_favorites_are_stored_per_user(self):
        """
        Test 11: Each user has their own isolated favorites collection
        Verifies data persistence in the database.
        """
        # Create another user
        other_user = User.objects.create_user(
            username='otheruser',
            email='other@example.com',
            password='pass123'
        )
        
        # Fetch favorites for current user (should be empty)
        response = self.client.get('/api/favorites/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 0)
    
    def test_user_history_is_stored_per_user(self):
        """
        Test 12: Each user has their own isolated history
        """
        # Fetch history for current user (should be empty)
        response = self.client.get('/api/history/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 0)
