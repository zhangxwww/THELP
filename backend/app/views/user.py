from flask import Blueprint
from flask import request
from flask import session
from sqlalchemy import and_

from app import db
from app.models import User

from .utils import get_user_by_session_id, generate_static_filename

user = Blueprint('user', __name__)


@user.route('/hello')
def hello():
    import os
    print(os.path.dirname(__file__))
    return 'Hello world'


@user.route('/signup', methods=['POST'])
def signup():
    phone = request.json.get('phone')
    password = request.json.get('password')
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
    u = User.query.filter(User.phone == phone).with_for_update().first()
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
    phone = request.json.get('phone')
    password = request.json.get('password')
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
    u = get_user_by_session_id()
    if u is None:
        return {
            'success': False,
            'error_msg': 'Login please'
        }
    password_old = request.json.get('password_old')
    password_new = request.json.get('password_new')
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

    nickname = request.json.get('nickname')
    if nickname is not None:
        u.nickname = nickname

    signature = request.json.get('signature')
    if signature is not None:
        u.signature = signature

    db.session.commit()
    return {
        'success': True,
        'error_msg': ''
    }


@user.route('/upload_avatar', methods=['POST'])
def upload():
    u = get_user_by_session_id()
    if u is None:
        return {
            'success': False,
            'error_msg': 'Login please'
        }
    f = request.files.get('file')
    if f is None:
        return {
            'success': False,
            'error_msg': 'No file received'
        }
    filename = generate_static_filename(f.filename, 'avatar')
    f.save(filename)
    u.avatar = filename
    db.session.commit()
    return {
        'success': True,
        'error_msg': ''
    }
