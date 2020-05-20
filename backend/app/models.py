from app import db

from datetime import datetime


class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    phone = db.Column(db.Integer, unique=True)
    password = db.Column(db.String(32))
    nickname = db.Column(db.String(18), nullable=True)
    avatar = db.Column(db.String(256), nullable=True)
    signature = db.Column(db.Text, nullable=True)
    score = db.Column(db.Float, default=0.0)
    finished_orders = db.Column(db.Integer, default=0)

    def __repr__(self):
        return '<User id:{} name:{}>'.format(self.id, self.nickname)


class Order(db.Model):
    __tablename__ = 'order'
    order_id = db.Column(db.Integer, primary_key=True)

    customer_id = db.Column(db.Integer)
    handler_id = db.Column(db.Integer, nullable=True)

    title = db.Column(db.String(64))
    description = db.Column(db.Text)
    genre = db.Column(db.String(32), default='other')
    state = db.Column(db.String(10), default='active')

    start_time = db.Column(db.DateTime)
    end_time = db.Column(db.DateTime)
    create_time = db.Column(db.DateTime, default=datetime.now)
    accept_time = db.Column(db.DateTime, nullable=True)
    finish_time = db.Column(db.DateTime, nullable=True)

    target_location = db.Column(db.Text)
    handler_location = db.Column(db.Text, nullable=True)

    reward = db.Column(db.Float, default=0.0)
    assessment = db.Column(db.Float, default=5.0)
