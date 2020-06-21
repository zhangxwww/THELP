from flask import session

from app.models import User, ImageNameRelation
from app.models import Order
from app.models import Message

from sqlalchemy import or_, and_

import os
import time
from datetime import datetime

from .return_value import field_required, permission_denied


def get_user_by_session_id():
    phone = session.get('phone')
    if phone is None:
        return None
    phone = int(phone)
    u = User.query.filter(User.phone == phone).first()
    return u


def get_order(order_id):
    if order_id is None:
        return None, field_required('order_id')
    o = Order.query.filter(Order.order_id == order_id).first()
    if o is None:
        return None, {
            'success': False,
            'error_msg': 'Order<id: {}> not exists'.format(order_id)
        }
    return o, None


def check_order_relation(o, u, relation):
    if relation == 'customer' and o.customer_id != u.id or relation == 'handler' and o.handler_id != u.id:
        return None, permission_denied()
    return o, None


def get_message_with(my_id, other_id, page, per_page):
    condition1 = and_(Message.from_id == my_id, Message.to_id == other_id)
    condition2 = and_(Message.from_id == other_id, Message.to_id == my_id)
    query = Message.query.filter(or_(condition1, condition2))

    pagination = query.order_by(Message.time.desc()).paginate(page, per_page=per_page, error_out=False)
    messages = pagination.items
    msg_list = []
    unread_messages = []
    for m in messages:
        from_id = m.from_id
        to_id = m.to_id

        if to_id == my_id:
            unread_messages.append(m)

        from_user = User.query.filter(User.id == from_id).first()
        to_user = User.query.filter(User.id == to_id).first()
        msg_list.append({
            'from': {
                'id': from_id,
                'name': from_user.nickname,
                'avatar': from_user.avatar
            },
            'to': {
                'id': to_id,
                'name': to_user.nickname,
                'avatar': to_user.avatar
            },
            'content': m.content,
            'content_type': m.content_type,
            'time': datetime_2_ymdhms(m.time),
            'has_read': m.has_read
        })
    return msg_list, unread_messages


def get_latest_message_with(my_id, other_id):
    condition1 = and_(Message.from_id == my_id, Message.to_id == other_id)
    condition2 = and_(Message.from_id == other_id, Message.to_id == my_id)
    query = Message.query.filter(or_(condition1, condition2))

    message = query.order_by(Message.time.desc()).first()

    return message


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
    base_path = os.path.dirname(__file__)[:-5]
    path = os.path.join(base_path, 'static', type_)
    if not os.path.exists(path):
        os.makedirs(path)
    return path


def generate_static_filename(name, type_):
    ext = name.split('.')[-1]
    t = str(int(time.time() * 1000000))
    path = get_static_path(type_)
    filename = '{}.{}'.format(t, ext)
    return os.path.join(path, filename)


def delete_url_file(url, type_):
    filename = url_2_filename(url)
    delete_file(filename, type_)


def delete_file(name, type_):
    path = get_static_path(type_)
    filename = os.path.join(path, name)
    if os.path.exists(filename):
        os.remove(filename)


def url_2_filename(url):
    return os.path.split(url)[1]


def filename_2_url(filename, type_):
    _, name = os.path.split(filename)
    return '/static/{}/{}'.format(type_, name)


def str_2_datetime(dt):
    form = '%Y.%m.%d %H:%M'
    try:
        return datetime.strptime(dt, form)
    except ValueError:
        form = '%Y-%m-%d %H:%M:%S'
        try:
            return datetime.strptime(dt, form)
        except ValueError:
            return None


def datetime_2_mdhm(dt):
    if dt is None:
        return None
    mdhmStr = dt.strftime('%m-%d %H:%M')
    return mdhmStr


def datetime_2_ymdhms(dt):
    if dt is None:
        return None
    ymdhmsStr = dt.strftime('%Y-%m-%d %H:%M:%S')
    return ymdhmsStr


def get_lat_longi_from_location(dt):
    if dt is None:
        return 0, 0
    lat, longi = dt.split(',', 1)
    return lat, longi
