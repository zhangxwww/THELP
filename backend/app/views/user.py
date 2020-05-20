from flask import Blueprint
from flask import request
from flask import session
from sqlalchemy import and_

from app import db
from app.models import User

from .utils import session_id_required, generate_static_filename
from .return_value import success, field_required, fail

user = Blueprint('user', __name__)


@user.route('/hello')
def hello(s='hello world'):
    import os
    print(os.path.dirname(__file__))
    return s


@user.route('/signup', methods=['POST'])
def signup():
    phone = request.json.get('phone')
    password = request.json.get('password')
    if phone is None:
        return field_required('Phone')
    try:
        phone = int(phone)
    except ValueError:
        return fail('Invalid phone number')
    if password is None:
        return field_required('Password')
    u = User.query.filter(User.phone == phone).with_for_update().first()
    if u:
        return fail('Phone number has been used')
    u = User(phone=phone, password=password)
    db.session.add(u)
    db.session.commit()
    return success()


@user.route('/login', methods=['POST'])
def login():
    phone = request.json.get('phone')
    password = request.json.get('password')
    if phone is None:
        return field_required('Phone')
    if password is None:
        return field_required('Password')
    try:
        phone = int(phone)
    except ValueError:
        return fail('Invalid phone number')
    if not User.query.filter(and_(User.phone == phone, User.password == password)):
        return fail('Wrong phone number or password')
    session['phone'] = str(phone)
    return success()


@user.route('/edit', methods=['POST'])
@session_id_required
def edit(u=None):
    password_old = request.json.get('password_old')
    password_new = request.json.get('password_new')
    if password_new is not None:
        if password_old is None:
            return fail('Old password is required')
        if password_old != u.password:
            return fail('Wrong password')
        u.password = password_new

    nickname = request.json.get('nickname')
    if nickname is not None:
        u.nickname = nickname

    signature = request.json.get('signature')
    if signature is not None:
        u.signature = signature

    db.session.commit()
    return success()


@user.route('/upload_avatar', methods=['POST'])
@session_id_required
def upload(u=None):
    f = request.files.get('file')
    if f is None:
        return field_required('File')
    filename = generate_static_filename(f.filename, 'avatar')
    f.save(filename)
    u.avatar = filename
    db.session.commit()
    return success()


@user.route('/info', methods=['GET'])
def info():
    user_id = request.json.get('user_id')
    if user_id is None:
        return field_required('user_id')
    u = User.query.filter(User.id == user_id).first()
    if u is None:
        return fail('User<id: {}> not exists'.format(user_id))
    return success({
        'nickname': u.nickname,
        'avatar': u.avatar,
        'signature': u.signature,
        'score': u.score
    })
