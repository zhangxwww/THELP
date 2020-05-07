from flask import Blueprint
from flask import request
from flask import session
from sqlalchemy import and_

from app import db
from app.models import User

user = Blueprint('user', __name__)


@user.route('/hello')
def hello():
    return 'Hello world'


@user.route('/signup', methods=['POST'])
def signup():
    phone = request.form.get('phone')
    password = request.form.get('password')
    if phone is None:
        return {
            'success': False,
            'error_msg': 'Phone is required'
        }
    try:
        phone = int(phone)
    except ValueError:
        return {
            'success': False,
            'error_msg': 'Invalid phone number'
        }
    if password is None:
        return {
            'success': False,
            'error_msg': 'Password is required'
        }
    u = User.query.filter(User.phone == phone).first()
    if u:
        return {
            'success': False,
            'error_msg': 'Phone number has been used'
        }
    u = User(phone=phone, password=password)
    db.session.add(u)
    db.session.commit()
    return {
        'success': True,
        'error_msg': ''
    }


@user.route('/login', methods=['POST'])
def login():
    phone = request.form.get('phone')
    password = request.form.get('password')
    if phone is None:
        return {
            'success': False,
            'error_msg': 'Phone number iis required'
        }
    if password is None:
        return {
            'success': False,
            'error_msg': 'password number iis required'
        }
    try:
        phone = int(phone)
    except ValueError:
        return {
            'success': False,
            'error_msg': 'Invalid phone number'
        }
    if not User.query.filter(and_(User.phone == phone, User.password == password)):
        return {
            'success': False,
            'error_msg': 'Wrong phone number or password'
        }
    session['phone'] = str(phone)
    return {
        'success': True,
        'error_msg': ''
    }

