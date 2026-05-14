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
)

router = DefaultRouter()
router.register(r'stations', ChargingStationViewSet)
router.register(r'favorites', FavoriteViewSet, basename='favorite')
router.register(r'history', HistorySessionViewSet, basename='history')
router.register(r'ratings', StationRatingViewSet, basename='rating')
router.register(r'contributions', ContributedStationViewSet, basename='contribution')

urlpatterns = [
    path('auth/register/', RegisterView.as_view(), name='auth-register'),
    path('auth/login/', LoginView.as_view(), name='auth-login'),
    path('auth/refresh/', TokenRefreshView.as_view(), name='auth-refresh'),
    path('', include(router.urls)),
]
