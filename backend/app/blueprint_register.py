from app import app
from app.views.user import user
from app.views.order import order
from app.views.msg import msg

app.register_blueprint(user, url_prefix='/user')
app.register_blueprint(order, url_prefix='/order')
app.register_blueprint(msg, url_prefix='/msg')
