from flask import Blueprint
from flask import request

from app import db

from app.models import User
from app.models import Order

from .utils import session_id_required

order = Blueprint('order', __name__)


@order.route('/homepage', methods=['GET'])
def homepage():
    query = Order.query.filter(Order.state == 'active')
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
    page = request.json.get('page', 1, type=int)
    per_page = request.json.get('num_each_page', 10, type=int)
    pagination = query.order_by(Order.create_time.desc()).paginate(page, per_page=per_page, error_out=False)
    orders = pagination.items
    order_list = [{
        'order_id': o.order_id,
        'title': o.title,
        'description': o.description,
        'genre': o.genre,
        'start_time': o.start_time,
        'end_time': o.end_time,
        'target_location': o.target_location,
        'reward': o.reward
    } for o in orders]
    return {
        'order_list': order_list,
        'success': True,
        'error_msg': ''
    }


@order.route('/create', methods=['POST'])
@session_id_required
def create(u=None):
    required_fields = {
        'title': '',
        'description': '',
        'genre': '',
        'state': '',
        'start_time': '',
        'end_time': '',
        'target_location': '',
        'reward': ''
    }
    trans_func = {
        'reward': int
    }
    for k in required_fields.keys():
        field = request.json.get(k)
        if field is None:
            return {
                'success': False,
                'error_msg': '{} required'.format(k)
            }
        if k in trans_func.keys():
            field = trans_func[k](field)
        required_fields[k] = field
    required_fields['customer_id'] = u.id
    o = Order(**required_fields)
    db.session.add(o)
    db.session.commit()
    return {
        'order_id': o.order_id,
        'success': True,
        'error_msg': ''
    }
