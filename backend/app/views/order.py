from flask import Blueprint
from flask import request

from app import db

from app.models import User
from app.models import Order

from datetime import datetime

from .utils import session_id_required, get_order, check_order_relation, str_2_datetime
from .return_value import success, field_required, permission_denied, fail

order = Blueprint('order', __name__)


@order.route('/homepage', methods=['POST'])
def homepage():
    query = Order.query.filter(Order.state == 'active')
    title = request.json.get('order_title')
    if title is not None:
        query = query.filter(Order.title.like('%' + title + '%'))
    start_time = request.json.get('order_start_time')
    if start_time is not None:
        query = query.filter(Order.start_time >= start_time)
    end_time = request.json.get('order_end_time')
    if end_time is not None:
        query = query.filter(Order.end_time <= end_time)
    reward_inf = request.json.get('order_reward_inf')
    if reward_inf is not None:
        query = query.filter(Order.reward >= float(reward_inf))
    reward_sup = request.json.get('order_reward_sup')
    if reward_inf is not None:
        query = query.filter(Order.reward >= float(reward_sup))
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
    pagination = query.order_by(Order.create_time.desc()).paginate(page, per_page=per_page, error_out=False)
    orders = pagination.items
    order_list = []
    for o in orders:
        u = User.query.filter(o.customer_id == User.id).first()
        order_list.append({
            'order_id': o.order_id,
            'title': o.title,
            'description': o.description,
            'genre': o.genre,
            'start_time': o.start_time,
            'end_time': o.end_time,
            'target_location': o.target_location,
            'reward': o.reward,
            'customer_name': u.nickname,
            'customer_id': u.id,
            'avatar': u.avatar
        })
    return success({
        'order_list': order_list
    })


@order.route('/create', methods=['POST'])
@session_id_required
def create(u=None):
    required_fields = {
        'title': '',
        'description': '',
        'genre': '',
        'start_time': '',
        'end_time': '',
        'target_location': '',
        'reward': 0
    }
    trans_func = {
        'reward': int,
        'start_time': str_2_datetime,
        'end_time': str_2_datetime
    }
    order_info = request.json.get('order_info', {})
    for k in required_fields.keys():
        field = order_info.get(k)
        if field is None:
            return field_required(field)
        if k in trans_func.keys():
            field = trans_func[k](field)
        required_fields[k] = field
    if required_fields['start_time'] is None or required_fields['end_time'] is None:
        return fail('Invalid time format!')
    required_fields['customer_id'] = u.id
    required_fields['state'] = 'active'
    o = Order(**required_fields)
    db.session.add(o)
    db.session.commit()
    return success()


@order.route('/edit', methods=['POST'])
@session_id_required
def edit(u=None):
    order_info = request.json.get('order_info', {})
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if not check_order_relation(o, u, 'customer'):
        return permission_denied()
    if 'title' in order_info.keys():
        o.title = order_info['title']
    if 'description' in order_info.keys():
        o.description = order_info['description']
    if 'genre' in order_info.keys():
        o.genre = order_info['genre']
    if 'start_time' in order_info.keys():
        o.start_time = order_info['start_time']
    if 'end_time' in order_info.keys():
        o.end_time = order_info['end_time']
    if 'target_location' in order_info.keys():
        o.target_location = order_info['target_location']
    if 'reward' in order_info.keys():
        o.reward = order_info['reward']
    db.session.commit()
    return success()


@order.route('/cancel', methods=['POST'])
@session_id_required
def cancel(u=None):
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if not check_order_relation(o, u, 'customer'):
        return permission_denied()
    o.state = 'canceled'
    db.session.commit()
    return success()


@order.route('/accept', methods=['POST'])
@session_id_required
def accept(u=None):
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if o.state != 'active':
        return fail('Order<id: {}> is not active'.format(order_id))
    o.state = 'accepted'
    o.handler = u.id
    o.accept_time = datetime.now()
    db.session.commit()
    return success()


@order.route('/finish', methods=['POST'])
@session_id_required
def finish(u=None):
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if not check_order_relation(o, u, 'handler'):
        return permission_denied()
    if o.state != 'accepted':
        return fail('Order<id: {}> is not accepted'.format(order_id))
    o.state = 'finished'
    o.finish_time = datetime.now()
    db.session.commit()
    return success()


@order.route('/abort', methods=['POST'])
@session_id_required
def abort(u=None):
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if not check_order_relation(o, u, 'customer'):
        return permission_denied()
    if o.state != 'accepted':
        return fail('Order<id: {}> is not accepted'.format(order_id))
    o.state = 'active'
    db.session.commit()
    db.session.commit()
    return success()


@order.route('/assess', methods=['POST'])
@session_id_required
def access(u=None):
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    if not check_order_relation(o, u, 'customer'):
        return permission_denied()
    if o.state != 'finished':
        return fail('Order<id: {}> is not finished'.format(order_id))
    o.state = 'assessed'
    assess = request.json.get('assess')
    if assess is None:
        return field_required('Assess')
    o.assessment = float(assess)

    h_id = o.handler_id
    if h_id is not None:
        handler = User.query.filter(User.id == h_id).first()
        if handler is not None:
            finished = handler.finished_orders
            score = handler.score
            handler.score = (score * finished + assess) / (finished + 1)
            handler.finished_orders = finished + 1

    db.session.commit()
    return success()


@order.route('/detail', methods=['POST'])
def detail():
    order_id = request.json.get('order_id')
    o, ret = get_order(order_id)
    if o is None:
        return ret
    customer = User.query.filter(o.customer_id == User.id).first()
    res = {
        'customer_id': o.customer_id,
        'customer': customer.nickname,
        'title': o.title,
        'description': o.description,
        'genre': o.genre,
        'state': o.state,
        'start_time': o.start_time,
        'end_time': o.end_time,
        'create_time': o.create_time,
        'accept_time': o.accept_time,
        'finish_time': o.finish_time,
        'target_location': o.target_location,
        'handler_location': o.handler_location,
        'reward': o.reward,
        'assessment': o.assessment
    }
    if o.handler_id is not None:
        handler = User.query.filter(o.handler_id == User.id).first()
        if handler is not None:
            res['handler_id'] = o.handler_id
            res['handler'] = handler.nickname
    return success(res)
