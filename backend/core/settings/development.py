from .base import *

DEBUG = True
ALLOWED_HOSTS = ['*']

# Local SQLite for easy development
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR / 'db.sqlite3',
    }
}
