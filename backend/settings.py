from datetime import timedelta
import os

SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(os.path.abspath(os.path.dirname(__file__)), 'thelp.sqlite')
SQLALCHEMY_TRACK_MODIFICATIONS = False
SECRET_KEY = 'akj3jv9i2os0ad'
PERMANENT_SESSION_LIFETIME = timedelta(days=365)
