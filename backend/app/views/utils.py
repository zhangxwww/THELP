from flask import session

from app.models import User
from app.models import Order

import os
import time


def get_user_by_session_id():
    phone = session.get('phone')
    if phone is None:
        return None
    phone = int(phone)
    u = User.query.filter(User.phone == phone).first()
    return u


def get_order(order_id):
    if order_id is None:
        return None, {
            'success': False,
            'error_msg': 'Order id required'
        }
    o = Order.query.filter(Order.order_id == order_id).first()
    if o is None:
        return None, {
            'success': False,
            'error_msg': 'Order<id: {}> not exists'.format(order_id)
        }


def check_order_relation(o, u, relation):
    if relation == 'customer' and o.customer_id != u.id or relation == 'handler' and o.handler_id != u.id:
        return None, {
            'success': False,
            'error_msg': 'Permission denied'
        }
    return o, None


def session_id_required(f):
    def g(*args, **kwargs):
        u = get_user_by_session_id()
        if u is None:
            return {
                'success': False,
                'error_msg': 'Login please'
            }
        return f(*args, u=u, **kwargs)

    g.__name__ = f.__name__
    return g


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
