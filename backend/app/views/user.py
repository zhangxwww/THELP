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


@user.route('/edit', methods=['POST'])
def edit():
    phone = session.get('phone')
    if phone is None:
        return {
            'success': False,
            'error_msg': 'Login please'
        }
    phone = int(phone)
    u = User.query.filter(User.phone == phone).first()
    password_old = request.form.get('password_old')
    password_new = request.form.get('password_new')
    if password_new is not None:
        if password_old is None:
            return {
                'success': False,
                'error_msg': 'Old password is required'
        }
        if password_old != u.password:
            return {
                'success': False,
                'error_msg': 'Wrong password'
            }
        u.password = password_new

    nickname = request.form.get('nickname')
    if nickname is not None:
        u.nickname = nickname

    signature = request.form.get('signature')
    if signature is not None:
        u.signature = signature

    db.session.commit()
    return {
        'success': True,
        'error_msg': ''
    }

