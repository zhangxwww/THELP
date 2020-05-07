from flask import Blueprint
from flask import request

from app.models import User

user = Blueprint('user', __name__)


@user.route('/hello')
def hello():
    return 'Hello world'


@user.route('/signup', methods=['POST'])
def signup():
    pass
