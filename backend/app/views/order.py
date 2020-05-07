from flask import Blueprint
from flask import request

from app.models import User

order = Blueprint('order', __name__)
