import environ
import dj_database_url
from .base import *

env = environ.Env()

DEBUG = False
ALLOWED_HOSTS = ['smartlocator-api.onrender.com']

# Render Database URL
DATABASES = {'default': dj_database_url.parse(env('DATABASE_URL'))}
DATABASES['default']['ENGINE'] = 'django.contrib.gis.db.backends.postgis'

STATIC_ROOT = os.path.join(BASE_DIR, 'staticfiles')
STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'
