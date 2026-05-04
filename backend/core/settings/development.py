from .base import *

DEBUG = True
ALLOWED_HOSTS = ['*']

# Local PostgreSQL with PostGIS
DATABASES = {
    'default': {
        'ENGINE': 'django.contrib.gis.db.backends.postgis',
        'NAME': 'smartlocator_db',
        'USER': 'postgres',
        'PASSWORD': 'yourpassword', # Update this!
        'HOST': 'localhost',
        'PORT': '5432',
    }
}
