from flask import session

from app.models import User

import os
import time


def get_user_by_session_id():
    phone = session.get('phone')
    if phone is None:
        return None
    phone = int(phone)
    u = User.query.filter(User.phone == phone).first()
    return u


def get_static_path(type_):
    basepath = os.path.dirname(__file__)
    path = os.path.join('..', basepath, type_)
    if not os.path.exists(path):
        os.makedirs(path)
    return path


def generate_static_filename(name, type_):
    ext = name.split('.')[-1]
    t = str(int(time.time() * 1000000))
    path = get_static_path(type_)
    filename = '{}.{}'.format(t, ext)
    return os.path.join(path, filename)
