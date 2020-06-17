from flask import Blueprint
from flask import request

from app import db
from app.models import User, Message, ImageNameRelation

from sqlalchemy import or_, and_, not_

from geventwebsocket.websocket import WebSocket, WebSocketError

import json

from .return_value import field_required, success, fail
from .utils import generate_static_filename, get_latest_message_with, datetime_2_ymdhms
from .utils import session_id_required
from .utils import get_message_with

msg = Blueprint('msg', __name__)


@msg.route('/upload', methods=['POST'])
def upload():
    f = request.files.get('file')
    if f is None:
        return field_required('File')
    filename = generate_static_filename(f.filename, 'image')
    f.save(filename)
    r = ImageNameRelation(generated_name=filename, raw_name=f.filename)
    db.session.add(r)
    db.session.commit()
    return success()


user_socket_dict = {}


@msg.route('/websocket')
@session_id_required
def websocket(u=None):
    global user_socket_dict

    user_socket = request.environ.get('wsgi.websocket')
    if not user_socket:
        return fail('Websocket required')
    from_id = u.id
    user_socket_dict[from_id] = user_socket

    while True:
        try:
            message_received = user_socket.receive()
            if message_received is None:
                continue
            message_received = json.loads(message_received)
            to_id = message_received.get('to_id', None)
            content_type = message_received.get('content_type', None)
            content = message_received.get('content', None)
            if to_id is None or content is None or content is None:
                continue
            if content_type == 'IMAGE':
                r = ImageNameRelation.query.filter(ImageNameRelation.raw_name == content).first()
                if r is None:
                    continue
                content = r.generated_name
            message_send = {
                'from_id': from_id,
                'content_type': content_type,
                'content': content
            }

            m = Message(
                from_id=from_id, to_id=to_id,
                content_type=content_type, content=content
            )
            db.session.add(m)
            db.session.commit()

            to_user_socket = user_socket_dict.get(to_id, None)
            if to_user_socket is not None:
                to_user_socket.send(json.dumps(message_send))
        except WebSocketError as e:
            print(e)
            user_socket_dict.pop(from_id)
            break


@msg.route('/history', methods=['POST'])
@session_id_required
def history(u=None):
    receivers = Message.query.filter(Message.from_id == u.id).with_entities(Message.to_id).distinct().all()
    senders = Message.query.filter(Message.to_id == u.id).with_entities(Message.from_id).distinct().all()

    related_user_ids = list(set(list(receivers)) | set(list(senders)))

    page = request.json.get('page', 1)
    try:
        page = int(page)
    except ValueError:
        return fail('Invalid page')
    per_page = request.json.get('num_each_page', 10)
    try:
        per_page = int(per_page)
    except ValueError:
        return fail('Invalid num each page')

    user_msg_list = []
    for uid in related_user_ids:
        uid = uid[0]
        messages, _ = get_message_with(u.id, uid, page, per_page)
        message = get_latest_message_with(u.id, uid)
        other_u = User.query.filter(User.id == uid).first()
        user_msg_list.append({
            'other_id': uid,
            'other_name': other_u.nickname,
            'other_avatar': other_u.avatar,
            'content': message.content,
            'content_type': message.content_type,
            'time': datetime_2_ymdhms(message.time),
            'has_read': message.has_read,
            'msg_list': messages
        })
    return success({
        'user_msg_list': user_msg_list
    })


@msg.route('/history/single', methods=['POST'])
@session_id_required
def single(u=None):
    other_id = request.json.get('other_id', None)
    try:
        other_id = int(other_id)
    except ValueError:
        return fail('Invalid other id')
    page = request.json.get('page', 1)
    try:
        page = int(page)
    except ValueError:
        return fail('Invalid page')
    per_page = request.json.get('num_each_page', 10)
    try:
        per_page = int(per_page)
    except ValueError:
        return fail('Invalid num each page')

    message_list, unread_messages = get_message_with(u.id, other_id, page, per_page)
    for m in unread_messages:
        m.has_read = True
    db.session.commit()

    return success({
        'msg_list': message_list
    })


@msg.route('/unread/count', methods=['POST'])
@session_id_required
def count(u=None):
    c = Message.query.filter(
        and_(Message.to_id == u.id,
             not_(Message.has_read))).count()
    return success({
        'count': c
    })
