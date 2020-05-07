from flask import Blueprint
from flask import request

from app.models import User

msg = Blueprint('msg', __name__)
