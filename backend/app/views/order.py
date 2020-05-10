from flask import Blueprint
from flask import request

from app.models import User
from app.models import Order

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
