from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APIClient
from rest_framework import status
from .models import ChargingStation, Favorite, HistorySession, ContributedStation, CommunityAlert


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
        """Test 1: User can register with valid credentials."""
        data = {
            'username': 'newuser',
            'email': 'newuser@example.com',
            'password': 'newpass123'
        }
        response = self.client.post(self.register_url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(User.objects.filter(username='newuser').exists())
        self.assertEqual(response.data['username'], 'newuser')
    
    def test_login_with_valid_credentials(self):
        """Test 2: User can login and receive JWT tokens."""
        data = {
            'username': 'testuser',
            'password': 'testpass123'
        }
        response = self.client.post(self.login_url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)
    
    def test_login_with_invalid_credentials(self):
        """Test 3: Login fails with incorrect password."""
        data = {
            'username': 'testuser',
            'password': 'wrongpassword'
        }
        response = self.client.post(self.login_url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class StationTests(TestCase):
    """
    Test suite for Charging Station visibility and discovery.
    """
    
    def setUp(self):
        self.client = APIClient()
        self.station = ChargingStation.objects.create(
            station_id="TEST001",
            name="Test Station Alpha",
            address="123 Test St",
            city="Tunis",
            availability="Available",
            power="150 kW",
            ports="6",
            network="ChargeTN",
            price="0.95 TND/kWh",
            reliability="96%",
            latitude=36.8,
            longitude=10.2
        )

    def test_list_stations_publicly(self):
        """Test 4: Map stations are accessible without authentication."""
        response = self.client.get('/api/stations/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['name'], "Test Station Alpha")

    def test_filter_stations_by_city(self):
        """Test 5: Stations can be filtered via query parameters."""
        ChargingStation.objects.create(
            station_id="TEST002",
            name="Sousse Station",
            city="Sousse",
            availability="Available", power="50kW", ports="2", network="X", price="X", reliability="X",
            latitude=35.8, longitude=10.6
        )
        response = self.client.get('/api/stations/?city=Tunis')
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['city'], "Tunis")


class FavoriteTests(TestCase):
    """
    Test suite for user favorites management.
    """
    
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(username='favuser', password='pass123')
        self.station = ChargingStation.objects.create(
            station_id="ST001", name="Fav Station", 
            city="X", availability="X", power="X", ports="X", network="X", price="X", reliability="X",
            latitude=0.0, longitude=0.0
        )
        
        # Authenticate
        login_res = self.client.post('/api/auth/login/', {'username': 'favuser', 'password': 'pass123'})
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {login_res.data["access"]}')

    def test_add_favorite(self):
        """Test 6: Authenticated user can favorite a station."""
        data = {'station': self.station.id}
        response = self.client.post('/api/favorites/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(Favorite.objects.filter(user=self.user, station=self.station).exists())

    def test_get_user_favorites(self):
        """Test 7: Users only see their own favorites."""
        Favorite.objects.create(user=self.user, station=self.station)
        response = self.client.get('/api/favorites/')
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['station'], self.station.id)

    def test_remove_favorite(self):
        """Test 8: User can remove a station from favorites."""
        fav = Favorite.objects.create(user=self.user, station=self.station)
        response = self.client.delete(f'/api/favorites/{fav.id}/')
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Favorite.objects.filter(id=fav.id).exists())


class ContributionTests(TestCase):
    """
    Test suite for community-driven station contributions.
    """
    
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(username='contributor', password='pass123')
        login_res = self.client.post('/api/auth/login/', {'username': 'contributor', 'password': 'pass123'})
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {login_res.data["access"]}')

    def test_submit_contribution(self):
        """Test 9: Logged-in user can submit a new station for approval."""
        data = {
            'name': 'New Community Station',
            'latitude': 36.9,
            'longitude': 10.3,
            'speed': '150 kW',
            'status': 'Available'
        }
        response = self.client.post('/api/contributions/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        contribution = ContributedStation.objects.get(name='New Community Station')
        self.assertEqual(contribution.submitted_by, self.user)
        self.assertFalse(contribution.approved)

    def test_unauthenticated_contribution_fails(self):
        """Test 10: Anonymous users cannot submit stations."""
        self.client.credentials() # Clear credentials
        data = {'name': 'Anonymous', 'latitude': 0, 'longitude': 0, 'speed': 'X'}
        response = self.client.post('/api/contributions/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class CommunityAlertTests(TestCase):
    """
    Test suite for community alerts and reporting.
    """
    
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(username='reporter', password='pass123')
        login_res = self.client.post('/api/auth/login/', {'username': 'reporter', 'password': 'pass123'})
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {login_res.data["access"]}')

    def test_create_alert(self):
        """Test 11: User can report a community alert (e.g., station broken)."""
        data = {
            'alert_type': 'BROKEN',
            'description': 'The charger screen is dead.',
            'latitude': 36.85,
            'longitude': 10.25
        }
        response = self.client.post('/api/alerts/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(CommunityAlert.objects.filter(user=self.user, alert_type='BROKEN').exists())

    def test_list_alerts_publicly(self):
        """Test 12: Community alerts are visible to everyone."""
        CommunityAlert.objects.create(
            user=self.user, alert_type='MAINTENANCE', description='X', 
            latitude=36.8, longitude=10.2
        )
        self.client.credentials() # Logout
        response = self.client.get('/api/alerts/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)


class RatingTests(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(username='rater', password='pass123')
        self.station = ChargingStation.objects.create(
            station_id="ST002", name="Rate Station", 
            city="X", availability="Available", power="X", ports="X", network="X", price="X", reliability="X",
            latitude=0.0, longitude=0.0
        )
        login_res = self.client.post('/api/auth/login/', {'username': 'rater', 'password': 'pass123'})
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {login_res.data["access"]}')

    def test_duplicate_rating_updates_existing(self):
        """Test that submitting a second rating updates the existing one."""
        self.client.post('/api/ratings/', {'station': self.station.id, 'stars': 3, 'comment': 'Good'}, format='json')
        response = self.client.post('/api/ratings/', {'station': self.station.id, 'stars': 5, 'comment': 'Great'}, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        from .models import StationRating
        rating = StationRating.objects.get(user=self.user, station=self.station)
        self.assertEqual(rating.stars, 5)
        self.assertEqual(rating.comment, 'Great')

    def test_user_rating_included_in_station_list(self):
        """Test that user's rating is included in the station data."""
        from .models import StationRating
        StationRating.objects.create(user=self.user, station=self.station, stars=4, comment='Nice')
        
        response = self.client.get('/api/stations/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        station_data = next(s for s in response.data if s['station_id'] == "ST002")
        self.assertEqual(station_data['userRating'], 4)


class CheckInTests(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.user = User.objects.create_user(username='driver', password='pass123')
        self.station = ChargingStation.objects.create(
            station_id="ST003", name="CheckIn Station", 
            city="X", availability="Available", power="150 kW", ports="X", network="X", price="X", reliability="X",
            latitude=0.0, longitude=0.0
        )
        login_res = self.client.post('/api/auth/login/', {'username': 'driver', 'password': 'pass123'})
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {login_res.data["access"]}')

    def test_check_in_creates_history_and_sets_busy(self):
        """Test that check-in sets station to Busy and creates history."""
        response = self.client.post(f'/api/stations/{self.station.id}/check_in/', {'action': 'start'}, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        self.station.refresh_from_db()
        self.assertEqual(self.station.availability, "Busy")
        self.assertIsNotNone(self.station.busy_until)
        
        from .models import HistorySession
        self.assertTrue(HistorySession.objects.filter(user=self.user, station=self.station, route_only=False).exists())

    def test_auto_reset_busy_stations(self):
        """Test that get_queryset resets expired busy stations."""
        import datetime
        from django.utils import timezone
        
        self.station.availability = "Busy"
        self.station.busy_until = timezone.now() - datetime.timedelta(minutes=5)
        self.station.save()
        
        response = self.client.get('/api/stations/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        self.station.refresh_from_db()
        self.assertEqual(self.station.availability, "Available")
