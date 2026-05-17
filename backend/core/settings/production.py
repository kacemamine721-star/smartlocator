import os
import environ
import dj_database_url
from .base import *

env = environ.Env()
# Read .env file if it exists
environ.Env.read_env(os.path.join(BASE_DIR, '.env'))

DEBUG = False
ALLOWED_HOSTS = ['smartlocator-api.onrender.com']

# Render Database URL
DATABASES = {'default': dj_database_url.parse(env('DATABASE_URL'))}
# Use standard PostgreSQL since models use FloatField instead of PointField.
# This avoids GDAL dependency issues on Windows during local management commands.
DATABASES['default']['ENGINE'] = 'django.db.backends.postgresql'

STATIC_ROOT = os.path.join(BASE_DIR, 'staticfiles')

# Redis Cache (Upstash)
# Fallback to local memory cache if REDIS_URL is not provided
redis_url = env("REDIS_URL", default="")
if redis_url:
    CACHES = {
        "default": {
            "BACKEND": "django_redis.cache.RedisCache",
            "LOCATION": redis_url,
            "OPTIONS": {
                "CLIENT_CLASS": "django_redis.client.DefaultClient",
            }
        }
    }
else:
    CACHES = {
        "default": {
            "BACKEND": "django.core.cache.backends.locmem.LocMemCache",
            "LOCATION": "unique-snowflake",
        }
    }

# File Storage (Cloudflare R2)
# Fallback to local storage if credentials are missing
r2_access_key = env("R2_ACCESS_KEY", default="")
if r2_access_key:
    STORAGES = {
        "default": {
            "BACKEND": "storages.backends.s3.S3Storage",
            "OPTIONS": {
                "access_key": r2_access_key,
                "secret_key": env("R2_SECRET_KEY", default=""),
                "bucket_name": env("R2_BUCKET_NAME", default=""),
                "endpoint_url": env("R2_ENDPOINT_URL", default=""),
                "region_name": "auto",
            },
        },
        "staticfiles": {
            "BACKEND": "whitenoise.storage.CompressedManifestStaticFilesStorage",
        },
    }
else:
    STORAGES = {
        "default": {
            "BACKEND": "django.core.files.storage.FileSystemStorage",
        },
        "staticfiles": {
            "BACKEND": "whitenoise.storage.CompressedManifestStaticFilesStorage",
        },
    }
