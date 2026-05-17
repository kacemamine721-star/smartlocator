from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenRefreshView

from .views import (
    ChargingStationViewSet,
    ContributedStationViewSet,
    FavoriteViewSet,
    HistorySessionViewSet,
    LoginView,
    RegisterView,
    StationRatingViewSet,
    CommunityAlertViewSet,
    EVVehicleViewSet,
    UserMeView,
)

router = DefaultRouter()
router.register(r'stations', ChargingStationViewSet, basename='station')
router.register(r'favorites', FavoriteViewSet, basename='favorite')
router.register(r'history', HistorySessionViewSet, basename='history')
router.register(r'ratings', StationRatingViewSet, basename='rating')
router.register(r'contributions', ContributedStationViewSet, basename='contribution')
router.register(r'alerts', CommunityAlertViewSet, basename='alert')
router.register(r'vehicles', EVVehicleViewSet, basename='vehicle')

urlpatterns = [
    path('auth/register/', RegisterView.as_view(), name='auth-register'),
    path('auth/login/', LoginView.as_view(), name='auth-login'),
    path('auth/refresh/', TokenRefreshView.as_view(), name='auth-refresh'),
    path('users/me/', UserMeView.as_view(), name='user-me'),
    path('', include(router.urls)),
]
